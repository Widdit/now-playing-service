package com.widdit.nowplaying.service;

import com.widdit.nowplaying.entity.Device;
import com.widdit.nowplaying.entity.RespData;
import com.widdit.nowplaying.entity.SettingsGeneral;
import com.widdit.nowplaying.entity.cmd.Args;
import com.widdit.nowplaying.entity.cmd.Option;
import com.widdit.nowplaying.event.MusicStatusUpdatedEvent;
import com.widdit.nowplaying.event.SettingsGeneralChangedEvent;
import com.widdit.nowplaying.util.ConsoleUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AudioService {

    @Autowired
    private SettingsService settingsService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // 当前播放状态（Playing, Paused, None）
    private volatile String status = "None";
    // 当前窗口标题
    private volatile String windowTitle = "";
    // 当前播放进度（秒），由 C# 端通过 Progress 行传递，-1 表示无进度
    private volatile int progressSeconds = -1;
    // 当前歌曲总时长（秒），由 C# 端通过 Progress 行传递，-1 表示无进度
    private volatile int totalSeconds = -1;

    // 首选音乐平台是否正在运行
    private boolean primaryPlatformRunning = true;

    private Process getMusicStatusProcess;
    private Thread musicStatusReaderThread;
    private Thread processShutdownHook;

    /**
     * 初始化操作。该方法会在该类实例被 Spring 创建时自动执行
     */
    @PostConstruct
    public void init() {
        startGetMusicStatus();
    }

    public String getStatus() {
        return status;
    }

    public String getWindowTitle() {
        return windowTitle;
    }

    public int getProgressSeconds() {
        return progressSeconds;
    }

    public int getTotalSeconds() {
        return totalSeconds;
    }

    public boolean getPrimaryPlatformRunning() {
        return primaryPlatformRunning;
    }

    /**
     * 启动 C# 程序 GetMusicStatus.exe 不断更新音乐状态（成员变量 status 和 windowTitle）
     */
    public void startGetMusicStatus() {
        // 封装命令行参数
        SettingsGeneral settingsGeneral = settingsService.getSettingsGeneral();

        String platform = settingsGeneral.getPlatform();
        if (settingsGeneral.getFallbackPlatformEnabled() && !primaryPlatformRunning) {
            platform = settingsGeneral.getFallbackPlatform();
        }

        List<Option> options = new ArrayList<>();
        options.add(new Option("--device-id", settingsGeneral.getDeviceId()));
        options.add(new Option("--platform", platform));
        options.add(new Option("--smtc", settingsGeneral.getSmtc().toString()));
        options.add(new Option("--poll-interval", settingsGeneral.getPollInterval().toString()));
        Args args = new Args(options);

        List<String> command = ConsoleUtil.getCommand("Assets\\AudioService\\GetMusicStatus.exe", args);

        // 运行并处理 C# 程序输出
        try {
            // 启动 C# 程序
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File("Assets"));  // 指定工作目录为 Assets
            getMusicStatusProcess = processBuilder.start();

            // 为了防止并发问题，在 Hook 内部使用局部变量捕获当前的进程对象
            final Process currentProc = getMusicStatusProcess;

            processShutdownHook = new Thread(() -> {
                // 这里检查 currentProc 而不是成员变量，确保只处理本次启动的进程
                if (currentProc != null && currentProc.isAlive()) {
                    currentProc.destroyForcibly();
                    log.info("Java 主程序退出，已清理 GetMusicStatus.exe 进程");
                }
            });

            Runtime.getRuntime().addShutdownHook(processShutdownHook);

            log.info("启动 C# 进程读取音乐状态");

            // 使用线程来异步读取 C# 程序的输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(getMusicStatusProcess.getInputStream()));

            // 启动一个线程来不断读取 C# 程序的输出
            musicStatusReaderThread = new Thread(() -> {
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        // 更新成员变量
                        if ("Playing".equals(line) || "Paused".equals(line) || "None".equals(line)) {
                            status = line.trim();
                            // 状态为 None 时，清空窗口标题和进度，防止关闭播放窗口后歌词继续播放
                            if ("None".equals(status)) {
                                windowTitle = "";
                                progressSeconds = -1;
                                totalSeconds = -1;
                            }
                        } else if (line.startsWith("Progress:")) {
                            // 解析 C# 端传递的精确进度，格式: "Progress:currentSec|totalSec"
                            try {
                                String[] parts = line.substring(9).split("\\|");
                                progressSeconds = Integer.parseInt(parts[0]);
                                totalSeconds = Integer.parseInt(parts[1]);
                            } catch (Exception e) {
                                log.debug("解析 Progress 行失败: {}", line);
                            }
                        } else {
                            windowTitle = line.trim();
                        }

                        // 发布事件，通知变化
                        eventPublisher.publishEvent(new MusicStatusUpdatedEvent(this, "音乐状态被更新"));
                    }
                } catch (Exception e) {
                    if (getMusicStatusProcess != null && getMusicStatusProcess.isAlive()) {
                        log.error("启用线程读取 C# 程序 GetMusicStatus.exe 失败：" + e.getMessage());
                    }
                }
            });
            musicStatusReaderThread.start();

        } catch (Exception e) {
            log.error("启动 C# 程序 GetMusicStatus.exe 失败：" + e.getMessage());
        }
    }

    /**
     * 结束 C# 程序 GetMusicStatus.exe
     */
    public void stopGetMusicStatus() {
        log.info("终止 C# 进程读取音乐状态");

        try {
            // 移除 Shutdown Hook
            if (processShutdownHook != null) {
                try {
                    // 如果手动停止了程序，就不需要在 Java 退出时再运行这个钩子了
                    Runtime.getRuntime().removeShutdownHook(processShutdownHook);
                } catch (IllegalStateException e) {
                    // 如果 JVM 已经在关闭过程中，移除钩子可能会抛出异常，这里忽略即可
                }
                processShutdownHook = null;
            }

            // 结束 C# 程序（使用 destroyForcibly 确保 Windows 上能杀死进程）
            if (getMusicStatusProcess != null) {
                getMusicStatusProcess.destroyForcibly();
            }

            // 停止读取线程
            if (musicStatusReaderThread != null) {
                musicStatusReaderThread.interrupt();
            }
        } catch (Exception e) {
            log.error("终止 C# 程序 GetMusicStatus.exe 失败：" + e.getMessage());
        }
    }

    /**
     * Spring 容器销毁时清理 C# 子进程
     */
    @javax.annotation.PreDestroy
    public void cleanup() {
        stopGetMusicStatus();
    }

    /**
     * 定时任务，用于更新首选音乐平台的运行状态
     * 每隔 5 秒执行一次
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void checkPrimaryPlatform() {
        SettingsGeneral settingsGeneral = settingsService.getSettingsGeneral();

        boolean fallbackPlatformEnabled = settingsGeneral.getFallbackPlatformEnabled();
        String platform = settingsGeneral.getPlatform();
        String fallbackPlatform = settingsGeneral.getFallbackPlatform();

        // 如果未启用备选音乐平台，则无需执行
        if (!fallbackPlatformEnabled) {
            return;
        }

        List<Option> options = new ArrayList<>();
        options.add(new Option("--platform", platform));
        Args args = new Args(options);

        // 执行 C# 程序 CheckMusicProcess.exe
        try {
            String stdOut = ConsoleUtil.runGetStdOut("Assets\\AudioService\\CheckMusicProcess.exe", args);
            boolean newPrimaryPlatformRunning = stdOut.contains("true");

            if (newPrimaryPlatformRunning != primaryPlatformRunning) {
                primaryPlatformRunning = newPrimaryPlatformRunning;

                if (newPrimaryPlatformRunning) {
                    log.info("检测到首选音乐平台已运行，开始识别首选音乐平台：" + platform);
                } else {
                    log.info("检测到首选音乐平台已关闭，开始识别备选音乐平台：" + fallbackPlatform);
                }

                // 重启 GetMusicStatus 进程，并清空播放状态和窗口标题
                stopGetMusicStatus();

                status = "None";
                windowTitle = "";
                progressSeconds = -1;
                totalSeconds = -1;

                startGetMusicStatus();
            }

        } catch (Exception e) {
            log.error("执行 C# 程序 CheckMusicProcess.exe 失败：" + e.getMessage());
        }
    }

    /**
     * 监听通用设置被修改的事件
     * @param event
     */
    @EventListener
    public void handleSettingsGeneralChange(SettingsGeneralChangedEvent event) {
        // 如果通用设置被修改，则重启 GetMusicStatus 进程，并清空播放状态和窗口标题
        stopGetMusicStatus();

        status = "None";
        windowTitle = "";
        progressSeconds = -1;
        totalSeconds = -1;

        startGetMusicStatus();
    }

    /**
     * 获取所有音频设备的列表
     * @return
     */
    public List<Device> getAudioDevices() {
        // 执行 C# 程序 GetAudioDevices.exe
        String stdOut = "";
        try {
            stdOut = ConsoleUtil.runGetStdOut("Assets\\AudioService\\GetAudioDevices.exe");
        } catch (Exception e) {
            log.error("获取音频设备列表失败：" + e.getMessage());
        }

        // 解析返回结果
        List<Device> devices = new ArrayList<>();
        devices.add(new Device("default", "主声音驱动程序"));

        String[] lines = stdOut.split("\n");
        for (String line : lines) {
            String[] split = line.split(" ", 2);

            devices.add(new Device(split[0], split[1]));
        }

        return devices;
    }

    /**
     * 获取音频设备识别结果
     * @param platform 音乐平台
     * @return
     */
    public RespData<String> deviceDetect(String platform) {
        if (platform == null || "".equals(platform)) {
            return new RespData<>("Fail! 缺少 platform 请求参数");
        }

        List<Option> options = new ArrayList<>();
        options.add(new Option("--platform", platform));
        Args args = new Args(options);

        // 执行 C# 程序 DeviceDetect.exe
        String stdOut = "";
        try {
            stdOut = ConsoleUtil.runGetStdOut("Assets\\AudioService\\DeviceDetect.exe", args);
        } catch (Exception e) {
            stdOut = "Fail! " + e.getMessage();
        }

        return new RespData<>(stdOut);
    }

}
