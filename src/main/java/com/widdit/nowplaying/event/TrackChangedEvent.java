package com.widdit.nowplaying.event;

import org.springframework.context.ApplicationEvent;

/**
 * 歌曲发生改变的事件
 */
public class TrackChangedEvent extends ApplicationEvent {

    private String message;

    public TrackChangedEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
