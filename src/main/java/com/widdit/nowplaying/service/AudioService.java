package com.widdit.nowplaying.service;

import com.widdit.nowplaying.entity.Device;
import com.widdit.nowplaying.entity.SettingsGeneral;
import com.widdit.nowplaying.entity.cmd.Args;
import com.widdit.nowplaying.entity.cmd.Option;
import com.widdit.nowplaying.util.ConsoleUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AudioService {

    @Autowired
    private SettingsService settingsService;

    /**
     * 检测音乐软件的播放状态（Playing, Paused, None）、音乐平台（Netease, QQ, KuGou, KuWo）、窗口标题
     * 输出格式：
     * "
     *     播放状态 音乐平台
     *     窗口标题
     * "
     * 接收命令行参数：
     *     --device-id  音频设备 ID。仅检测该音频设备，默认值为 "default"，检测默认音频设备。
     *     --platform  音乐平台。期望检测的音乐软件平台，默认值为 "netease"，检测网易云音乐。
     */
    public String getMusicStatus() {
        // 封装命令行参数
        SettingsGeneral settingsGeneral = settingsService.getSettingsGeneral();
        String deviceId = settingsGeneral.getDeviceId();
        String platform = settingsGeneral.getPlatform();

        List<Option> options = new ArrayList<>();
        options.add(new Option("--device-id", deviceId));
        options.add(new Option("--platform", platform));

        Args args = new Args(options);

        // 执行命令
        String stdOut = "";
        try {
            stdOut = ConsoleUtil.runGetStdOut("Assets\\AudioService\\GetMusicStatus.exe", args);
        } catch (Exception e) {
            log.error("获取音乐软件的播放状态失败：" + e.getMessage());
        }
        return stdOut;
    }

    /**
     * 获取所有音频设备的列表
     * @return
     */
    public List<Device> getAudioDevices() {
        // 执行命令
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

}
