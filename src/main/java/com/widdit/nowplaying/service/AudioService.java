package com.widdit.nowplaying.service;

import com.widdit.nowplaying.entity.Device;
import com.widdit.nowplaying.entity.SettingsGeneral;
import com.widdit.nowplaying.entity.cmd.Args;
import com.widdit.nowplaying.entity.cmd.Option;
import com.widdit.nowplaying.event.SettingsGeneralChangeEvent;
import com.widdit.nowplaying.util.ConsoleUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AudioService {

    // 当前播放状态（Playing, Paused, None）
    private String status = "None";
    // 当前窗口标题
    private String windowTitle = "";

    private Process getMusicStatusProcess;
    private Thread musicStatusReaderThread;

    @Autowired
    private SettingsService settingsService;

    /**
     * 初始化操作。该方法会在该类被 Spring 创建时自动执行
     */
    @PostConstruct
    public void init() {
        startGetMusicStatus();
    }

    /**
     * 启动 C# 程序 GetMusicStatus.exe 不断更新音乐状态（成员变量 status 和 windowTitle）
     */
    public void startGetMusicStatus() {
        // 封装命令行参数
        SettingsGeneral settingsGeneral = settingsService.getSettingsGeneral();
        String deviceId = settingsGeneral.getDeviceId();
        String platform = settingsGeneral.getPlatform();

        List<Option> options = new ArrayList<>();
        options.add(new Option("--device-id", deviceId));
        options.add(new Option("--platform", platform));
        Args args = new Args(options);

        List<String> command = ConsoleUtil.getCommand("Assets\\AudioService\\GetMusicStatus.exe", args);

        // 运行并处理 C# 程序输出
        try {
            // 启动 C# 程序
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            getMusicStatusProcess = processBuilder.start();

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
                    }
                } catch (Exception e) {
                    log.error("启用线程读取 C# 程序 GetMusicStatus.exe 失败：" + e.getMessage());
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
            if (getMusicStatusProcess != null) {
                getMusicStatusProcess.destroy();  // 结束 C# 程序
            }

            if (musicStatusReaderThread != null) {
                musicStatusReaderThread.interrupt();  // 停止读取线程
            }
        } catch (Exception e) {
            log.error("终止 C# 程序 GetMusicStatus.exe 失败：" + e.getMessage());
        }
    }

    /**
     * 监听通用设置被修改的事件
     * @param event
     */
    @EventListener
    public void handleSettingsGeneralChange(SettingsGeneralChangeEvent event) {
        // 如果通用设置被修改，则重启 GetMusicStatus 进程
        stopGetMusicStatus();
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

    public String getStatus() {
        return status;
    }

    public String getWindowTitle() {
        return windowTitle;
    }

}
