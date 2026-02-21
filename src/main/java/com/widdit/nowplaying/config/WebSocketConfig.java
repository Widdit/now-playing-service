package com.widdit.nowplaying.config;

import com.widdit.nowplaying.controller.WebSocketLyricController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.annotation.PostConstruct;

/**
 * WebSocket 配置类
 */
@Configuration
public class WebSocketConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @PostConstruct
    public void init() {
        WebSocketLyricController.setApplicationContext(applicationContext);
    }

}
