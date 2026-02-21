package com.widdit.nowplaying.event;

import org.springframework.context.ApplicationEvent;

/**
 * 播放器重新播放事件（一般发生在单曲循环的情况下）
 * 注：该事件仅针对同一首歌重新播放的情况，不包含切歌情况
 */
public class PlayerProgressReplayEvent extends ApplicationEvent {

    private String message;

    public PlayerProgressReplayEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
