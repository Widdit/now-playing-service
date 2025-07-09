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

    @GetMapping("/widget/*")
    public String widgetWithProfile() {
        return "widget";
    }

    @GetMapping("/widget-widdit")
    public String widgetWiddit() {
        return "widget-widdit";
    }

    @GetMapping("/widget-widdit/*")
    public String widgetWithProfileWiddit() {
        return "widget-widdit";
    }

    @GetMapping("/widget-green")
    public String widgetGreen() {
        return "widget-green";
    }

    @GetMapping("/widget-green/*")
    public String widgetWithProfileGreen() {
        return "widget-green";
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

    @GetMapping("/settings/output")
    public String settingsOutput() {
        return "settings-output";
    }

    @GetMapping("/404")
    public String state404() {
        return "404";
    }

    @GetMapping("/lyric")
    public String lyric() {
        return "lyric";
    }

    @GetMapping("/lyric/1")
    public String lyric1() {
        return "lyric-1";
    }

}
