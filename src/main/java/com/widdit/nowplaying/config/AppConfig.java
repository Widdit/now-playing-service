package com.widdit.nowplaying.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    // 用在 VersionController 里
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
