package com.widdit.nowplaying.service.kugou;

import com.widdit.nowplaying.entity.Lyric;
import com.widdit.nowplaying.entity.Track;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KuGouMusicServiceTest {
    private static final String LRC = "[00:00.00]晴天 - 周杰伦\n[00:10.20]故事的小黄花";
    private static final String LYRIC_SEARCH_RESPONSE = "{"
            + "\"status\":200,"
            + "\"candidates\":[{"
            + "\"id\":\"wrong\","
            + "\"accesskey\":\"WRONG_ACCESS_KEY\","
            + "\"singer\":\"其他歌手\","
            + "\"song\":\"晴天\","
            + "\"duration\":269000"
            + "},{"
            + "\"id\":\"right\","
            + "\"accesskey\":\"RIGHT_ACCESS_KEY\","
            + "\"singer\":\"周杰伦\","
            + "\"song\":\"晴天\","
            + "\"duration\":269792"
            + "}]}";
    private static final String LYRIC_DOWNLOAD_RESPONSE = "{"
            + "\"status\":200,"
            + "\"content\":\""
            + Base64.getEncoder().encodeToString(LRC.getBytes(StandardCharsets.UTF_8))
            + "\"}";

    @Test
    void searchSelectsStudioTrackInsteadOfFirstLiveResult() throws IOException {
        KuGouHttpClient client = mock(KuGouHttpClient.class);
        when(client.get(anyString())).thenReturn(searchResponse());
        KuGouMusicService service = new KuGouMusicService(client);

        Track track = service.search("晴天 - 周杰伦");

        assertEquals("studio", track.getId());
        assertEquals("晴天", track.getTitle());
        assertEquals(269, track.getDuration());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(urlCaptor.capture());
        String requestedUrl = urlCaptor.getValue();
        assertTrue(requestedUrl.startsWith("http://songsearch.kugou.com/song_search_v2?"));
        assertTrue(requestedUrl.contains(
                "keyword=%E6%99%B4%E5%A4%A9%20-%20%E5%91%A8%E6%9D%B0%E4%BC%A6"));
    }

    @Test
    void searchSongRetainsFileHashForSelectedStudioTrack() throws IOException {
        KuGouHttpClient client = mock(KuGouHttpClient.class);
        when(client.get(anyString())).thenReturn(searchResponse());
        KuGouMusicService service = new KuGouMusicService(client);

        KuGouSong song = service.searchSong("晴天 - 周杰伦");

        assertEquals("STUDIO_HASH", song.getFileHash());
        assertEquals("studio", song.getTrack().getId());
    }

    @Test
    void getLyricDownloadsBestMatchingStandardLrc() throws IOException {
        KuGouHttpClient client = mock(KuGouHttpClient.class);
        when(client.get(contains("song_search_v2"))).thenReturn(searchResponse());
        when(client.get(contains("lyrics.kugou.com/search"))).thenReturn(LYRIC_SEARCH_RESPONSE);
        when(client.get(contains("lyrics.kugou.com/download"))).thenReturn(LYRIC_DOWNLOAD_RESPONSE);
        KuGouMusicService service = new KuGouMusicService(client);

        Lyric lyric = service.getLyric("晴天 - 周杰伦");

        assertEquals("kugou", lyric.getSource());
        assertEquals("晴天", lyric.getTitle());
        assertEquals("周杰伦", lyric.getAuthor());
        assertEquals(269, lyric.getDuration());
        assertTrue(lyric.getHasLyric());
        assertEquals(LRC, lyric.getLrc());
        assertFalse(lyric.getHasKaraokeLyric());
        assertFalse(lyric.getHasTranslatedLyric());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(client, org.mockito.Mockito.times(3)).get(urlCaptor.capture());
        String lyricSearchUrl = urlCaptor.getAllValues().get(1);
        String downloadUrl = urlCaptor.getAllValues().get(2);
        assertTrue(lyricSearchUrl.startsWith("https://lyrics.kugou.com/search?"));
        assertTrue(lyricSearchUrl.contains("ver=1"));
        assertTrue(lyricSearchUrl.contains("man=yes"));
        assertTrue(lyricSearchUrl.contains("client=pc"));
        assertTrue(lyricSearchUrl.contains("keyword=%E6%99%B4%E5%A4%A9%20-%20%E5%91%A8%E6%9D%B0%E4%BC%A6"));
        assertTrue(lyricSearchUrl.contains("duration=269000"));
        assertTrue(lyricSearchUrl.contains("hash=STUDIO_HASH"));
        assertTrue(downloadUrl.startsWith("https://lyrics.kugou.com/download?"));
        assertTrue(downloadUrl.contains("ver=1"));
        assertTrue(downloadUrl.contains("client=pc"));
        assertTrue(downloadUrl.contains("id=right"));
        assertTrue(downloadUrl.contains("accesskey=RIGHT_ACCESS_KEY"));
        assertTrue(downloadUrl.contains("fmt=lrc"));
        assertTrue(downloadUrl.contains("charset=utf8"));
    }

    @Test
    void getLyricBreaksEqualSimilarityByClosestDuration() throws IOException {
        String equalSimilarityCandidates = "{"
                + "\"status\":200,"
                + "\"candidates\":[{"
                + "\"id\":\"far\","
                + "\"accesskey\":\"FAR_KEY\","
                + "\"singer\":\"周杰伦\","
                + "\"song\":\"晴天\","
                + "\"duration\":240000"
                + "},{"
                + "\"id\":\"near\","
                + "\"accesskey\":\"NEAR_KEY\","
                + "\"singer\":\"周杰伦\","
                + "\"song\":\"晴天\","
                + "\"duration\":269500"
                + "}]}";
        KuGouHttpClient client = lyricClientWithSearchResponse(equalSimilarityCandidates);
        when(client.get(contains("lyrics.kugou.com/download"))).thenReturn(LYRIC_DOWNLOAD_RESPONSE);

        Lyric lyric = new KuGouMusicService(client).getLyric("晴天 - 周杰伦");

        assertTrue(lyric.getHasLyric());
        verify(client).get(contains("id=near"));
        verify(client, never()).get(contains("id=far"));
    }

    @Test
    void getLyricUsesLowerThresholdWhenAuthorIsMissing() throws IOException {
        String candidate = "{"
                + "\"status\":200,"
                + "\"candidates\":[{"
                + "\"id\":\"right\","
                + "\"accesskey\":\"RIGHT_ACCESS_KEY\","
                + "\"singer\":\"周杰伦\","
                + "\"song\":\"晴天\","
                + "\"duration\":269792"
                + "}]}";
        KuGouHttpClient client = lyricClientWithSearchResponse(candidate);
        when(client.get(contains("lyrics.kugou.com/download"))).thenReturn(LYRIC_DOWNLOAD_RESPONSE);

        Lyric lyric = new KuGouMusicService(client).getLyric("晴天");

        assertTrue(lyric.getHasLyric());
        assertEquals(LRC, lyric.getLrc());
    }

    @Test
    void getLyricAcceptsWholeSecondTimedLrcTag() throws IOException {
        String wholeSecondLrc = "[00:00]晴天 - 周杰伦";
        String encoded = Base64.getEncoder().encodeToString(
                wholeSecondLrc.getBytes(StandardCharsets.UTF_8));
        KuGouHttpClient client = lyricClientWithSearchResponse(LYRIC_SEARCH_RESPONSE);
        when(client.get(contains("lyrics.kugou.com/download"))).thenReturn(
                "{\"status\":200,\"content\":\"" + encoded + "\"}");

        Lyric lyric = new KuGouMusicService(client).getLyric("晴天 - 周杰伦");

        assertTrue(lyric.getHasLyric());
        assertEquals(wholeSecondLrc, lyric.getLrc());
    }

    @Test
    void getLyricAcceptsOneDigitMinuteAndOneFractionDigit() throws IOException {
        String lrc = "[0:05.5]单字";
        String encoded = Base64.getEncoder().encodeToString(lrc.getBytes(StandardCharsets.UTF_8));
        KuGouHttpClient client = lyricClientWithSearchResponse(LYRIC_SEARCH_RESPONSE);
        when(client.get(contains("lyrics.kugou.com/download"))).thenReturn(
                "{\"status\":200,\"content\":\"" + encoded + "\"}");

        Lyric lyric = new KuGouMusicService(client).getLyric("晴天 - 周杰伦");

        assertTrue(lyric.getHasLyric());
        assertEquals(lrc, lyric.getLrc());
    }

    @Test
    void getLyricAcceptsThreeFractionDigitTimedTag() throws IOException {
        String lrc = "[00:05.123]三位小数";
        String encoded = Base64.getEncoder().encodeToString(lrc.getBytes(StandardCharsets.UTF_8));
        KuGouHttpClient client = lyricClientWithSearchResponse(LYRIC_SEARCH_RESPONSE);
        when(client.get(contains("lyrics.kugou.com/download"))).thenReturn(
                "{\"status\":200,\"content\":\"" + encoded + "\"}");

        Lyric lyric = new KuGouMusicService(client).getLyric("晴天 - 周杰伦");

        assertTrue(lyric.getHasLyric());
        assertEquals(lrc, lyric.getLrc());
    }

    @Test
    void getLyricReturnsEmptyWhenCandidatesAreMissing() throws IOException {
        KuGouHttpClient client = lyricClientWithSearchResponse(
                "{\"status\":200,\"candidates\":[]}");

        Lyric lyric = assertDoesNotThrow(
                () -> new KuGouMusicService(client).getLyric("晴天 - 周杰伦"));

        assertEmptyKugouLyric(lyric);
        verify(client, never()).get(contains("lyrics.kugou.com/download"));
    }

    @Test
    void getLyricReturnsEmptyWhenLyricSearchStatusFails() throws IOException {
        KuGouHttpClient client = lyricClientWithSearchResponse(
                LYRIC_SEARCH_RESPONSE.replace("\"status\":200", "\"status\":500"));
        when(client.get(contains("lyrics.kugou.com/download"))).thenReturn(LYRIC_DOWNLOAD_RESPONSE);

        Lyric lyric = assertDoesNotThrow(
                () -> new KuGouMusicService(client).getLyric("晴天 - 周杰伦"));

        assertEmptyKugouLyric(lyric);
        verify(client, never()).get(contains("lyrics.kugou.com/download"));
    }

    @Test
    void getLyricRejectsOnlyMismatchedCandidate() throws IOException {
        String mismatchedCandidate = "{"
                + "\"status\":200,"
                + "\"candidates\":[{"
                + "\"id\":\"wrong\","
                + "\"accesskey\":\"WRONG_ACCESS_KEY\","
                + "\"singer\":\"其他歌手\","
                + "\"song\":\"晴天\","
                + "\"duration\":269000"
                + "}]}";
        KuGouHttpClient client = lyricClientWithSearchResponse(mismatchedCandidate);
        when(client.get(contains("lyrics.kugou.com/download"))).thenReturn(LYRIC_DOWNLOAD_RESPONSE);

        Lyric lyric = assertDoesNotThrow(
                () -> new KuGouMusicService(client).getLyric("晴天 - 周杰伦"));

        assertEmptyKugouLyric(lyric);
        verify(client, never()).get(contains("lyrics.kugou.com/download"));
    }

    @Test
    void getLyricReturnsEmptyWhenDownloadStatusFails() throws IOException {
        KuGouHttpClient client = lyricClientWithSearchResponse(LYRIC_SEARCH_RESPONSE);
        when(client.get(contains("lyrics.kugou.com/download"))).thenReturn(
                LYRIC_DOWNLOAD_RESPONSE.replace("\"status\":200", "\"status\":500"));

        Lyric lyric = assertDoesNotThrow(
                () -> new KuGouMusicService(client).getLyric("晴天 - 周杰伦"));

        assertEmptyKugouLyric(lyric);
    }

    @Test
    void getLyricReturnsEmptyWhenDownloadContentIsEmpty() throws IOException {
        KuGouHttpClient client = lyricClientWithSearchResponse(LYRIC_SEARCH_RESPONSE);
        when(client.get(contains("lyrics.kugou.com/download")))
                .thenReturn("{\"status\":200,\"content\":\"\"}");

        Lyric lyric = assertDoesNotThrow(
                () -> new KuGouMusicService(client).getLyric("晴天 - 周杰伦"));

        assertEmptyKugouLyric(lyric);
    }

    @Test
    void getLyricReturnsEmptyWhenDownloadContentIsInvalidBase64() throws IOException {
        KuGouHttpClient client = lyricClientWithSearchResponse(LYRIC_SEARCH_RESPONSE);
        when(client.get(contains("lyrics.kugou.com/download")))
                .thenReturn("{\"status\":200,\"content\":\"not-base64%%%\"}");

        Lyric lyric = assertDoesNotThrow(
                () -> new KuGouMusicService(client).getLyric("晴天 - 周杰伦"));

        assertEmptyKugouLyric(lyric);
    }

    @Test
    void getLyricReturnsEmptyWhenDecodedBytesAreMalformedUtf8() throws IOException {
        byte[] malformedLrc = new byte[]{
                '[', '0', '0', ':', '0', '1', '.', '0', '0', ']', (byte) 0xC3, 0x28
        };
        String encoded = Base64.getEncoder().encodeToString(malformedLrc);
        KuGouHttpClient client = lyricClientWithSearchResponse(LYRIC_SEARCH_RESPONSE);
        when(client.get(contains("lyrics.kugou.com/download"))).thenReturn(
                "{\"status\":200,\"content\":\"" + encoded + "\"}");

        Lyric lyric = assertDoesNotThrow(
                () -> new KuGouMusicService(client).getLyric("晴天 - 周杰伦"));

        assertEmptyKugouLyric(lyric);
    }

    @Test
    void getLyricReturnsEmptyWhenDecodedTextHasNoTimedLrcTag() throws IOException {
        String untimedContent = Base64.getEncoder().encodeToString(
                "晴天 - 周杰伦\n故事的小黄花".getBytes(StandardCharsets.UTF_8));
        KuGouHttpClient client = lyricClientWithSearchResponse(LYRIC_SEARCH_RESPONSE);
        when(client.get(contains("lyrics.kugou.com/download"))).thenReturn(
                "{\"status\":200,\"content\":\"" + untimedContent + "\"}");

        Lyric lyric = assertDoesNotThrow(
                () -> new KuGouMusicService(client).getLyric("晴天 - 周杰伦"));

        assertEmptyKugouLyric(lyric);
    }

    @Test
    void getLyricRejectsMatchedSongBelowThreshold() throws IOException {
        KuGouHttpClient client = mock(KuGouHttpClient.class);
        when(client.get(contains("song_search_v2"))).thenReturn(
                singleSongSearchResponse("晴天", "其他歌手", 269));
        when(client.get(contains("lyrics.kugou.com/search"))).thenReturn(LYRIC_SEARCH_RESPONSE);
        when(client.get(contains("lyrics.kugou.com/download"))).thenReturn(LYRIC_DOWNLOAD_RESPONSE);

        Lyric lyric = assertDoesNotThrow(
                () -> new KuGouMusicService(client).getLyric("晴天 - 周杰伦"));

        assertEmptyKugouLyric(lyric);
        assertEquals("晴天", lyric.getTitle());
        assertEquals("周杰伦", lyric.getAuthor());
        verify(client, never()).get(contains("lyrics.kugou.com/search"));
    }

    @Test
    void getLyricPropagatesHttpClientIoException() throws IOException {
        KuGouHttpClient client = mock(KuGouHttpClient.class);
        when(client.get(contains("song_search_v2"))).thenThrow(new IOException("offline"));

        IOException exception = assertThrows(IOException.class,
                () -> new KuGouMusicService(client).getLyric("晴天 - 周杰伦"));

        assertEquals("offline", exception.getMessage());
    }

    private static KuGouHttpClient lyricClientWithSearchResponse(String lyricSearchResponse)
            throws IOException {
        KuGouHttpClient client = mock(KuGouHttpClient.class);
        when(client.get(contains("song_search_v2"))).thenReturn(searchResponse());
        when(client.get(contains("lyrics.kugou.com/search"))).thenReturn(lyricSearchResponse);
        return client;
    }

    private static void assertEmptyKugouLyric(Lyric lyric) {
        assertEquals("kugou", lyric.getSource());
        assertFalse(lyric.getHasLyric());
        assertFalse(lyric.getHasKaraokeLyric());
        assertFalse(lyric.getHasTranslatedLyric());
    }

    private static String singleSongSearchResponse(String title, String singer, int duration) {
        return "{"
                + "\"status\":1,"
                + "\"error_code\":0,"
                + "\"data\":{"
                + "\"total\":1,"
                + "\"lists\":[{"
                + "\"ID\":\"single\","
                + "\"FileHash\":\"SINGLE_HASH\","
                + "\"SongName\":\"" + title + "\","
                + "\"Singers\":[{\"id\":1,\"name\":\"" + singer + "\"}],"
                + "\"AlbumName\":\"album\","
                + "\"Duration\":" + duration + ","
                + "\"Image\":\"https://img.kugou.com/{size}/single.jpg\""
                + "}]}"
                + "}";
    }

    private static String searchResponse() {
        return "{"
                + "\"status\":1,"
                + "\"error_code\":0,"
                + "\"data\":{"
                + "\"total\":2,"
                + "\"lists\":[{"
                + "\"ID\":\"live\","
                + "\"FileHash\":\"LIVE_HASH\","
                + "\"SongName\":\"晴天 (Live)\","
                + "\"Singers\":[{\"id\":3520,\"name\":\"周杰伦\"}],"
                + "\"AlbumName\":\"演唱会\","
                + "\"Duration\":300,"
                + "\"Image\":\"https://img.kugou.com/{size}/live.jpg\""
                + "},{"
                + "\"ID\":\"studio\","
                + "\"FileHash\":\"STUDIO_HASH\","
                + "\"SongName\":\"晴天\","
                + "\"Singers\":[{\"id\":3520,\"name\":\"周杰伦\"}],"
                + "\"AlbumName\":\"叶惠美\","
                + "\"Duration\":269,"
                + "\"Image\":\"https://img.kugou.com/{size}/cover.jpg\""
                + "}]}"
                + "}";
    }
}
