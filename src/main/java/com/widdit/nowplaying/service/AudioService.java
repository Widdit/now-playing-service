package com.widdit.nowplaying.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import java.util.Locale;

import static java.lang.Thread.sleep;

@Service
@Slf4j
public class AudioService {

    @Autowired
    private SettingsService settingsService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // 当前播放状态（Playing, Paused, None）
    private String status = "None";
    // 当前窗口标题
    private String windowTitle = "";
    // 当前播放进度（毫秒）
    private long progressMs = 0L;
    // 当前歌曲时长（毫秒）
    private int durationMs = 0;
    // 当前专辑名
    private String album = "";

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

    public long getProgressMs() {
        return progressMs;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public String getAlbum() {
        return album;
    }

    public boolean isMacMode() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac");
    }

    public boolean getPrimaryPlatformRunning() {
        return primaryPlatformRunning;
    }

    /**
     * 启动 C# 程序 GetMusicStatus.exe 不断更新音乐状态（成员变量 status 和 windowTitle）
     */
    public void startGetMusicStatus() {
        if (isMacMode()) {
            startMacNowPlaying();
            return;
        }

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

    private void startMacNowPlaying() {
        final String helperBin = resolveMintMacNpBin();
        if (helperBin == null) {
            log.error("未找到 mint-mac-np，可通过环境变量 MINT_MAC_NP_BIN 指定路径");
            return;
        }

        final SettingsGeneral settingsGeneral = settingsService.getSettingsGeneral();
        final String configuredPlatform = settingsGeneral.getPlatform();
        final String provider = configuredPlatform == null || configuredPlatform.isEmpty() ? "netease" : configuredPlatform;

        log.info("启动 mint-mac-np 读取音乐状态：{}", helperBin);

        musicStatusReaderThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Process process = null;
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder(helperBin, "--provider", provider);
                    process = processBuilder.start();
                    getMusicStatusProcess = process;

                    StringBuilder stdOut = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stdOut.append(line);
                        }
                    }

                    process.waitFor();
                    getMusicStatusProcess = null;

                    if (process.exitValue() != 0 || stdOut.length() == 0) {
                        updateMacState(null);
                    } else {
                        updateMacState(JSON.parseObject(stdOut.toString()));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("读取 mint-mac-np 失败：{}", e.getMessage());
                    updateMacState(null);
                } finally {
                    if (process != null && process.isAlive()) {
                        process.destroyForcibly();
                    }
                }

                try {
                    sleep(250);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "mint-mac-np-reader");

        musicStatusReaderThread.start();
    }

    private void updateMacState(JSONObject payload) {
        if (payload == null || !payload.getBooleanValue("ok")) {
            status = "None";
            windowTitle = "";
            progressMs = 0L;
            durationMs = 0;
            album = "";
            eventPublisher.publishEvent(new MusicStatusUpdatedEvent(this, "mac 音乐状态被更新"));
            return;
        }

        String title = payload.getString("title");
        String artist = payload.getString("artist");
        album = payload.getString("album") == null ? "" : payload.getString("album").trim();
        progressMs = payload.getLongValue("position_ms");
        durationMs = payload.getIntValue("duration_ms");
        boolean isPlaying = payload.getBooleanValue("is_playing");

        title = title == null ? "" : title.trim();
        artist = artist == null ? "" : artist.trim();

        if (title.isEmpty() && artist.isEmpty()) {
            status = "None";
            windowTitle = "";
        } else {
            status = isPlaying ? "Playing" : "Paused";
            windowTitle = artist.isEmpty() ? title : title + " - " + artist;
        }

        eventPublisher.publishEvent(new MusicStatusUpdatedEvent(this, "mac 音乐状态被更新"));
    }

    private String resolveMintMacNpBin() {
        String envPath = System.getenv("MINT_MAC_NP_BIN");
        if (envPath != null && !envPath.trim().isEmpty()) {
            File file = new File(envPath.trim());
            if (file.isFile()) {
                return file.getAbsolutePath();
            }
        }

        String[] candidates = new String[] {
                "mint-mac-np",
                "../server/mint/mint_mac_np/target/release/mint-mac-np",
                "../../server/mint/mint_mac_np/target/release/mint-mac-np",
                "server/mint/mint_mac_np/target/release/mint-mac-np"
        };

        for (String candidate : candidates) {
            File file = new File(candidate);
            if (file.isFile()) {
                return file.getAbsolutePath();
            }
        }

        return null;
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

            // 结束 C# 程序
            if (getMusicStatusProcess != null) {
                getMusicStatusProcess.destroy();
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
     * 定时任务，用于更新首选音乐平台的运行状态
     * 每隔 5 秒执行一次
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void checkPrimaryPlatform() {
        if (isMacMode()) {
            return;
        }

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

        startGetMusicStatus();
    }

    /**
     * 获取所有音频设备的列表
     * @return
     */
    public List<Device> getAudioDevices() {
        if (isMacMode()) {
            List<Device> devices = new ArrayList<>();
            devices.add(new Device("default", "macOS 默认音频设备"));
            return devices;
        }

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
        if (isMacMode()) {
            return new RespData<>("Fail! macOS 模式下不支持 DeviceDetect.exe");
        }

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
