package com.widdit.nowplaying.controller;

import com.widdit.nowplaying.entity.Device;
import com.widdit.nowplaying.entity.SettingsGeneral;
import com.widdit.nowplaying.service.AudioService;
import com.widdit.nowplaying.service.SettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class SettingsController {

    @Autowired
    private SettingsService settingsService;
    @Autowired
    private AudioService audioService;

    /**
     * 获取通用设置
     * @return
     */
    @GetMapping("/api/settings/general")
    public SettingsGeneral settingsGeneral() {
        return settingsService.getSettingsGeneral();
    }

    /**
     * 获取音频设备列表
     * @return
     */
    @GetMapping("/api/audio/devices")
    public List<Device> audioDevices() {
        return audioService.getAudioDevices();
    }

    /**
     * 更新通用设置
     * @param settings
     */
    @PutMapping("/api/settings/general")
    public void settingsUpdate(@RequestBody SettingsGeneral settings) {
        settingsService.updateSettingsGeneral(settings);
    }

}
