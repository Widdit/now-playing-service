package com.widdit.nowplaying.service;

import com.widdit.nowplaying.util.ConsoleUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SystemService {

    public void enableRunAtStartup() {
        log.info("尝试启用开机自启");

        String exePath = "Assets\\EnableRunAtStartup.exe";
        try {
            ConsoleUtil.run(exePath);
            log.info("启用开机自启成功");
        } catch (Exception e) {
            log.error("运行 " + exePath + " 失败：" + e.getMessage());
        }
    }

    public void disableRunAtStartup() {
        log.info("尝试禁用开机自启");

        String exePath = "Assets\\DisableRunAtStartup.exe";
        try {
            ConsoleUtil.run(exePath);
            log.info("禁用开机自启成功");
        } catch (Exception e) {
            log.error("运行 " + exePath + " 失败：" + e.getMessage());
        }
    }

}
