package com.widdit.nowplaying.event;

import org.springframework.context.ApplicationEvent;

/**
 * 通用设置被修改的事件
 */
public class SettingsGeneralChangedEvent extends ApplicationEvent {

    private String message;

    public SettingsGeneralChangedEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
