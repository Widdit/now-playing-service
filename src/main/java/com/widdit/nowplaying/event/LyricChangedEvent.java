package com.widdit.nowplaying.event;

import org.springframework.context.ApplicationEvent;

/**
 * 歌词发生改变的事件
 */
public class LyricChangedEvent extends ApplicationEvent {

    private String message;

    public LyricChangedEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
