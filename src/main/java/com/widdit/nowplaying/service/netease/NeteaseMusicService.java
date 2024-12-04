package com.widdit.nowplaying.service.netease;

import com.widdit.nowplaying.entity.Track;
import com.widdit.nowplaying.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class NeteaseMusicService {

    /**
     * 根据关键词搜索歌曲，返回歌曲信息对象
     * @param keyword 关键词
     * @return
     */
    public Track search(String keyword) throws IOException {
        log.info("获取网易云音乐歌曲信息..");

        // 封装请求参数对象
        UrlParamPair upp = new UrlParamPair();
        upp.addPara("s", keyword);
        upp.addPara("type", 1);
        upp.addPara("offset", 0);
        upp.addPara("limit", 1);
        upp.addPara("csrf_token", "");

        // 发送搜索歌曲请求
        String respStr = sendPostRequest("https://music.163.com/weapi/search/get", upp);

        // 解析 JSON 字符串为 JSONObject
        JSONObject jsonObject = JSON.parseObject(respStr);

        // 检查响应数据的 code
        if (!jsonObject.containsKey("code") || jsonObject.getIntValue("code") != 200) {
            throw new RuntimeException("网易云音乐歌曲信息获取失败");
        }

        // 提取所需字段
        JSONObject song = jsonObject.getJSONObject("result").getJSONArray("songs").getJSONObject(0);

        String id = song.getString("id");
        String title = song.getString("name");
        String album = song.getJSONObject("album").getString("name");
        Integer duration = song.getInteger("duration") / 1000;  // 毫秒转为秒
        String author = song.getJSONArray("artists").getJSONObject(0).getString("name");

        // 如果有第二个歌手，则进行拼接
        JSONArray artists = song.getJSONArray("artists");
        if (artists.size() > 1) {
            author += " / " + artists.getJSONObject(1).getString("name");
        }

        // 计算出格式化的时长
        String durationHuman = TimeUtil.getFormattedDuration(duration);

        // 封装歌曲对象
        Track track = Track.builder()
                .author(author)
                .title(title)
                .album(album)
                .cover("https://gitee.com/widdit/now-playing/raw/master/spotify_no_cover.jpg")
                .duration(duration)
                .durationHuman(durationHuman)
                .url("https://music.youtube.com/watch?v=dQw4w9WgXcQ")
                .id(id)
                .isVideo(false)
                .isAdvertisement(false)
                .inLibrary(false)
                .build();

        track.setCover(getCover(id));

        log.info("获取成功");

        return track;
    }

    /**
     * 获取歌曲封面 URL
     * @param id 歌曲 id
     * @return
     */
    private String getCover(String id) throws IOException {
        log.info("获取网易云音乐歌曲封面..");

        // 封装请求参数对象
        UrlParamPair upp = new UrlParamPair();
        upp.addPara("id", id);
        upp.addPara("c", "[{\"id\":" + id + "}]");
        upp.addPara("ids", "[" + id + "]");
        upp.addPara("csrf_token", "");

        // 发送搜索歌曲请求
        String respStr = sendPostRequest("https://music.163.com/weapi/v3/song/detail", upp);

        // 解析 JSON 字符串为 JSONObject
        JSONObject jsonObject = JSON.parseObject(respStr);

        // 检查响应数据的 code
        if (!jsonObject.containsKey("code") || jsonObject.getIntValue("code") != 200) {
            throw new RuntimeException("网易云音乐歌曲封面获取失败");
        }

        // 提取所需字段
        String cover = jsonObject.getJSONArray("songs").getJSONObject(0).getJSONObject("al").getString("picUrl");
        cover += "?param=500y500";  // 图片大小设置为 500*500

        log.info("获取成功");

        return cover;
    }

    /**
     * 发送 POST 请求
     * @param url 请求 URL
     * @param upp 请求参数对象
     * @return 响应 JSON 字符串
     * @throws IOException
     */
    private String sendPostRequest(String url, UrlParamPair upp) throws IOException {
        String reqStr = upp.getParas().toJSONString();

        Connection.Response
                response = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:57.0) Gecko/20100101 Firefox/57.0")
                .header("Accept", "*/*")
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .header("Host", "music.163.com")
                .header("Accept-Language", "zh-CN,en-US;q=0.7,en;q=0.3")
                .header("DNT", "1")
                .header("Pragma", "no-cache")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .data(JSSecret.getDatas(reqStr))
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .timeout(10000)
                .execute();

        return response.body();
    }

}
