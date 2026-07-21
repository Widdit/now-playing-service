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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
@Slf4j
public class KuGouMusicService {

    private static final Pattern LRC_TIME_TAG = Pattern.compile(
            "(?m)^\\[\\d{2}:\\d{2}(?:\\.\\d{2})?]");

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
        KuGouSong song = searchSong(keyword);
        Track track = song.getTrack();

        Lyric lyric = new Lyric();
        lyric.setSource("kugou");
        lyric.setTitle(track.getTitle());
        lyric.setAuthor(track.getAuthor());
        lyric.setDuration(track.getDuration());

        int matchThreshold = getMatchThreshold(realAuthor);
        int trackSimilarity = SongMatchingUtil.calculateSimilarity(
                realTitle, realAuthor, track.getTitle(), track.getAuthor());
        if (trackSimilarity < matchThreshold) {
            lyric.setTitle(realTitle);
            lyric.setAuthor(realAuthor);
            return lyric;
        }

        JSONObject candidate = findBestLyricCandidate(
                keyword, song, realTitle, realAuthor, matchThreshold);
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
        if (download == null || download.getIntValue("status") != 200) {
            return lyric;
        }
        String content = download.getString("content");
        if (content == null || content.isBlank()) {
            return lyric;
        }

        String lrc;
        try {
            lrc = new String(Base64.getDecoder().decode(content), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            return lyric;
        }
        if (!LRC_TIME_TAG.matcher(lrc).find()) {
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
                .queryParam("duration", track.getDuration() * 1000)
                .queryParam("hash", song.getFileHash())
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();
        JSONObject response = JSON.parseObject(httpClient.get(url));
        if (response == null || response.getIntValue("status") != 200) {
            return null;
        }
        JSONArray candidates = response.getJSONArray("candidates");
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        JSONObject bestCandidate = null;
        int bestSimilarity = Integer.MIN_VALUE;
        int bestDurationDifference = Integer.MAX_VALUE;
        for (int index = 0; index < candidates.size(); index++) {
            JSONObject candidate = candidates.getJSONObject(index);
            int similarity = SongMatchingUtil.calculateSimilarity(
                    realTitle,
                    realAuthor,
                    candidate.getString("song"),
                    candidate.getString("singer"));
            int durationDifference = Math.abs(
                    candidate.getIntValue("duration") - track.getDuration() * 1000);
            if (similarity > bestSimilarity
                    || similarity == bestSimilarity && durationDifference < bestDurationDifference) {
                bestCandidate = candidate;
                bestSimilarity = similarity;
                bestDurationDifference = durationDifference;
            }
        }
        return bestSimilarity >= matchThreshold ? bestCandidate : null;
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
