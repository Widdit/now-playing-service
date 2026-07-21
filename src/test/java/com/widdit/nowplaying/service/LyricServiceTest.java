package com.widdit.nowplaying.service;

import com.widdit.nowplaying.entity.Lyric;
import com.widdit.nowplaying.entity.SettingsLyricCommon;
import com.widdit.nowplaying.service.kugou.KuGouMusicService;
import com.widdit.nowplaying.service.netease.NeteaseMusicService;
import com.widdit.nowplaying.service.qq.QQMusicService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LyricServiceTest {

    @Test
    void explicitKugouSourceUsesKugouLyricService() throws Exception {
        AudioService audioService = mock(AudioService.class);
        NeteaseMusicService neteaseMusicService = mock(NeteaseMusicService.class);
        QQMusicService qqMusicService = mock(QQMusicService.class);
        KuGouMusicService kuGouMusicService = mock(KuGouMusicService.class);
        Lyric kugouLyric = Lyric.builder().source("kugou").title("晴天").author("周杰伦").build();
        when(audioService.getWindowTitle()).thenReturn("晴天 - 周杰伦");
        when(audioService.getStatus()).thenReturn("Playing");
        when(kuGouMusicService.getLyric("晴天 - 周杰伦")).thenReturn(kugouLyric);

        LyricService service = createService(
                audioService,
                neteaseMusicService,
                qqMusicService,
                kuGouMusicService,
                new SettingsLyricCommon("kugou", false));

        Lyric result = service.getLyric();

        assertSame(kugouLyric, result);
        verify(kuGouMusicService).getLyric("晴天 - 周杰伦");
        verify(neteaseMusicService, never()).getLyric("晴天 - 周杰伦");
        verify(qqMusicService, never()).getLyric("晴天 - 周杰伦");
    }

    @Test
    void unknownExplicitSourceFallsBackToNeteaseForNonJaySong() throws Exception {
        AudioService audioService = mock(AudioService.class);
        NeteaseMusicService neteaseMusicService = mock(NeteaseMusicService.class);
        QQMusicService qqMusicService = mock(QQMusicService.class);
        KuGouMusicService kuGouMusicService = mock(KuGouMusicService.class);
        Lyric neteaseLyric = Lyric.builder().source("netease").title("夜曲").author("周杰伦").build();
        when(audioService.getWindowTitle()).thenReturn("夜曲 - 其他歌手");
        when(audioService.getStatus()).thenReturn("Playing");
        when(neteaseMusicService.getLyric("夜曲 - 其他歌手")).thenReturn(neteaseLyric);

        LyricService service = createService(
                audioService,
                neteaseMusicService,
                qqMusicService,
                kuGouMusicService,
                new SettingsLyricCommon("broken-source", false));

        Lyric result = service.getLyric();

        assertSame(neteaseLyric, result);
        verify(neteaseMusicService).getLyric("夜曲 - 其他歌手");
        verify(qqMusicService, never()).getLyric("夜曲 - 其他歌手");
        verify(kuGouMusicService, never()).getLyric("夜曲 - 其他歌手");
    }

    @Test
    void explicitNeteaseSourceKeepsQqFallbackForJaySong() throws Exception {
        AudioService audioService = mock(AudioService.class);
        NeteaseMusicService neteaseMusicService = mock(NeteaseMusicService.class);
        QQMusicService qqMusicService = mock(QQMusicService.class);
        KuGouMusicService kuGouMusicService = mock(KuGouMusicService.class);
        Lyric qqLyric = Lyric.builder().source("qq").title("晴天").author("周杰伦").build();
        when(audioService.getWindowTitle()).thenReturn("晴天 - 周杰伦");
        when(audioService.getStatus()).thenReturn("Playing");
        when(qqMusicService.getLyric("晴天 - 周杰伦")).thenReturn(qqLyric);

        LyricService service = createService(
                audioService,
                neteaseMusicService,
                qqMusicService,
                kuGouMusicService,
                new SettingsLyricCommon("netease", false));

        Lyric result = service.getLyric();

        assertSame(qqLyric, result);
        verify(qqMusicService).getLyric("晴天 - 周杰伦");
        verify(neteaseMusicService, never()).getLyric("晴天 - 周杰伦");
        verify(kuGouMusicService, never()).getLyric("晴天 - 周杰伦");
    }

    @Test
    void explicitKugouFailureReturnsEmptyLyricWithParsedMetadata() throws Exception {
        AudioService audioService = mock(AudioService.class);
        NeteaseMusicService neteaseMusicService = mock(NeteaseMusicService.class);
        QQMusicService qqMusicService = mock(QQMusicService.class);
        KuGouMusicService kuGouMusicService = mock(KuGouMusicService.class);
        when(audioService.getWindowTitle()).thenReturn("晴天 - 周杰伦");
        when(audioService.getStatus()).thenReturn("Playing");
        when(kuGouMusicService.getLyric("晴天 - 周杰伦"))
                .thenThrow(new IOException("offline"));

        LyricService service = createService(
                audioService,
                neteaseMusicService,
                qqMusicService,
                kuGouMusicService,
                new SettingsLyricCommon("kugou", false));

        Lyric result = service.getLyric();

        assertEquals("kugou", result.getSource());
        assertEquals("晴天", result.getTitle());
        assertEquals("周杰伦", result.getAuthor());
        verify(kuGouMusicService).getLyric("晴天 - 周杰伦");
    }

    private static LyricService createService(
            AudioService audioService,
            NeteaseMusicService neteaseMusicService,
            QQMusicService qqMusicService,
            KuGouMusicService kuGouMusicService,
            SettingsLyricCommon settingsCommon) {
        LyricService service = new LyricService();
        ReflectionTestUtils.setField(service, "audioService", audioService);
        ReflectionTestUtils.setField(service, "neteaseMusicService", neteaseMusicService);
        ReflectionTestUtils.setField(service, "qqMusicService", qqMusicService);
        ReflectionTestUtils.setField(service, "kuGouMusicService", kuGouMusicService);
        ReflectionTestUtils.setField(service, "settingsCommon", settingsCommon);
        return service;
    }
}
