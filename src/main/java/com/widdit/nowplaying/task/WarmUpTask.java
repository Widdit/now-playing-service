package com.widdit.nowplaying.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class WarmUpTask {

    private static final String WARM_UP_URL = "http://localhost:9863/api/system/warmUp";
    private static final int MAX_RETRY = 5;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void warmUpAsync() {
        // 构造请求头
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Api-Debug", "true");

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        for (int i = 1; i <= MAX_RETRY; i++) {
            try {
                restTemplate.exchange(WARM_UP_URL, HttpMethod.GET, requestEntity, Void.class);
                log.info("System warm-up completed");
                return;
            } catch (Exception e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
