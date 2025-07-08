package com.widdit.nowplaying.service.netease;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.widdit.nowplaying.entity.Track;
import com.widdit.nowplaying.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Service
@Slf4j
public class NeteaseMusicNewService {

    private final String userHome = System.getProperty("user.home");

    private String appVersion;
    private long prevPlaytime = 0;
    private long prevModifiedTime = 0;

    @Autowired
    private NeteaseMusicService neteaseMusicService;

    /**
     * 读取网易云音乐本地文件，获取当前正在播放的歌曲，返回歌曲信息对象
     * @param keyword 关键词
     * @return
     */
    public Track getTrackInfo(String keyword) throws Exception {
        if (appVersion == null) {
            loadAppVersion();
        }

        log.info("获取网易云音乐歌曲信息..");

        Track track;

        try {
            if (appVersion.startsWith("3.")) {  // 新版网易云
                track = getCurrentTrackV3();
            } else if (appVersion.startsWith("2.")) {  // 旧版网易云
                track = getCurrentTrackV2();
            } else {
                throw new RuntimeException("网易云音乐 appVersion 属性有误（appVersion = " + appVersion + "）");
            }
            log.info("获取成功");

        } catch (Exception e) {
            log.error("读取网易云音乐本地文件获取歌曲信息时出错：" + e.getMessage());
            log.info("使用传统方案查询");
            track = neteaseMusicService.search(keyword);
        }

        return track;
    }

    /**
     * 新版网易云获取当前正在播放的歌曲信息
     * @return
     */
    private Track getCurrentTrackV3() throws Exception {
        String filePath = userHome + "\\AppData\\Local\\NetEase\\CloudMusic\\Library\\webdb.dat";

        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + filePath);

            statement = conn.createStatement();
            statement.setQueryTimeout(3);
            statement.execute("PRAGMA journal_mode=WAL;");  // WAL 模式允许多个进程同时操作数据库，防止在查询时网易云无法写入

            String sql = "SELECT playtime, jsonStr FROM historyTracks ORDER BY playtime DESC LIMIT 1";

            rs = statement.executeQuery(sql);

            boolean hasNext = rs.next();
            if (!hasNext) {
                throw new RuntimeException("webdb.dat 数据库文件中没有最近播放歌曲的记录");
            }

            // 为防止网易云本地文件没来得及更新，判断这条记录是否和上次相同，如果相同则等待 500 毫秒再次判断，最多尝试 2 次
            long playtime = rs.getLong("playtime");

            for (int i = 0; i < 2; i++) {
                if (playtime != prevPlaytime) {
                    break;
                }

                rs.close();
                Thread.sleep(500);

                rs = statement.executeQuery(sql);
                rs.next();

                playtime = rs.getLong("playtime");
            }

            if (playtime == prevPlaytime) {
                throw new RuntimeException("webdb.dat 文件中的数据未及时更新");
            }
            prevPlaytime = playtime;

            String jsonStr = rs.getString("jsonStr");
            JSONObject jsonObject = JSONObject.parseObject(jsonStr);

            String id = jsonObject.getString("id");
            Integer duration = jsonObject.getInteger("duration") / 1000;
            String title = jsonObject.getString("name");
            String album = jsonObject.getJSONObject("album").getString("name");
            String cover = jsonObject.getJSONObject("album").getString("picUrl");
            cover += "?param=500y500";  // 图片大小设置为 500*500

            JSONArray artistsArray = jsonObject.getJSONArray("artists");
            String author = "";
            for (int i = 0; i < artistsArray.size(); i++) {
                JSONObject artist = artistsArray.getJSONObject(i);
                if (i > 0) {
                    author += " / ";
                }
                author += artist.getString("name");
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

            return track;
        } catch (Exception e) {
            throw e;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (statement != null) statement.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * 旧版网易云获取当前正在播放的歌曲信息
     * @return
     */
    private Track getCurrentTrackV2() throws Exception {
        // 等待网易云更新 history 文件
        Thread.sleep(1800);

        String filePath = userHome + "\\AppData\\Local\\NetEase\\CloudMusic\\webdata\\file\\history";

        Path path = Paths.get(filePath);

        // 判断文件修改时间戳是否和上次相同，如果相同则等待 100 毫秒再次判断，最多尝试 12 次
        long modifiedTime = Files.getLastModifiedTime(path).toMillis();

        for (int i = 0; i < 12; i++) {
            if (modifiedTime != prevModifiedTime) {
                break;
            }
            Thread.sleep(100);
            modifiedTime = Files.getLastModifiedTime(path).toMillis();
        }

        if (modifiedTime == prevModifiedTime) {
            throw new RuntimeException("history 文件中的数据未及时更新");
        }
        prevModifiedTime = modifiedTime;

        String jsonStr = new String(Files.readAllBytes(path));

        JSONArray jsonArray = JSONArray.parseArray(jsonStr);

        JSONObject song = jsonArray.getJSONObject(0).getJSONObject("track");

        String title = song.getString("name");
        String id = song.getString("id");
        Integer duration = song.getInteger("duration") / 1000;
        String album = song.getJSONObject("album").getString("name");
        String cover = song.getJSONObject("album").getString("picUrl");
        cover += "?param=500y500";  // 图片大小设置为 500*500

        JSONArray artists = song.getJSONArray("artists");
        String author = "";
        for (int i = 0; i < artists.size(); i++) {
            if (i > 0) {
                author += " / ";
            }
            author += artists.getJSONObject(i).getString("name");
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

        return track;
    }

    /**
     * 读取网易云音乐本地文件，设置应用版本号（新版网易云为 3.X.X.XXXXXX，旧版网易云为 2.X.X.XXXXXX）
     */
    private void loadAppVersion() {
        String filePath = userHome + "\\AppData\\Local\\NetEase\\CloudMusic\\dumps\\appParam";

        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject jsonObject = JSON.parseObject(content);
            appVersion = jsonObject.getString("appVersion");
            if (appVersion == null || "".equals(appVersion)) {
                throw new RuntimeException("appParam 文件中不存在 appVersion 属性");
            }
        } catch (Exception e) {
            log.error("读取网易云音乐版本信息失败：" + e.getMessage());
            appVersion = "3.1.3.203419";
        }
    }

}
