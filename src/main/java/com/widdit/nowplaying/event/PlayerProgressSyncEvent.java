package com.widdit.nowplaying.event;

import org.springframework.context.ApplicationEvent;

/**
 * 播放器进度同步事件
 * 用于 kg 等需要外部进度源校准的平台，定期将 C# 端的实际进度同步到前端
 */
public class PlayerProgressSyncEvent extends ApplicationEvent {

    private String message;

    public PlayerProgressSyncEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
