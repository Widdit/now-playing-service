package com.widdit.nowplaying.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String root() {
        return "index";
    }

    @GetMapping("/index.html")
    public String index() {
        return "index";
    }

    @GetMapping("/widget")
    public String widget() {
        return "widget";
    }

    @GetMapping("/widget-widdit")
    public String widgetWiddit() {
        return "widget-widdit";
    }

    @GetMapping("/widget/*")
    public String widgetWithProfile() {
        return "widget";
    }

    @GetMapping("/widget-widdit/*")
    public String widgetWithProfileWiddit() {
        return "widget-widdit";
    }

    @GetMapping("/settings")
    public String settings() {
        return "settings-general";
    }

    @GetMapping("/settings/general")
    public String settingsGeneral() {
        return "settings-general";
    }

    @GetMapping("/settings/widget")
    public String settingsWidget() {
        return "settings-widget";
    }

    @GetMapping("/settings/desktop")
    public String settingsDesktop() {
        return "settings-desktop";
    }

    @GetMapping("/404")
    public String state404() {
        return "404";
    }

}
