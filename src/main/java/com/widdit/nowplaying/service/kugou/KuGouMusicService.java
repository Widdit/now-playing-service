package com.widdit.nowplaying.service.kugou;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.widdit.nowplaying.entity.Lyric;
import com.widdit.nowplaying.entity.Track;
import com.widdit.nowplaying.util.SongMatchingUtil;
import com.widdit.nowplaying.util.SongUtil;
import com.widdit.nowplaying.util.TimeUtil;
import com.widdit.nowplaying.util.lyric.generator.LrcGenerator;
import com.widdit.nowplaying.util.lyric.generator.LysGenerator;
import com.widdit.nowplaying.util.lyric.model.LyricLine;
import com.widdit.nowplaying.util.lyric.parser.KrcParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class KuGouMusicService {

    // 缓存相关变量
    private String prevKeyword;
    private Track prevTrack;

    // 锁对象
    private final Object cacheLock = new Object();

    // 歌曲哈希（用作歌词获取凭证）
    private String fileHash;

    /**
     * 根据关键词搜索歌曲，返回歌曲信息对象
     * @param keyword 关键词
     * @return
     */
    public Track search(String keyword) throws IOException {
        log.info("获取酷狗音乐歌曲信息..");

        // 尝试从缓存获取 (加锁读取，保证读取到的是完整的一组数据)
        synchronized (cacheLock) {
            if (Objects.equals(keyword, prevKeyword) && prevTrack != null) {
                log.info("命中歌曲缓存：" + keyword);
                return prevTrack;
            }
        }

        // 缓存未命中，执行网络请求逻辑
        String url = UriComponentsBuilder
                .fromHttpUrl("http://songsearch.kugou.com/song_search_v2")
                .queryParam("keyword", keyword)
                .queryParam("platform", "WebFilter")
                .queryParam("format", "json")
                .queryParam("page", 1)
                .queryParam("pagesize", 5)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        // 发送搜索歌曲请求
        String respStr = sendGetRequest(url);

        // 解析 JSON 字符串为 JSONObject
        JSONObject jsonObject = JSON.parseObject(respStr);

        // 检查响应数据的 code
        if (!jsonObject.containsKey("error_code") || jsonObject.getIntValue("error_code") != 0) {
            throw new RuntimeException("酷狗音乐歌曲信息获取失败，响应码错误（" + respStr + "）");
        }

        // 提取所需字段
        JSONArray songs = jsonObject.getJSONObject("data").getJSONArray("lists");

        // 检查数组是否为空
        if (songs == null || songs.isEmpty()) {
            throw new RuntimeException("酷狗音乐歌曲信息获取失败，搜索结果为空");
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
            String songTitle = song.getString("SongName");

            // 提取歌手名
            JSONArray artists = song.getJSONArray("Singers");
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
        String title = bestMatchSong.getString("SongName");

        JSONArray artists = bestMatchSong.getJSONArray("Singers");
        StringBuilder authorBuilder = new StringBuilder();
        for (int i = 0; i < artists.size(); i++) {
            if (authorBuilder.length() > 0) {
                authorBuilder.append(" / ");
            }
            authorBuilder.append(artists.getJSONObject(i).getString("name"));
        }
        String author = authorBuilder.toString();

        String id = bestMatchSong.getString("ID");
        String album = bestMatchSong.getString("AlbumName");
        Integer duration = bestMatchSong.getInteger("Duration");
        String cover = bestMatchSong.getString("Image").replace("/{size}", "");

        // 提取 FileHash，作为获取歌词的凭证参数
        this.fileHash = bestMatchSong.getString("FileHash");

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
     * 从酷狗音乐获取歌词
     * @param keyword 关键词
     * @return
     * @throws Exception
     */
    public Lyric getLyric(String keyword) throws Exception {
        String[] parseResult = SongUtil.parseWindowTitle(keyword);
        String realTitle = parseResult[0];
        String realAuthor = parseResult[1];

        // 1. 获取歌曲在酷狗音乐的基本信息
        Track track = search(keyword);
        String title = track.getTitle();
        String author = track.getAuthor();
        Integer duration = track.getDuration();

        log.info("从酷狗音乐获取歌词..");

        Lyric lyric = new Lyric();
        lyric.setSource("kugou");
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

        // 如果歌曲错误，则说明酷狗音乐没有该歌曲，也就没有必要再调用 API 获取歌词了
        if (similarity < matchThreshold) {
            // 设置真实歌曲标题，而非错误歌曲标题
            lyric.setTitle(realTitle);
            lyric.setAuthor(realAuthor);

            // 宁可返回空歌词，也不要返回不匹配的歌词
            log.warn("酷狗歌词获取失败（未找到匹配歌曲）");
            return lyric;
        }

        // 2. 搜索歌词，获取歌词 id 和 accesskey
        String searchUrl = UriComponentsBuilder
                .fromHttpUrl("https://lyrics.kugou.com/search")
                .queryParam("ver", "1")
                .queryParam("man", "yes")
                .queryParam("client", "pc")
                .queryParam("keyword", "")
                .queryParam("hash", this.fileHash)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        String searchRespStr = sendGetRequest(searchUrl);

        JSONObject searchJsonObject = JSON.parseObject(searchRespStr);

        if (!searchJsonObject.containsKey("errcode")) {
            throw new RuntimeException("酷狗歌词搜索失败（hash = " + this.fileHash + "）：响应结果不包含 errcode 字段");
        }
        int searchErrCode = searchJsonObject.getIntValue("errcode");
        if (searchErrCode != 200 && searchErrCode != 0) {
            throw new RuntimeException("酷狗歌词搜索失败（hash = " + this.fileHash + "）：响应结果的 errcode 为 " + searchErrCode);
        }

        JSONArray candidates = searchJsonObject.getJSONArray("candidates");
        if (candidates == null || candidates.isEmpty()) {
            log.info("酷狗歌词获取成功（匹配度：{}%，该歌曲无歌词）", similarity);
            return lyric;
        }

        JSONObject firstCandidate = candidates.getJSONObject(0);
        String lyricId = firstCandidate.getString("id");
        String accesskey = firstCandidate.getString("accesskey");

        // 3. 获取 KRC 歌词内容，并提取翻译歌词和 LRC 歌词
        String downloadUrl = UriComponentsBuilder
                .fromHttpUrl("https://lyrics.kugou.com/download")
                .queryParam("ver", "1")
                .queryParam("client", "pc")
                .queryParam("id", lyricId)
                .queryParam("accesskey", accesskey)
                .queryParam("fmt", "krc")
                .queryParam("charset", "utf8")
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        String downloadRespStr = sendGetRequest(downloadUrl);

        JSONObject downloadJsonObject = JSON.parseObject(downloadRespStr);

        if (!downloadJsonObject.containsKey("error_code")) {
            throw new RuntimeException("酷狗歌词获取失败（hash = " + this.fileHash + "）：响应结果不包含 error_code 字段");
        }
        int downloadErrCode = downloadJsonObject.getIntValue("error_code");
        if (downloadErrCode != 0 && downloadErrCode != 200) {
            throw new RuntimeException("酷狗歌词获取失败（hash = " + this.fileHash + "）：响应结果的 error_code 为 " + downloadErrCode);
        }

        String encryptedContent = downloadJsonObject.getString("content");
        String krcContent = Decrypter.decryptLyrics(encryptedContent);

        if (!StringUtils.isBlank(krcContent) && !hasInstrumentalHint(krcContent)) {
            // 将 KRC 格式的逐字歌词解析为 List<LyricLine> 内部对象
            List<LyricLine> lyricLines = KrcParser.parse(krcContent);

            // 根据 List<LyricLine> 内部对象生成 LYS 格式的逐字歌词
            String lys = LysGenerator.generate(lyricLines, "krc");

            lyric.setHasKaraokeLyric(true);
            lyric.setKaraokeLyric(lys);

            // 根据 List<LyricLine> 内部对象生成 LRC 歌词
            String lrc = LrcGenerator.generate(lyricLines);

            lyric.setHasLyric(true);
            lyric.setLrc(lrc);

            // 从 KRC 中提取翻译歌词，生成翻译歌词字符串（若无翻译则为 null）
            String translationLrc = KrcParser.parseTranslationLrc(krcContent, lyricLines);

            if (translationLrc != null && !translationLrc.isBlank()) {
                lyric.setHasTranslatedLyric(true);
                lyric.setTranslatedLyric(translationLrc);
            }
        }

        if (lyric.getHasKaraokeLyric() || lyric.getHasLyric()) {
            log.info("酷狗歌词获取成功（匹配度：{}%）", similarity);
        } else {
            log.info("酷狗歌词获取成功（匹配度：{}%，该歌曲无歌词）", similarity);
        }

        return lyric;
    }

    /**
     * 判断歌词是否包含纯音乐提示
     * @param krc KRC 歌词字符串
     * @return 如果包含纯音乐提示则返回 true
     */
    private boolean hasInstrumentalHint(String krc) {
        StringBuilder sb = new StringBuilder();
        boolean inTag = false;
        for (int i = 0; i < krc.length(); i++) {
            char c = krc.charAt(i);
            if (c == '<') inTag = true;
            else if (c == '>') inTag = false;
            else if (!inTag) sb.append(c);
        }
        return sb.indexOf("纯音乐，请欣赏") >= 0;
    }

    /**
     * 发送 GET 请求
     * @param url 请求 URL
     * @return 响应 JSON 字符串
     * @throws IOException
     */
    private String sendGetRequest(String url) throws IOException {
        URL parsedUrl = new URL(url);
        String host = parsedUrl.getHost();

        Connection.Response response = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:57.0) Gecko/20100101 Firefox/57.0")
                .header("Accept", "*/*")
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .header("Host", host)
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
