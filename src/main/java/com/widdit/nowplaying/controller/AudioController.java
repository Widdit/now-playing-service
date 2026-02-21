package com.widdit.nowplaying.controller;

import com.widdit.nowplaying.annotation.ApiDebugLog;
import com.widdit.nowplaying.entity.Device;
import com.widdit.nowplaying.entity.RespData;
import com.widdit.nowplaying.service.AudioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class AudioController {

    @Autowired
    private AudioService audioService;

    /**
     * 获取音频设备列表
     * @return
     */
    @ApiDebugLog
    @GetMapping("/api/audio/devices")
    public List<Device> audioDevices() {
        return audioService.getAudioDevices();
    }

    /**
     * 获取音频设备识别结果
     * @param platform 音乐平台
     * @return
     */
    @GetMapping("/api/audio/deviceDetect")
    public RespData<String> deviceDetect(String platform) {
        return audioService.deviceDetect(platform);
    }

}
