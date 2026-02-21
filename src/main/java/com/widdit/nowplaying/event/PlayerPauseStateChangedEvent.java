package com.widdit.nowplaying.event;

import org.springframework.context.ApplicationEvent;

/**
 * 播放器暂停状态改变事件
 */
public class PlayerPauseStateChangedEvent extends ApplicationEvent {

    private String message;

    public PlayerPauseStateChangedEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
