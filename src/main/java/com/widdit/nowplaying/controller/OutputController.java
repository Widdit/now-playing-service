package com.widdit.nowplaying.controller;

import com.widdit.nowplaying.entity.SettingsOutput;
import com.widdit.nowplaying.service.OutputService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class OutputController {

    @Autowired
    private OutputService outputService;

    /**
     * 获取输出设置
     * @return
     */
    @GetMapping("/api/settings/output")
    public SettingsOutput settingsOutput() {
        return outputService.getSettingsOutput();
    }

    /**
     * 更新输出设置
     * @param settings
     */
    @PutMapping("/api/settings/output")
    public void settingsOutputUpdate(@RequestBody SettingsOutput settings) {
        outputService.updateSettingsOutput(settings);
    }

    /**
     * 恢复默认输出设置
     */
    @GetMapping("/api/settings/output/reset")
    public void settingsOutputReset() {
        outputService.resetSettingsOutput();
    }

}
