package com.widdit.nowplaying.service.kugou;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.widdit.nowplaying.entity.Lyric;
import com.widdit.nowplaying.entity.Track;
import com.widdit.nowplaying.util.SongMatchingUtil;
import com.widdit.nowplaying.util.SongUtil;
import com.widdit.nowplaying.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
@Slf4j
public class KuGouMusicService {

    private static final Pattern LRC_TIME_TAG = Pattern.compile(
            "(?m)^\\[\\d{1,2}:\\d{2}(?:\\.\\d{1,3})?]");

    private final KuGouHttpClient httpClient;

    // 缓存相关变量
    private String prevKeyword;
    private KuGouSong prevSong;

    // 锁对象
    private final Object cacheLock = new Object();

    public KuGouMusicService(KuGouHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * 根据关键词搜索歌曲，返回歌曲信息对象
     * @param keyword 关键词
     * @return
     */
    public Track search(String keyword) throws IOException {
        return searchSong(keyword).getTrack();
    }

    public Lyric getLyric(String keyword) throws IOException {
        String[] parsed = SongUtil.parseWindowTitle(keyword);
        String realTitle = parsed[0];
        String realAuthor = parsed[1];

        Lyric lyric = new Lyric();
        lyric.setSource("kugou");
        lyric.setTitle(realTitle);
        lyric.setAuthor(realAuthor);

        try {
            return retrieveLyric(keyword, realTitle, realAuthor, lyric);
        } catch (IOException | RuntimeException exception) {
            log.warn("Kugou lyric retrieval failed: {}", exception.getClass().getSimpleName());
            return lyric;
        }
    }

    private Lyric retrieveLyric(
            String keyword,
            String realTitle,
            String realAuthor,
            Lyric lyric) throws IOException {
        String searchKeyword = keyword;
        KuGouSong song = searchSong(searchKeyword);
        Track track = song.getTrack();

        lyric.setTitle(track.getTitle());
        lyric.setAuthor(track.getAuthor());
        lyric.setDuration(track.getDuration());

        int matchThreshold = getMatchThreshold(realAuthor);
        int trackSimilarity = SongMatchingUtil.calculateSimilarity(
                realTitle, realAuthor, track.getTitle(), track.getAuthor());
        String fallbackKeyword = SongUtil.buildSearchKeywordWithoutAnnotations(keyword);
        if (trackSimilarity < matchThreshold && !fallbackKeyword.equals(keyword)) {
            log.info("完整标题未匹配，改用主标题搜索：{}", fallbackKeyword);
            try {
                KuGouSong fallbackSong = searchSong(fallbackKeyword);
                Track fallbackTrack = fallbackSong.getTrack();
                int fallbackSimilarity = SongMatchingUtil.calculateSimilarity(
                        realTitle, realAuthor, fallbackTrack.getTitle(), fallbackTrack.getAuthor());
                if (fallbackSimilarity > trackSimilarity) {
                    searchKeyword = fallbackKeyword;
                    song = fallbackSong;
                    track = fallbackTrack;
                    trackSimilarity = fallbackSimilarity;
                    lyric.setTitle(track.getTitle());
                    lyric.setAuthor(track.getAuthor());
                    lyric.setDuration(track.getDuration());
                }
            } catch (IOException | RuntimeException exception) {
                log.debug("Kugou fallback song search failed: {}", exception.getClass().getSimpleName());
            }
        }
        if (trackSimilarity < matchThreshold) {
            lyric.setTitle(realTitle);
            lyric.setAuthor(realAuthor);
            return lyric;
        }

        JSONObject candidate = findBestLyricCandidate(
                searchKeyword, song, realTitle, realAuthor, matchThreshold);
        if (candidate == null) {
            return lyric;
        }

        String downloadUrl = UriComponentsBuilder
                .fromHttpUrl("https://lyrics.kugou.com/download")
                .queryParam("ver", 1)
                .queryParam("client", "pc")
                .queryParam("id", candidate.getString("id"))
                .queryParam("accesskey", candidate.getString("accesskey"))
                .queryParam("fmt", "lrc")
                .queryParam("charset", "utf8")
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();
        JSONObject download = JSON.parseObject(httpClient.get(downloadUrl));
        if (download == null) {
            log.warn("Kugou lyric download returned no response");
            return lyric;
        }
        int downloadStatus = download.getIntValue("status");
        if (downloadStatus != 200) {
            log.warn("Kugou lyric download returned status {}", downloadStatus);
            return lyric;
        }
        String content = download.getString("content");
        if (content == null || content.isBlank()) {
            log.debug("Kugou lyric download returned blank content");
            return lyric;
        }

        byte[] decodedContent;
        try {
            decodedContent = Base64.getDecoder().decode(content);
        } catch (IllegalArgumentException exception) {
            log.debug("Kugou lyric download content is not valid Base64");
            return lyric;
        }

        String lrc;
        try {
            lrc = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(decodedContent))
                    .toString();
        } catch (CharacterCodingException exception) {
            log.debug("Kugou lyric download content is not valid UTF-8");
            return lyric;
        }
        if (!LRC_TIME_TAG.matcher(lrc).find()) {
            log.debug("Kugou lyric download content has no timed LRC tag");
            return lyric;
        }

        lyric.setHasLyric(true);
        lyric.setLrc(lrc);
        return lyric;
    }

