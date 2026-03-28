package com.widdit.nowplaying.service.qq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.widdit.nowplaying.entity.Lyric;
import com.widdit.nowplaying.entity.Track;
import com.widdit.nowplaying.util.SongMatchingUtil;
import com.widdit.nowplaying.util.SongUtil;
import com.widdit.nowplaying.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class QQMusicService {

    // 缓存相关变量
    private String prevKeyword;
    private Track prevTrack;

    // 锁对象
    private final Object cacheLock = new Object();

    private static final Map<String, String> VERBATIM_XML_MAPPING_DICT = new HashMap<>();

    static {
        VERBATIM_XML_MAPPING_DICT.put("content", "orig");       // 原文
        VERBATIM_XML_MAPPING_DICT.put("contentts", "ts");       // 译文
        VERBATIM_XML_MAPPING_DICT.put("contentroma", "roma");   // 罗马音
        VERBATIM_XML_MAPPING_DICT.put("Lyric_1", "lyric");      // 解压后的内容
    }

    /**
     * 根据关键词搜索歌曲，返回歌曲信息对象
     * @param keyword 关键词
     * @return
     */
    public Track search(String keyword) throws Exception {
        log.info("获取 QQ 音乐歌曲信息..");

        // 尝试从缓存获取 (加锁读取，保证读取到的是完整的一组数据)
        synchronized (cacheLock) {
            if (Objects.equals(keyword, prevKeyword) && prevTrack != null) {
                log.info("命中歌曲缓存：" + keyword);
                return prevTrack;
            }
        }

        // 缓存未命中，执行网络请求逻辑
        // 构建请求体
        JSONObject param = new JSONObject();
        param.put("search_type", 0);
        param.put("query", keyword);
        param.put("page_num", 1);
        param.put("num_per_page", 8);

        JSONObject req1 = new JSONObject();
        req1.put("method", "DoSearchForQQMusicDesktop");
        req1.put("module", "music.search.SearchCgiService");
        req1.put("param", param);

        JSONObject requestBody = new JSONObject();
        requestBody.put("req_1", req1);

        // 发送搜索歌曲请求
        String respStr = sendPostRequest("https://u.y.qq.com/cgi-bin/musicu.fcg", requestBody.toJSONString());

        // 解析 JSON 字符串为 JSONObject
        JSONObject jsonObject = JSON.parseObject(respStr);

        // 检查响应数据的 code
        if (!jsonObject.containsKey("code") || jsonObject.getIntValue("code") != 0) {
            throw new RuntimeException("QQ 音乐歌曲信息获取失败，响应码错误（" + respStr + "）");
        }

        // 提取所需字段
        JSONArray songs = jsonObject.getJSONObject("req_1").getJSONObject("data").getJSONObject("body").getJSONObject("song").getJSONArray("list");

        // 检查数组是否为空
        if (songs == null || songs.isEmpty()) {
            throw new RuntimeException("QQ 音乐歌曲信息获取失败，搜索结果为空");
        }

        // 最多遍历前 8 个元素
        int maxCount = Math.min(songs.size(), 8);

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
            String songTitle = song.getString("title");

            // 提取歌手名
            JSONArray artists = song.getJSONArray("singer");
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
        String title = bestMatchSong.getString("title");

        JSONArray artists = bestMatchSong.getJSONArray("singer");
        StringBuilder authorBuilder = new StringBuilder();
        for (int i = 0; i < artists.size(); i++) {
            if (authorBuilder.length() > 0) {
                authorBuilder.append(" / ");
            }
            authorBuilder.append(artists.getJSONObject(i).getString("name"));
        }
        String author = authorBuilder.toString();

        String id = bestMatchSong.getString("id");
        String album = bestMatchSong.getJSONObject("album").getString("name");
        String albumMid = bestMatchSong.getJSONObject("album").getString("mid");
        Integer duration = bestMatchSong.getInteger("interval");

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

        // 更新缓存 (加锁写入)
        synchronized (cacheLock) {
            this.prevKeyword = keyword;
            this.prevTrack = track;
        }

        return track;
    }

    /**
     * 从 QQ 音乐获取歌词
     * @param keyword 关键词
     * @return
     * @throws Exception
     */
    public Lyric getLyric(String keyword) throws Exception {
        String[] parseResult = SongUtil.parseWindowTitle(keyword);
        String realTitle = parseResult[0];
        String realAuthor = parseResult[1];

        // 1. 获取歌曲在 QQ 音乐的 ID 和基本信息
        Track track = search(keyword);
        String id = track.getId();
        String title = track.getTitle();
        String author = track.getAuthor();
        Integer duration = track.getDuration();

        log.info("从 QQ 音乐获取歌词..");

        Lyric lyric = new Lyric();
        lyric.setSource("qq");
        lyric.setTitle(title);
        lyric.setAuthor(author);
        lyric.setDuration(duration);

        // 计算相似度，判断歌曲信息与真实信息是否匹配
        int similarity = SongMatchingUtil.calculateSimilarity(realTitle, realAuthor, title, author);

        int matchThreshold = SongMatchingUtil.EXACT_MATCH_THRESHOLD;
        // 对于歌手名缺失的情况，可适当降低阈值标准
        if (realAuthor == null || realAuthor.isBlank()) {
            matchThreshold = 75;
        }

        // 如果歌曲错误，则说明 QQ 音乐没有该歌曲，也就没有必要再调用 API 获取歌词了
        if (similarity < matchThreshold) {
            // 设置真实歌曲标题，而非错误歌曲标题
            lyric.setTitle(realTitle);
            lyric.setAuthor(realAuthor);

            // 宁可返回空歌词，也不要返回不匹配的歌词
            log.warn("QQ 歌词获取失败（未找到匹配歌曲）");
            return lyric;
        }

        // 2. 获取逐字歌词与翻译歌词
        QrcLyric qrcLyric = getQrcLyric(id);

        if (!StringUtils.isBlank(qrcLyric.getQrc())) {
            lyric.setHasKaraokeLyric(true);
            lyric.setKaraokeLyric(qrcLyric.getQrc());
        }

        if (!StringUtils.isBlank(qrcLyric.getTrans())) {
            lyric.setHasTranslatedLyric(true);
            lyric.setTranslatedLyric(qrcLyric.getTrans());
        }

        // 3. 获取原始歌词
        // 构建请求参数
        Map<String, String> params = new HashMap<>();
        params.put("musicid", id);
        params.put("callback", "MusicJsonCallback_lrc");
        params.put("pcachetime", String.valueOf(System.currentTimeMillis()));
        params.put("g_tk", "5381");
        params.put("jsonpCallback", "MusicJsonCallback_lrc");
        params.put("loginUin", "0");
        params.put("hostUin", "0");
        params.put("format", "json");
        params.put("inCharset", "utf8");
        params.put("outCharset", "utf8");
        params.put("notice", "0");
        params.put("platform", "yqq");
        params.put("needNewCode", "0");
        params.put("nobase64", "1");

        // 发送请求
        String respStr = sendGetRequest("https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg", params);

        // 解析 JSON 字符串为 JSONObject
        JSONObject jsonObject = JSON.parseObject(respStr);

        // 检查响应数据的 code
        if (!jsonObject.containsKey("code")) {
            throw new RuntimeException("获取原始歌词失败（id = " + id + "）：响应结果不包含 code 字段");
        }
        int retCode = jsonObject.getIntValue("code");
        if (retCode != 0 && retCode != -1901) {
            throw new RuntimeException("获取原始歌词失败（id = " + id + "）：响应结果的 code 为 " + retCode);
        }

        // -1901 为一个特殊的 code，它并不代表 API 请求错误，而是 QQ 音乐没有歌词，因此需要特殊处理，不抛出异常
        if (jsonObject.containsKey("lyric") && retCode != -1901) {
            // 提取原始歌词
            String lrc = jsonObject.getString("lyric");
            if (lrc != null && !lrc.isBlank() && lrc.contains("00") && !lrc.contains("此歌曲为没有填词的纯音乐")) {
                lyric.setHasLyric(true);
                lyric.setLrc(lrc);
            }
        }

        // 提取翻译歌词（兼容处理）
        // 说明：
        // 1. 自 2026 年 3 月起，QQ 音乐该接口不再返回翻译歌词（可能是为了减少网络开销）
        // 2. 多数歌曲已通过 QRC 歌词提供完整信息（含逐字 + 翻译）
        // 3. 为兼容仍返回翻译歌词的情况，此处保留兜底判断
        if (!lyric.getHasTranslatedLyric() && jsonObject.containsKey("trans")) {
            String translatedLyric = jsonObject.getString("trans");
            if (!StringUtils.isBlank(translatedLyric)) {
                lyric.setHasTranslatedLyric(true);
                lyric.setTranslatedLyric(translatedLyric);
            }
        }

        if (lyric.getHasKaraokeLyric() || lyric.getHasLyric()) {
            log.info("QQ 歌词获取成功（匹配度：{}%）", similarity);
        } else {
            log.info("QQ 歌词获取成功（匹配度：{}%，该歌曲无歌词）", similarity);
        }

        return lyric;
    }

    /**
     * 获取 QRC 逐字歌词（含翻译歌词）
     *
     * 原作者：WXRIW
     * 代码链接：https://github.com/WXRIW/Lyricify-Lyrics-Helper/blob/master/Lyricify.Lyrics.Helper/Providers/Web/QQMusic/Api.cs
     * Licensed under the Apache License, Version 2.0
     *
     * 修改者：Widdit
     *
     * @param songid 歌曲 ID
     * @return 包含解密后的 QRC 逐字歌词和翻译歌词的 QrcLyric 对象
     */
    public QrcLyric getQrcLyric(String songid) {
        try {
            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("musicid", songid);
            params.put("version", "15");
            params.put("miniversion", "82");
            params.put("lrctype", "4");

            // 发送请求
            String resp = sendGetRequest("https://c.y.qq.com/qqmusic/fcgi-bin/lyric_download.fcg", params);

            // 处理响应
            resp = resp.replace("<!--", "").replace("-->", "");

            Map<String, Node> dict = new HashMap<>();
            Document doc = Decrypter.createXmlDocument(resp);
            Decrypter.recursionFindElement(doc.getDocumentElement(), VERBATIM_XML_MAPPING_DICT, dict);

            QrcLyric qrcLyric = new QrcLyric();

            // 提取逐字歌词节点 "orig"
            if (dict.containsKey("orig")) {
                String text = Decrypter.getNodeText(dict.get("orig"));

                if (text != null && !text.isBlank()) {
                    try {
                        String decompressText = Decrypter.decryptLyrics(text);
                        if (decompressText != null && !decompressText.isBlank()) {
                            qrcLyric.setQrc(decompressText);
                        }
                    } catch (Exception e) {
                        log.error("解密 QRC 歌词失败（id = {}）：{}", songid, e.getMessage());
                    }
                }
            }

            // 提取翻译歌词节点 "ts"
            if (dict.containsKey("ts")) {
                String text = Decrypter.getNodeText(dict.get("ts"));

                if (text != null && !text.isBlank()) {
                    // 去除影响美观的 "//" 标记
                    text = text.replace("//", "");
                    qrcLyric.setTrans(text);
                }
            }

            return qrcLyric;

        } catch (Exception e) {
            log.error("获取 QRC 逐字歌词失败（id = {}）：{}", songid, e.getMessage());
            return new QrcLyric();
        }
    }

    /**
     * 发送 GET 请求
     * @param url 请求 URL
     * @param params 查询参数
     * @return 响应 JSON 字符串
     */
    private String sendGetRequest(String url, Map<String, String> params) throws Exception {
        URL parsedUrl = new URL(url);
        String host = parsedUrl.getHost();
        String referer = parsedUrl.getProtocol() + "://" + host + "/";

        Connection.Response response = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                .header("Accept", "*/*")
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .header("Host", host)
                .header("Referer", referer)
                .method(Connection.Method.GET)
                .data(params)
                .ignoreContentType(true)
                .timeout(10000)
                .execute();

        return response.body();
    }

    /**
     * 发送 POST 请求
     * @param url 请求 URL
     * @param body 请求体 JSON 字符串
     * @return 响应 JSON 字符串
     */
    private String sendPostRequest(String url, String body) throws Exception {
        URL parsedUrl = new URL(url);
        String host = parsedUrl.getHost();
        String referer = parsedUrl.getProtocol() + "://" + host + "/";

        Connection.Response response = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                .header("Accept", "*/*")
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .header("Host", host)
                .header("Referer", referer)
                .header("Content-Type", "application/json")
                .method(Connection.Method.POST)
                .requestBody(body)
                .ignoreContentType(true)
                .timeout(10000)
                .execute();

        return response.body();
    }

}