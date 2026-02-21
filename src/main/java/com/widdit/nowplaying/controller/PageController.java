package com.widdit.nowplaying.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/widget")
    public String widget() {
        return "widget";
    }

    @GetMapping("/widget/*")
    public String widgetWithProfile() {
        return "widget";
    }

    @GetMapping("/widget-widdit")
    public String widgetWiddit() {
        return "widget-widdit";
    }

    @GetMapping("/widget-widdit/*")
    public String widgetWidditWithProfile() {
        return "widget-widdit";
    }

    @GetMapping("/settings/widget")
    public String settingsWidget() {
        return "settings-widget";
    }

    /**
     * 捕获所有其它路由
     * 排除 /api、/assets、/vite-assets、/public
     */
    @GetMapping({
            "/",
            "/{path:^(?!api|assets|vite-assets|public).*}/**"
    })
    public String index() {
        return "index";
    }

}
