package com.widdit.nowplaying.service.netease;

import com.widdit.nowplaying.entity.Lyric;
import com.widdit.nowplaying.entity.Track;
import com.widdit.nowplaying.service.AudioService;
import com.widdit.nowplaying.service.SettingsService;
import com.widdit.nowplaying.service.qq.QQMusicService;
import com.widdit.nowplaying.util.SongMatchingUtil;
import com.widdit.nowplaying.util.SongUtil;
import com.widdit.nowplaying.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class NeteaseMusicService {

    @Autowired
    private QQMusicService qqMusicService;
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private AudioService audioService;

    // 缓存相关变量
    private String prevKeyword;
    private Track prevTrack;

    // 锁对象
    private final Object cacheLock = new Object();

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

        // 尝试从缓存获取 (加锁读取，保证读取到的是完整的一组数据)
        synchronized (cacheLock) {
            if (Objects.equals(keyword, prevKeyword) && prevTrack != null) {
                log.info("命中歌曲缓存：" + keyword);
                return prevTrack;
            }
        }

        // 缓存未命中，执行网络请求逻辑
        // 封装请求参数对象
        Map<String, String> data = new HashMap<>();
        data.put("s", keyword);
        data.put("limit", "5");
        data.put("offset", "0");
        data.put("type", "1");
        data.put("csrf_token", "");

        // 发送搜索歌曲请求
        String respStr = EapiHelper.post("https://interface3.music.163.com/eapi/search/get", data);

        // 解析 JSON 字符串为 JSONObject
        JSONObject jsonObject = JSON.parseObject(respStr);

        // 检查响应数据的 code
        if (jsonObject == null || !jsonObject.containsKey("code") || jsonObject.getIntValue("code") != 200) {
            throw new RuntimeException("网易云音乐歌曲信息获取失败，响应码错误（" + respStr + "）");
        }

        // 提取所需字段
        JSONArray songs = jsonObject.getJSONObject("result").getJSONArray("songs");

        // 检查数组是否为空
        if (songs == null || songs.isEmpty()) {
            throw new RuntimeException("网易云音乐歌曲信息获取失败，搜索结果为空");
        }

        // 最多遍历前 5 个元素
        int maxCount = Math.min(songs.size(), 5);

        // 解析出本地歌曲信息，用于后续计算歌曲信息匹配度
        String[] parseResult = SongUtil.parseWindowTitle(keyword);
        String localTitle = parseResult[0];
        String localAuthor = parseResult[1];

        // 用于记录最佳匹配的歌曲
        JSONObject bestMatchSong = null;
        int highestSimilarity = -1;

        // 遍历歌曲数组
        for (int index = 0; index < maxCount; index++) {
            JSONObject song = songs.getJSONObject(index);

            // 提取歌曲标题
            String songTitle = song.getString("name");

            // 提取歌手名
            JSONArray artists = song.getJSONArray("artists");
            StringBuilder authorBuilder = new StringBuilder();
            for (int i = 0; i < artists.size(); i++) {
                if (authorBuilder.length() > 0) {
                    authorBuilder.append(" / ");
                }
                authorBuilder.append(artists.getJSONObject(i).getString("name"));
            }
            String songAuthor = authorBuilder.toString();

            // 计算相似度
            int similarity = SongMatchingUtil.calculateSimilarity(localTitle, localAuthor, songTitle, songAuthor);

            // 如果完美匹配，直接选中并退出循环
            if (similarity >= 100) {
                bestMatchSong = song;
                break;
            }

            // 记录相似度最高的歌曲
            if (similarity > highestSimilarity) {
                highestSimilarity = similarity;
                bestMatchSong = song;
            }
        }

        // 从最佳匹配的歌曲中提取最终信息
        String title = bestMatchSong.getString("name");

        JSONArray artists = bestMatchSong.getJSONArray("artists");
        StringBuilder authorBuilder = new StringBuilder();
        for (int i = 0; i < artists.size(); i++) {
            if (authorBuilder.length() > 0) {
                authorBuilder.append(" / ");
            }
            authorBuilder.append(artists.getJSONObject(i).getString("name"));
        }
        String author = authorBuilder.toString();

        String id = bestMatchSong.getString("id");
        Integer duration = bestMatchSong.getInteger("duration") / 1000;  // 毫秒转为秒

        JSONObject albumObject = bestMatchSong.getJSONObject("album");

        String album = albumObject.getString("name");
        long picId = albumObject.getLong("picId");
        String cover = CoverHelper.buildCoverUrl(picId, 500);

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

        // 更新缓存 (加锁写入)
        synchronized (cacheLock) {
            this.prevKeyword = keyword;
            this.prevTrack = track;
        }

        return track;
    }

    /**
     * 获取歌曲封面 URL
     * （由于需要多一次网络请求，已弃用，推荐使用 CoverHelper 直接根据 picUrl 进行加密获取封面 URL）
     * @param id 歌曲 id
     * @return
     */
    public String getCoverUrl(String id) throws Exception {
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
            throw new RuntimeException("网易云音乐歌曲封面获取失败：响应码错误（" + respStr + "）");
        }

        // 提取所需字段
        String cover = jsonObject.getJSONArray("songs").getJSONObject(0).getJSONObject("album").getString("picUrl");
        cover += "?param=500y500";  // 图片大小设置为 500*500

        return cover;
    }

    /**
     * 从网易云音乐获取歌词
     * @param keyword 关键词
     * @return
     * @throws Exception
     */
    public Lyric getLyric(String keyword) throws Exception {
        String[] parseResult = SongUtil.parseWindowTitle(keyword);
        String realTitle = parseResult[0];
        String realAuthor = parseResult[1];

        String title = "";
        String author = "";
        Integer duration = 0;

        // 1. 获取歌曲在网易云音乐的 ID 和基本信息
        String id = null;

        boolean isNetease = "netease".equals(settingsService.getSettingsGeneral().getPlatform());
        boolean fallbackEnabled = settingsService.getSettingsGeneral().getFallbackPlatformEnabled();
        boolean primaryRunning = audioService.getPrimaryPlatformRunning();

        // 当前平台刚好就是网易云音乐，免去一次搜索歌曲的网络请求
        if (isNetease && (!fallbackEnabled || primaryRunning)) {
            int maxRetries = 5;

            for (int attempt = 0; attempt <= maxRetries; attempt++) {
                // 发送 GET 请求
                URL url = new URL("http://localhost:9863/api/query/track");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // 读取响应
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // 解析 JSON
                JSONObject jsonObject = JSON.parseObject(response.toString());
                title = jsonObject.getString("title");
                author = jsonObject.getString("author");
                duration = jsonObject.getInteger("duration");

                int similarity = SongMatchingUtil.calculateSimilarity(realTitle, realAuthor, title, author);
                if (similarity >= SongMatchingUtil.EXACT_MATCH_THRESHOLD) {
                    // 歌曲信息匹配，则提取歌曲 ID
                    id = jsonObject.getString("id");
                    break;

                } else if (attempt < maxRetries) {
                    // 歌曲信息不匹配（通常是由于 query 接口未及时更新数据），等待一段时间后重试
                    Thread.sleep(50);
                }
            }
        }

        if (id == null || "".equals(id)) {
            Track track = search(keyword);
            id = track.getId();
            title = track.getTitle();
            author = track.getAuthor();
            duration = track.getDuration();
        }

        log.info("从网易云音乐获取歌词..");

        Lyric lyric = new Lyric();
        lyric.setSource("netease");
        lyric.setTitle(title);
        lyric.setAuthor(author);
        lyric.setDuration(duration);

        // 计算相似度，判断歌曲信息与真实信息是否匹配
        int similarity = SongMatchingUtil.calculateSimilarity(realTitle, realAuthor, title, author);

        // 如果歌曲错误，则说明网易云音乐没有该歌曲，也就没有必要再调用 API 获取歌词了
        if (similarity < SongMatchingUtil.EXACT_MATCH_THRESHOLD) {
            // 设置真实歌曲标题，而非错误歌曲标题
            lyric.setTitle(realTitle);
            lyric.setAuthor(realAuthor);

            // 宁可返回空歌词，也不要返回不匹配的歌词
            log.warn("网易云歌词获取失败（未找到匹配歌曲）");
            return lyric;
        }

        // 2. 获取歌词
        // 构建请求参数
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

        if (!jsonObject.containsKey("lrc")) {
            log.info("网易云歌词获取成功（匹配度：{}%，该歌曲无歌词）", similarity);
            return lyric;
        }

        // 提取原始歌词
        String lrc = jsonObject.getJSONObject("lrc").getString("lyric");
        if (lrc == null || "".equals(lrc) || !lrc.contains("00") || lrc.contains("纯音乐，请欣赏")) {
            log.info("网易云歌词获取成功（匹配度：{}%，该歌曲无歌词）", similarity);
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

        // 提取逐字歌词
        if (jsonObject.containsKey("yrc")) {
            String karaokeLyric = jsonObject.getJSONObject("yrc").getString("lyric");
            if (!StringUtils.isBlank(karaokeLyric)) {
                lyric.setHasKaraokeLyric(true);
                lyric.setKaraokeLyric(karaokeLyric);
            }
        }

        log.info("网易云歌词获取成功（匹配度：{}%）", similarity);

        return lyric;
    }

}
