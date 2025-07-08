package com.widdit.nowplaying.service.qq;

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
public class QQMusicService {

    /**
     * 根据关键词搜索歌曲，返回歌曲信息对象
     * @param keyword 关键词
     * @return
     */
    public Track search(String keyword) throws IOException {
        log.info("获取 QQ 音乐歌曲信息..");

        String url = "http://c.y.qq.com/soso/fcgi-bin/client_search_cp?w=" + keyword + "&p=1&n=2&cr=1";

        // 发送搜索歌曲请求
        String respStr = sendGetRequest(url);
        if (respStr.startsWith("callback(") && respStr.endsWith(")")) {
            // 去掉开头和结尾，使之符合 JSON 格式
            respStr = respStr.substring(9, respStr.length() - 1);
        }

        // 解析 JSON 字符串为 JSONObject
        JSONObject jsonObject = JSON.parseObject(respStr);

        // 检查响应数据的 code
        if (!jsonObject.containsKey("code") || jsonObject.getIntValue("code") != 0) {
            throw new RuntimeException("QQ 音乐歌曲信息获取失败：响应码错误");
        }

        // 提取所需字段
        JSONArray songs = jsonObject.getJSONObject("data").getJSONObject("song").getJSONArray("list");
        JSONObject song = songs.getJSONObject(0);

        String author = song.getJSONArray("singer").getJSONObject(0).getString("name");
        String title = song.getString("songname");

        // 比较第二首歌是否更匹配
        if (songs.size() > 1) {
            JSONObject song2 = songs.getJSONObject(1);
            String author2 = song2.getJSONArray("singer").getJSONObject(0).getString("name");
            String title2 = song2.getString("songname");

            // 先保证歌手是对的，再保证标题是对的
            if (!keyword.contains(author) && keyword.contains(author2)) {
                song = song2;
                title = title2;
                author = author2;
            } else if (!keyword.contains(title) && keyword.contains(title2)) {
                song = song2;
                title = title2;
                author = author2;
            }
        }

        String album = song.getString("albumname");
        String albumMid = song.getString("albummid");
        Integer duration = song.getInteger("interval");
        String id = song.getString("songmid");

        // 如果有第二个歌手，则进行拼接
        if (song.getJSONArray("singer").size() > 1) {
            author += " / " + song.getJSONArray("singer").getJSONObject(1).getString("name");
        }

        // 计算出格式化的时长
        String durationHuman = TimeUtil.getFormattedDuration(duration);

        // 封装歌曲对象
        Track track = Track.builder()
                .author(author)
                .title(title)
                .album(album)
                .cover("https://y.qq.com/music/photo_new/T002R500x500M000" + albumMid + "_1.jpg")
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
                .header("Host", "c.y.qq.com")
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
