package com.widdit.nowplaying.service.kugou;

import com.widdit.nowplaying.entity.Track;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KuGouMusicServiceTest {
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
