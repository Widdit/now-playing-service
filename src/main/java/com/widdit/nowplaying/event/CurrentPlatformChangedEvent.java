package com.widdit.nowplaying.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CurrentPlatformChangedEvent extends ApplicationEvent {

    private final String oldPlatform;
    private final String newPlatform;

    public CurrentPlatformChangedEvent(Object source, String oldPlatform, String newPlatform) {
        super(source);
        this.oldPlatform = oldPlatform;
        this.newPlatform = newPlatform;
    }
}