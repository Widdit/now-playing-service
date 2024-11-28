package com.widdit.nowplaying.service.kugou;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.widdit.nowplaying.entity.Track;
import com.widdit.nowplaying.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class KuGouMusicService {

    /**
     * 根据关键词搜索歌曲，返回歌曲信息对象
     * @param keyword 关键词
     * @return
     */
    public Track search(String keyword) throws IOException {
        log.info("获取酷狗音乐歌曲信息..");

        String url = "http://songsearch.kugou.com/song_search_v2?keyword=" + keyword + "&platform=WebFilter&format=json&page=1&pagesize=1";

        // 发送搜索歌曲请求
        String respStr = sendGetRequest(url);

        // 解析 JSON 字符串为 JSONObject
        JSONObject jsonObject = JSON.parseObject(respStr);

        // 检查响应数据的 code
        if (!jsonObject.containsKey("error_code") || jsonObject.getIntValue("error_code") != 0) {
            throw new RuntimeException("酷狗音乐歌曲信息获取失败");
        }

        // 提取所需字段
        JSONObject song = jsonObject.getJSONObject("data").getJSONArray("lists").getJSONObject(0);

        String album = song.getString("AlbumName");
        String title = song.getString("SongName");
        Integer duration = song.getInteger("SQDuration");
        String cover = song.getString("Image").replace("/{size}", "");
        String id = song.getString("ID");

        JSONArray singers = song.getJSONArray("Singers");
        String author = singers.getJSONObject(0).getString("name");
        if (singers.size() > 1) {
            author += " / " + singers.getJSONObject(1).getString("name");
        }

        // 计算出格式化的时长
        String durationHuman = TimeUtil.getFormattedDuration(duration);

        // 封装歌曲对象
        Track track = Track.builder()
                .author(author)
                .title(title)
                .album(album)
                .cover(cover)
                .duration(duration)
                .durationHuman(durationHuman)
                .url("https://music.youtube.com/watch?v=dQw4w9WgXcQ")
                .id(id)
                .isVideo(false)
                .isAdvertisement(false)
                .inLibrary(false)
                .build();

        log.info("获取成功");

        return track;
    }

    /**
     * 发送 GET 请求
     * @param url 请求 URL
     * @return 响应 JSON 字符串
     * @throws IOException
     */
    private String sendGetRequest(String url) throws IOException {
        Connection.Response
                response = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:57.0) Gecko/20100101 Firefox/57.0")
                .header("Accept", "*/*")
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .header("Host", "songsearch.kugou.com")
                .header("Accept-Language", "zh-CN,en-US;q=0.7,en;q=0.3")
                .header("DNT", "1")
                .header("Pragma", "no-cache")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .timeout(10000)
                .execute();

        return response.body();
    }

}
