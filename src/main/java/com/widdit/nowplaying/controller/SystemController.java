package com.widdit.nowplaying.controller;

import com.widdit.nowplaying.service.SystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class SystemController {

    @Autowired
    private SystemService systemService;

    /**
     * 启用开机自启
     */
    @GetMapping("/api/system/enableRunAtStartup")
    public void enableRunAtStartup() {
        systemService.enableRunAtStartup();
    }

    /**
     * 禁用开机自启
     */
    @GetMapping("/api/system/disableRunAtStartup")
    public void disableRunAtStartup() {
        systemService.disableRunAtStartup();
    }

}
