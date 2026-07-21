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
    void delegatesSearchAndMapsResponse() throws IOException {
        KuGouHttpClient client = mock(KuGouHttpClient.class);
        when(client.get(anyString())).thenReturn("{"
                + "\"status\":1,"
                + "\"error_code\":0,"
                + "\"data\":{"
                + "\"total\":1,"
                + "\"lists\":[{"
                + "\"ID\":\"12345\","
                + "\"SongName\":\"晴天\","
                + "\"Singers\":[{\"id\":3520,\"name\":\"周杰伦\"}],"
                + "\"AlbumName\":\"叶惠美\","
                + "\"Duration\":269,"
                + "\"Image\":\"https://img.kugou.com/{size}/cover.jpg\""
                + "}]}"
                + "}");
        KuGouMusicService service = new KuGouMusicService(client);

        Track track = service.search("晴天 - 周杰伦");

        assertEquals("12345", track.getId());
        assertEquals("晴天", track.getTitle());
        assertEquals("周杰伦", track.getAuthor());
        assertEquals("叶惠美", track.getAlbum());
        assertEquals(269, track.getDuration());
        assertEquals("4:29", track.getDurationHuman());
        assertEquals("https://img.kugou.com/cover.jpg", track.getCover());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(urlCaptor.capture());
        String requestedUrl = urlCaptor.getValue();
        assertTrue(requestedUrl.startsWith("http://songsearch.kugou.com/song_search_v2?"));
        assertTrue(requestedUrl.contains(
                "keyword=%E6%99%B4%E5%A4%A9%20-%20%E5%91%A8%E6%9D%B0%E4%BC%A6"));
    }
}
