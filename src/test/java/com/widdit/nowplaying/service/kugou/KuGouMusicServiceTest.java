package com.widdit.nowplaying.service.kugou;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class KuGouMusicServiceTest {
    @Test
    void acceptsInjectedHttpClient() {
        KuGouMusicService service = new KuGouMusicService(mock(KuGouHttpClient.class));
        assertNotNull(service);
    }
}
