package com.widdit.nowplaying.event;

import org.springframework.context.ApplicationEvent;

/**
 * 音乐状态被更新的事件
 */
public class MusicStatusUpdatedEvent extends ApplicationEvent {

    private String message;

    public MusicStatusUpdatedEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
