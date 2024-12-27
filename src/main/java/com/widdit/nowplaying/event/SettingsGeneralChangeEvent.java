package com.widdit.nowplaying.event;

import org.springframework.context.ApplicationEvent;

/**
 * 通用设置被修改的事件
 */
public class SettingsGeneralChangeEvent extends ApplicationEvent {

    private String message;

    public SettingsGeneralChangeEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