    private JSONObject findBestLyricCandidate(
            String keyword,
            KuGouSong song,
            String realTitle,
            String realAuthor,
            int matchThreshold) throws IOException {
        Track track = song.getTrack();
        String url = UriComponentsBuilder
                .fromHttpUrl("https://lyrics.kugou.com/search")
                .queryParam("ver", 1)
                .queryParam("man", "yes")
                .queryParam("client", "pc")
                .queryParam("keyword", keyword)
                .queryParam("duration", track.getDuration().longValue() * 1000L)
                .queryParam("hash", song.getFileHash())
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();
        JSONObject response = JSON.parseObject(httpClient.get(url));
        if (response == null) {
            log.warn("Kugou lyric search returned no response");
            return null;
        }
        int searchStatus = response.getIntValue("status");
        if (searchStatus != 200) {
            log.warn("Kugou lyric search returned status {}", searchStatus);
            return null;
        }
        Object candidateValue = response.get("candidates");
        if (!(candidateValue instanceof JSONArray)) {
            log.debug("Kugou lyric search returned missing or invalid candidates");
            return null;
        }
        JSONArray candidates = (JSONArray) candidateValue;
        if (candidates.isEmpty()) {
            log.debug("Kugou lyric search returned no candidates");
            return null;
        }

        JSONObject bestCandidate = null;
        int bestSimilarity = Integer.MIN_VALUE;
        long bestDurationDifference = Long.MAX_VALUE;
        long expectedDurationMillis = track.getDuration().longValue() * 1000L;
        for (int index = 0; index < candidates.size(); index++) {
            Object value = candidates.get(index);
            if (!(value instanceof JSONObject)) {
                log.debug("Skipping invalid Kugou lyric candidate at index {}: not an object", index);
                continue;
            }
            JSONObject candidate = (JSONObject) value;
            Long candidateDuration = validateCandidate(candidate, realAuthor, index);
            if (candidateDuration == null) {
                continue;
            }
            int similarity = SongMatchingUtil.calculateSimilarity(
                    realTitle,
                    realAuthor,
                    candidate.getString("song"),
                    candidate.getString("singer"));
            long durationDifference = getDurationDifference(
                    candidateDuration, expectedDurationMillis);
            if (similarity > bestSimilarity
                    || similarity == bestSimilarity && durationDifference < bestDurationDifference) {
                bestCandidate = candidate;
                bestSimilarity = similarity;
                bestDurationDifference = durationDifference;
            }
        }
        if (bestCandidate == null) {
            log.debug("Kugou lyric search returned no valid candidates");
            return null;
        }
        return bestSimilarity >= matchThreshold ? bestCandidate : null;
    }

    private Long validateCandidate(JSONObject candidate, String realAuthor, int index) {
        if (isBlank(candidate.getString("song"))) {
            log.debug("Skipping invalid Kugou lyric candidate at index {}: missing song", index);
            return null;
        }
        if (isBlank(candidate.getString("id"))) {
            log.debug("Skipping invalid Kugou lyric candidate at index {}: missing id", index);
            return null;
        }
        if (isBlank(candidate.getString("accesskey"))) {
            log.debug("Skipping invalid Kugou lyric candidate at index {}: missing access key", index);
            return null;
        }
        if (!isBlank(realAuthor) && isBlank(candidate.getString("singer"))) {
            log.debug("Skipping invalid Kugou lyric candidate at index {}: missing singer", index);
            return null;
        }

        Object durationValue = candidate.get("duration");
        if (!(durationValue instanceof Number)) {
            log.debug("Skipping invalid Kugou lyric candidate at index {}: invalid duration", index);
            return null;
        }
        try {
            long duration = new BigDecimal(durationValue.toString()).longValueExact();
            if (duration < 0) {
                log.debug("Skipping invalid Kugou lyric candidate at index {}: invalid duration", index);
                return null;
            }
            return duration;
        } catch (ArithmeticException | NumberFormatException exception) {
            log.debug("Skipping invalid Kugou lyric candidate at index {}: invalid duration", index);
            return null;
        }
    }

    private long getDurationDifference(long candidateDuration, long expectedDuration) {
        try {
            return Math.abs(Math.subtractExact(candidateDuration, expectedDuration));
        } catch (ArithmeticException exception) {
            return Long.MAX_VALUE;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private int getMatchThreshold(String realAuthor) {
        return realAuthor == null || realAuthor.isBlank()
                ? 75
                : SongMatchingUtil.EXACT_MATCH_THRESHOLD;
    }

    KuGouSong searchSong(String keyword) throws IOException {
        log.info("获取酷狗音乐歌曲信息..");

        // 尝试从缓存获取 (加锁读取，保证读取到的是完整的一组数据)
        synchronized (cacheLock) {
            if (Objects.equals(keyword, prevKeyword) && prevSong != null) {
                log.info("命中歌曲缓存：" + keyword);
                return prevSong;
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
        String respStr = httpClient.get(url);

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

        KuGouSong song = new KuGouSong(track, bestMatchSong.getString("FileHash"));

        log.info("获取成功");

        // 更新缓存 (加锁写入)
        synchronized (cacheLock) {
            this.prevKeyword = keyword;
            this.prevSong = song;
        }

        return song;
    }

}
