package com.widdit.nowplaying.service.netease;

import com.widdit.nowplaying.entity.Lyric;
import com.widdit.nowplaying.entity.Track;
import com.widdit.nowplaying.service.qq.QQMusicService;
import com.widdit.nowplaying.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class NeteaseMusicService {

    @Autowired
    private QQMusicService qqMusicService;

    /**
     * 根据关键词搜索歌曲，返回歌曲信息对象
     * @param keyword 关键词
     * @return
     */
    public Track search(String keyword) throws Exception {
        // 网易云没有周杰伦版权，借用 QQ 音乐搜索
        if (keyword.contains("周杰伦") || keyword.contains("周杰倫")) {
            log.info("当前歌手为周杰伦，借用 QQ 音乐搜索");
            return qqMusicService.search(keyword);
        }

        log.info("获取网易云音乐歌曲信息..");

        // 封装请求参数对象
        Map<String, String> data = new HashMap<>();
        data.put("s", keyword);
        data.put("limit", "2");
        data.put("offset", "0");
        data.put("type", "1");
        data.put("csrf_token", "");

        // 发送搜索歌曲请求
        String respStr = EapiHelper.post("https://interface3.music.163.com/eapi/search/get", data);

        // 解析 JSON 字符串为 JSONObject
        JSONObject jsonObject = JSON.parseObject(respStr);

        // 检查响应数据的 code
        if (jsonObject == null || !jsonObject.containsKey("code") || jsonObject.getIntValue("code") != 200) {
            throw new RuntimeException("网易云音乐歌曲信息获取失败：响应码错误");
        }

        // 提取所需字段
        JSONArray songs = jsonObject.getJSONObject("result").getJSONArray("songs");
        JSONObject song = songs.getJSONObject(0);

        String author = song.getJSONArray("artists").getJSONObject(0).getString("name");
        String title = song.getString("name");

        // 比较第二首歌是否更匹配
        if (songs.size() > 1) {
            JSONObject song2 = songs.getJSONObject(1);
            String author2 = song2.getJSONArray("artists").getJSONObject(0).getString("name");
            String title2 = song2.getString("name");

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

        String id = song.getString("id");
        String album = song.getJSONObject("album").getString("name");
        Integer duration = song.getInteger("duration") / 1000;  // 毫秒转为秒

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
    public String getCover(String id) throws Exception {
        // 封装请求参数对象
        Map<String, String> data = new HashMap<>();
        data.put("id", id);
        data.put("c", "[{\"id\":" + id + "}]");
        data.put("ids", "[" + id + "]");
        data.put("csrf_token", "");

        // 发送获取歌曲详情请求
        String respStr = EapiHelper.post("https://interface3.music.163.com/eapi/song/detail", data);

        // 解析 JSON 字符串为 JSONObject
        JSONObject jsonObject = JSON.parseObject(respStr);

        // 检查响应数据的 code
        if (!jsonObject.containsKey("code") || jsonObject.getIntValue("code") != 200) {
            throw new RuntimeException("网易云音乐歌曲封面获取失败：响应码错误");
        }

        // 提取所需字段
        String cover = jsonObject.getJSONArray("songs").getJSONObject(0).getJSONObject("album").getString("picUrl");
        cover += "?param=500y500";  // 图片大小设置为 500*500

        return cover;
    }

    /**
     * 从网易云音乐获取歌词对象
     * @param keyword 关键词
     * @return
     * @throws Exception
     */
    public Lyric getLyric(String keyword) throws Exception {
        Track track = search(keyword);
        String id = track.getId();

        log.info("从网易云音乐获取歌词..");

        Map<String, String> data = new HashMap<>();
        data.put("id", id);
        data.put("cp", "false");
        data.put("lv", "0");
        data.put("kv", "0");
        data.put("tv", "0");
        data.put("rv", "0");
        data.put("yv", "0");
        data.put("ytv", "0");
        data.put("yrv", "0");
        data.put("csrf_token", "");
        String respStr = EapiHelper.post("https://interface3.music.163.com/eapi/song/lyric/v1", data);

        // 解析 JSON 字符串为 JSONObject
        JSONObject jsonObject = JSON.parseObject(respStr);

        // 检查响应数据的 code
        if (!jsonObject.containsKey("code") || jsonObject.getIntValue("code") != 200) {
            throw new RuntimeException("获取歌词失败：id = " + id);
        }

        Lyric lyric = new Lyric();
        lyric.setSource("netease");

        if (!jsonObject.containsKey("lrc")) {
            return lyric;
        }

        // 提取原版歌词
        String lrc = jsonObject.getJSONObject("lrc").getString("lyric");
        if (lrc == null || "".equals(lrc) || !lrc.contains("00")) {
            return lyric;
        }
        lyric.setHasLyric(true);
        lyric.setLrc(lrc);

        // 提取翻译歌词
        if (jsonObject.containsKey("tlyric")) {
            String translatedLyric = jsonObject.getJSONObject("tlyric").getString("lyric");
            if (!StringUtils.isBlank(translatedLyric)) {
                lyric.setHasTranslatedLyric(true);
                lyric.setTranslatedLyric(translatedLyric);
            }
        }

        // 提取逐词歌词
        if (jsonObject.containsKey("yrc")) {
            String karaokeLyric = jsonObject.getJSONObject("yrc").getString("lyric");
            if (!StringUtils.isBlank(karaokeLyric)) {
                lyric.setHasKaraokeLyric(true);
                lyric.setKaraokeLyric(karaokeLyric);
            }
        }

        log.info("获取成功");

        return lyric;
    }

}
