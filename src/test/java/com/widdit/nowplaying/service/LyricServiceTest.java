package com.widdit.nowplaying.service;

import com.widdit.nowplaying.entity.Lyric;
import com.widdit.nowplaying.entity.SettingsLyricCommon;
import com.widdit.nowplaying.service.kugou.KuGouMusicService;
import com.widdit.nowplaying.service.netease.NeteaseMusicService;
import com.widdit.nowplaying.service.qq.QQMusicService;
import com.widdit.nowplaying.util.SongMatchingUtil;
import com.widdit.nowplaying.util.SongUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LyricServiceTest {

    @Test
    void fallbackSearchKeywordRemovesTranslatedTitleAnnotation() {
        assertEquals(
                "なりすましゲンガー - 鏡音リン",
                SongUtil.buildSearchKeywordWithoutAnnotations(
                        "なりすましゲンガー (乔装Gengar) - 鏡音リン"));
    }

    @Test
    void translatedCjkTitleMatchesKugouMultiArtistCandidate() {
        int similarity = SongMatchingUtil.calculateSimilarity(
                "希望有羽毛和翅膀 (Hope Is the Thing With Feathers)",
                "知更鸟",
                "希望有羽毛和翅膀",
                "知更鸟、HOYO-MiX、Chevy");

        assertEquals(96, similarity);
        assertTrue(similarity >= SongMatchingUtil.EXACT_MATCH_THRESHOLD);
    }

    @Test
    void japaneseArtistSeparatorAndMissingFeaturedSingerStillMatchLyricCandidate() {
        int similarity = SongMatchingUtil.calculateSimilarity(
                "目撃！テト31世 (Teto the 31st)(feat. 重音テト & 雨衣)",
                "はろける",
                "目撃！テト31世",
                "はろける、雨衣");

        assertEquals(88, similarity);
        assertTrue(similarity >= SongMatchingUtil.EXACT_MATCH_THRESHOLD);
    }

    @Test
    void japaneseAndSlashArtistSeparatorsAreEquivalent() {
        int similarity = SongMatchingUtil.calculateSimilarity(
                "芒种",
                "音阙诗听、赵方婧",
                "芒种",
                "音阙诗听 / 赵方婧");

        assertEquals(100, similarity);
    }

    @Test
    void latinTranslationInLocalCjkTitleMatchesProviderBaseTitle() {
        int similarity = SongMatchingUtil.calculateSimilarity(
                "アイドル (Idol)",
                "YOASOBI",
                "アイドル",
                "YOASOBI");

        assertEquals(96, similarity);
        assertTrue(similarity >= SongMatchingUtil.EXACT_MATCH_THRESHOLD);
    }

    @Test
    void localVersionMarkerIsNotTreatedAsTranslation() {
        int similarity = SongMatchingUtil.calculateSimilarity(
                "アイドル (Live)",
                "YOASOBI",
                "アイドル",
                "YOASOBI");

        assertTrue(similarity < SongMatchingUtil.EXACT_MATCH_THRESHOLD);
    }

    @Test
    void featuredArtistInTitleMatchesProviderArtistList() {
        int similarity = SongMatchingUtil.calculateSimilarity(
                "溯 (Reverse)(feat. 马吟吟)",
                "CORSAK胡梦周",
                "溯 (Reverse)",
                "CORSAK胡梦周 / 马吟吟");

        assertEquals(100, similarity);
        assertTrue(similarity >= SongMatchingUtil.EXACT_MATCH_THRESHOLD);
    }

    @Test
    void differentFeaturedArtistStillFailsExactMatch() {
        int similarity = SongMatchingUtil.calculateSimilarity(
                "溯 (Reverse)(feat. 马吟吟)",
                "CORSAK胡梦周",
                "溯 (Reverse)",
                "CORSAK胡梦周 / 其他歌手");

        assertTrue(similarity < SongMatchingUtil.EXACT_MATCH_THRESHOLD);
    }

    @Test
    void autoModeRequestsAllThreeSourcesForJayAndPrefersKugouWhenResultsTie() throws Exception {
        TestContext context = autoContext("晴天 - 周杰伦", "kugou");
        Lyric netease = lyric("netease", "晴天", "周杰伦", 240, true, true, true);
        Lyric qq = lyric("qq", "晴天", "周杰伦", 240, true, true, true);
        Lyric kugou = lyric("kugou", "晴天", "周杰伦", 240, true, true, true);
        when(context.netease.getLyric(context.windowTitle)).thenReturn(netease);
        when(context.qq.getLyric(context.windowTitle)).thenReturn(qq);
        when(context.kugou.getLyric(context.windowTitle)).thenReturn(kugou);

        Lyric result = context.service.getLyric();

        assertSame(kugou, result);
        verify(context.netease, times(1)).getLyric(context.windowTitle);
        verify(context.qq, times(1)).getLyric(context.windowTitle);
        verify(context.kugou, times(1)).getLyric(context.windowTitle);
    }

    @Test
    void autoModeIsolatesProviderExceptionsAndNullResults() throws Exception {
        TestContext context = autoContext("红豆 - 王菲", "netease");
        Lyric kugou = lyric("kugou", "红豆", "王菲", 240, true, false, false);
        when(context.netease.getLyric(context.windowTitle)).thenThrow(new IOException("offline"));
        when(context.qq.getLyric(context.windowTitle)).thenReturn(null);
        when(context.kugou.getLyric(context.windowTitle)).thenReturn(kugou);

        assertSame(kugou, context.service.getLyric());
    }

    @Test
    void completenessWinsWhenSimilaritiesAreClose() throws Exception {
        TestContext context = autoContext("红豆 - 王菲", "kugou");
        Lyric netease = lyric("netease", "红豆", "王菲", 240, true, false, false);
        Lyric qq = lyric("qq", "红豆", "王菲", 240, true, true, true);
        Lyric kugou = lyric("kugou", "红豆", "王菲", 240, true, false, false);
        stubAll(context, netease, qq, kugou);

        assertSame(qq, context.service.getLyric());
    }

    @Test
    void preferredSourceWinsACompletenessTie() throws Exception {
        TestContext context = autoContext("红豆 - 王菲", "qq");
        Lyric netease = lyric("netease", "红豆", "王菲", 240, true, true, false);
        Lyric qq = lyric("qq", "红豆", "王菲", 240, true, true, false);
        Lyric kugou = lyric("kugou", "红豆", "王菲", 240, true, false, false);
        stubAll(context, netease, qq, kugou);

        assertSame(qq, context.service.getLyric());
    }

    @ParameterizedTest
    @ValueSource(strings = {"unknown", "kugou"})
    void stableFallbackUsesNeteaseWhenPreferenceIsInvalidOrNotAmongTiedBest(String preferred) throws Exception {
        TestContext context = autoContext("红豆 - 王菲", preferred);
        Lyric netease = lyric("netease", "红豆", "王菲", 240, true, true, false);
        Lyric qq = lyric("qq", "红豆", "王菲", 240, true, true, false);
        Lyric kugou = lyric("kugou", "红豆", "王菲", 240, true, false, false);
        stubAll(context, netease, qq, kugou);

        assertSame(netease, context.service.getLyric());
    }

    @Test
    void nullPreferenceUsesStableNeteaseFallback() throws Exception {
        TestContext context = autoContext("红豆 - 王菲", null);
        Lyric netease = lyric("netease", "红豆", "王菲", 240, true, false, false);
        Lyric qq = lyric("qq", "红豆", "王菲", 240, true, false, false);
        Lyric kugou = lyric("kugou", "红豆", "王菲", 240, true, false, false);
        stubAll(context, netease, qq, kugou);

        assertSame(netease, context.service.getLyric());
    }

    @Test
    void clearlyHigherSimilarityWinsOverCompleteness() throws Exception {
        TestContext context = autoContext("红豆 - 王菲", "qq");
        Lyric netease = lyric("netease", "红豆", "王菲", 240, true, false, false);
        Lyric qq = lyric("qq", "完全不同", "其他歌手", 240, true, true, true);
        Lyric kugou = lyric("kugou", "另一首歌", "其他歌手", 240, true, true, true);
        stubAll(context, netease, qq, kugou);

        assertSame(netease, context.service.getLyric());
    }

    @Test
    void durationClosenessIncludesCandidateWithinEightSimilarityPoints() throws Exception {
        TestContext context = autoContext("Counting Stars - OneRepublic", "qq");
        Lyric netease = lyric("netease", "Counting Stars", "OneRepublic", 240, true, false, false);
        Lyric qq = lyric("qq", "Counting Stars (翻译)", "OneRepublic", 242, true, true, true);
        Lyric kugou = lyric("kugou", "Different Song", "Other", 400, true, true, true);
        stubAll(context, netease, qq, kugou);

        assertSame(qq, context.service.getLyric());
    }

    @Test
    void allFailuresReturnEmptyLyricWithNormalizedPreferredSource() throws Exception {
        TestContext context = autoContext("红豆 - 王菲", "unknown");
        when(context.netease.getLyric(context.windowTitle)).thenThrow(new IOException("offline"));
        when(context.qq.getLyric(context.windowTitle)).thenReturn(null);
        when(context.kugou.getLyric(context.windowTitle)).thenThrow(new IOException("offline"));

        Lyric result = context.service.getLyric();

        assertEquals("netease", result.getSource());
        assertEquals("红豆", result.getTitle());
        assertEquals("王菲", result.getAuthor());
        assertFalse(result.getHasLyric());
    }

    @Test
    void nullableProviderFieldsDoNotBreakSelection() throws Exception {
        TestContext context = autoContext("红豆 - 王菲", "qq");
        Lyric netease = lyric("netease", "红豆", "王菲", null, null, null, null);
        Lyric qq = lyric("qq", "红豆", "王菲", 1, true, false, false);
        Lyric kugou = lyric("kugou", "红豆", "王菲", Integer.MIN_VALUE, false, false, false);
        stubAll(context, netease, qq, kugou);

        Lyric result = assertDoesNotThrow(context.service::getLyric);
        assertSame(qq, result);
    }

    @Test
    void durationComparisonDoesNotOverflowAndAdmitACompleteLowerSimilarityCandidate() throws Exception {
        String title = "Counting Stars";
        String author = "OneRepublic";
        TestContext context = autoContext(title + " - " + author, "qq");
        Lyric netease = lyric("netease", title, author, 0, true, false, false);
        Lyric qq = lyric("qq", title + " (翻译) (译名) (别名)", author,
                Integer.MIN_VALUE, true, true, true);
        Lyric kugou = lyric("kugou", "Different Song", "Other", 0, true, true, true);
        stubAll(context, netease, qq, kugou);
        int similarityDiff = SongMatchingUtil.calculateSimilarity(title, author, title, author)
                - SongMatchingUtil.calculateSimilarity(title, author, qq.getTitle(), qq.getAuthor());

        assertTrue(similarityDiff >= 3 && similarityDiff <= 8,
                "fixture must exercise the duration-based closeness rule");
        assertSame(netease, context.service.getLyric());
    }

    @Test
    void autoModeTimesOutBlockedProviderAndReturnsAnotherUsefulResult() throws Exception {
        TestContext context = autoContext("红豆 - 王菲", "qq");
        ReflectionTestUtils.setField(context.service, "lyricFetchTimeoutMillis", 100L);
        CountDownLatch releaseBlockedProvider = new CountDownLatch(1);
        Lyric qq = lyric("qq", "红豆", "王菲", 240, true, true, false);
        Lyric kugou = lyric("kugou", "红豆", "王菲", 240, true, false, false);
        when(context.netease.getLyric(context.windowTitle)).thenAnswer(invocation -> {
            releaseBlockedProvider.await();
            return lyric("netease", "红豆", "王菲", 240, true, true, true);
        });
        when(context.qq.getLyric(context.windowTitle)).thenReturn(qq);
        when(context.kugou.getLyric(context.windowTitle)).thenReturn(kugou);

        try {
            Lyric result = assertTimeoutPreemptively(Duration.ofSeconds(2), context.service::getLyric);
            assertSame(qq, result);
            verify(context.netease).getLyric(context.windowTitle);
            verify(context.qq).getLyric(context.windowTitle);
            verify(context.kugou).getLyric(context.windowTitle);
        } finally {
            releaseBlockedProvider.countDown();
        }
    }

    @Test
    void contentEmptyExactMatchCannotBeatUsefulLyrics() throws Exception {
        TestContext context = autoContext("Counting Stars - OneRepublic", "kugou");
        Lyric netease = lyric("netease", "Counting Stars (翻译)", "OneRepublic", 240, true, false, false);
        Lyric qq = lyric("qq", "Different Song", "Other", 240, true, true, true);
        Lyric kugou = lyric("kugou", "Counting Stars", "OneRepublic", 240, false, false, false);
        stubAll(context, netease, qq, kugou);

        assertSame(netease, context.service.getLyric());
    }

    @Test
    void allContentEmptyCandidatesReturnNormalizedParsedEmptyLyric() throws Exception {
        TestContext context = autoContext("红豆 - 王菲", "unknown");
        stubAll(context,
                lyric("netease", "Wrong", "Wrong", 240, false, false, false),
                lyric("qq", "红豆", "王菲", 240, false, false, false),
                lyric("kugou", "红豆", "王菲", 240, false, false, false));

        Lyric result = context.service.getLyric();

        assertEquals("netease", result.getSource());
        assertEquals("红豆", result.getTitle());
        assertEquals("王菲", result.getAuthor());
        assertFalse(result.getHasLyric());
    }

    @Test
    void invalidDurationDoesNotMakeLowerSimilarityCandidateClose() throws Exception {
        String title = "Counting Stars";
        String author = "OneRepublic";
        TestContext context = autoContext(title + " - " + author, "qq");
        Lyric netease = lyric("netease", title, author, 0, true, false, false);
        Lyric qq = lyric("qq", title + " (翻译) (译名) (别名)", author, null, true, true, true);
        Lyric kugou = lyric("kugou", "Different Song", "Other", 240, true, true, true);
        stubAll(context, netease, qq, kugou);
        int similarityDiff = SongMatchingUtil.calculateSimilarity(title, author, title, author)
                - SongMatchingUtil.calculateSimilarity(title, author, qq.getTitle(), qq.getAuthor());

        assertTrue(similarityDiff >= 3 && similarityDiff <= 8,
                "fixture must exercise the duration-based closeness rule");
        assertSame(netease, context.service.getLyric());
    }

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
    void nullExplicitSourceFallsBackToNeteaseForNonJaySong() throws Exception {
        AudioService audioService = mock(AudioService.class);
        NeteaseMusicService neteaseMusicService = mock(NeteaseMusicService.class);
        QQMusicService qqMusicService = mock(QQMusicService.class);
        KuGouMusicService kuGouMusicService = mock(KuGouMusicService.class);
        Lyric neteaseLyric = Lyric.builder().source("netease").title("红豆").author("王菲").build();
        when(audioService.getWindowTitle()).thenReturn("红豆 - 王菲");
        when(audioService.getStatus()).thenReturn("Playing");
        when(neteaseMusicService.getLyric("红豆 - 王菲")).thenReturn(neteaseLyric);

        LyricService service = createService(
                audioService,
                neteaseMusicService,
                qqMusicService,
                kuGouMusicService,
                new SettingsLyricCommon(null, false));

        Lyric result = service.getLyric();

        assertSame(neteaseLyric, result);
        verify(neteaseMusicService).getLyric("红豆 - 王菲");
        verify(qqMusicService, never()).getLyric("红豆 - 王菲");
        verify(kuGouMusicService, never()).getLyric("红豆 - 王菲");
    }

    @Test
    void explicitQqSourceUsesQqLyricService() throws Exception {
        AudioService audioService = mock(AudioService.class);
        NeteaseMusicService neteaseMusicService = mock(NeteaseMusicService.class);
        QQMusicService qqMusicService = mock(QQMusicService.class);
        KuGouMusicService kuGouMusicService = mock(KuGouMusicService.class);
        Lyric qqLyric = Lyric.builder().source("qq").title("红豆").author("王菲").build();
        when(audioService.getWindowTitle()).thenReturn("红豆 - 王菲");
        when(audioService.getStatus()).thenReturn("Playing");
        when(qqMusicService.getLyric("红豆 - 王菲")).thenReturn(qqLyric);

        LyricService service = createService(
                audioService,
                neteaseMusicService,
                qqMusicService,
                kuGouMusicService,
                new SettingsLyricCommon("qq", false));

        Lyric result = service.getLyric();

        assertSame(qqLyric, result);
        verify(qqMusicService).getLyric("红豆 - 王菲");
        verify(neteaseMusicService, never()).getLyric("红豆 - 王菲");
        verify(kuGouMusicService, never()).getLyric("红豆 - 王菲");
    }

    @Test
    void explicitNeteaseSourceUsesNeteaseLyricServiceForOrdinarySong() throws Exception {
        AudioService audioService = mock(AudioService.class);
        NeteaseMusicService neteaseMusicService = mock(NeteaseMusicService.class);
        QQMusicService qqMusicService = mock(QQMusicService.class);
        KuGouMusicService kuGouMusicService = mock(KuGouMusicService.class);
        Lyric neteaseLyric = Lyric.builder().source("netease").title("红豆").author("王菲").build();
        when(audioService.getWindowTitle()).thenReturn("红豆 - 王菲");
        when(audioService.getStatus()).thenReturn("Playing");
        when(neteaseMusicService.getLyric("红豆 - 王菲")).thenReturn(neteaseLyric);

        LyricService service = createService(
                audioService,
                neteaseMusicService,
                qqMusicService,
                kuGouMusicService,
                new SettingsLyricCommon("netease", false));

        Lyric result = service.getLyric();

        assertSame(neteaseLyric, result);
        verify(neteaseMusicService).getLyric("红豆 - 王菲");
        verify(qqMusicService, never()).getLyric("红豆 - 王菲");
        verify(kuGouMusicService, never()).getLyric("红豆 - 王菲");
    }

    @ParameterizedTest
    @ValueSource(strings = {"周杰伦", "周杰倫"})
    void explicitNeteaseSourceKeepsQqFallbackForJaySong(String jayName) throws Exception {
        AudioService audioService = mock(AudioService.class);
        NeteaseMusicService neteaseMusicService = mock(NeteaseMusicService.class);
        QQMusicService qqMusicService = mock(QQMusicService.class);
        KuGouMusicService kuGouMusicService = mock(KuGouMusicService.class);
        String windowTitle = "晴天 - " + jayName;
        Lyric qqLyric = Lyric.builder().source("qq").title("晴天").author(jayName).build();
        when(audioService.getWindowTitle()).thenReturn(windowTitle);
        when(audioService.getStatus()).thenReturn("Playing");
        when(qqMusicService.getLyric(windowTitle)).thenReturn(qqLyric);

        LyricService service = createService(
                audioService,
                neteaseMusicService,
                qqMusicService,
                kuGouMusicService,
                new SettingsLyricCommon("netease", false));

        Lyric result = service.getLyric();

        assertSame(qqLyric, result);
        verify(qqMusicService).getLyric(windowTitle);
        verify(neteaseMusicService, never()).getLyric(windowTitle);
        verify(kuGouMusicService, never()).getLyric(windowTitle);
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
        assertFalse(result.getHasLyric());
        assertFalse(result.getHasTranslatedLyric());
        assertFalse(result.getHasKaraokeLyric());
        assertEquals("", result.getLrc());
        assertEquals("", result.getTranslatedLyric());
        assertEquals("", result.getKaraokeLyric());
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

    private static TestContext autoContext(String windowTitle, String preferred) {
        AudioService audio = mock(AudioService.class);
        NeteaseMusicService netease = mock(NeteaseMusicService.class);
        QQMusicService qq = mock(QQMusicService.class);
        KuGouMusicService kugou = mock(KuGouMusicService.class);
        when(audio.getWindowTitle()).thenReturn(windowTitle);
        when(audio.getStatus()).thenReturn("Playing");
        LyricService service = createService(audio, netease, qq, kugou,
                new SettingsLyricCommon(preferred, true));
        return new TestContext(windowTitle, service, netease, qq, kugou);
    }

    private static void stubAll(TestContext context, Lyric netease, Lyric qq, Lyric kugou) throws Exception {
        when(context.netease.getLyric(context.windowTitle)).thenReturn(netease);
        when(context.qq.getLyric(context.windowTitle)).thenReturn(qq);
        when(context.kugou.getLyric(context.windowTitle)).thenReturn(kugou);
    }

    private static Lyric lyric(String source, String title, String author, Integer duration,
                               Boolean hasLyric, Boolean hasTranslated, Boolean hasKaraoke) {
        return Lyric.builder()
                .source(source)
                .title(title)
                .author(author)
                .duration(duration)
                .hasLyric(hasLyric)
                .hasTranslatedLyric(hasTranslated)
                .hasKaraokeLyric(hasKaraoke)
                .build();
    }

    private static final class TestContext {
        private final String windowTitle;
        private final LyricService service;
        private final NeteaseMusicService netease;
        private final QQMusicService qq;
        private final KuGouMusicService kugou;

        private TestContext(String windowTitle, LyricService service, NeteaseMusicService netease,
                            QQMusicService qq, KuGouMusicService kugou) {
            this.windowTitle = windowTitle;
            this.service = service;
            this.netease = netease;
            this.qq = qq;
            this.kugou = kugou;
        }
    }
}
