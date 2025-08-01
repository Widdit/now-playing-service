package com.widdit.nowplaying.service;

import com.widdit.nowplaying.entity.*;
import com.widdit.nowplaying.event.SettingsGeneralChangeEvent;
import com.widdit.nowplaying.service.kugou.KuGouMusicService;
import com.widdit.nowplaying.service.kuwo.KuWoMusicService;
import com.widdit.nowplaying.service.netease.NeteaseMusicNewService;
import com.widdit.nowplaying.service.netease.NeteaseMusicService;
import com.widdit.nowplaying.service.qq.QQMusicService;
import com.widdit.nowplaying.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class NowPlayingService {

    // 播放器信息
    private Player player = new Player();
    // 歌曲信息
    private Track track = new Track();

    // 计时器
    private StopWatch stopWatch = new StopWatch();

    // 前一个窗口标题
    private String prevWindowTitle = "";
    // 前 1 秒的播放状态
    private String prevStatus = "None";
    // 状态转为 None 时的时间戳（毫秒）
    private long noneOccursTime = 0;

    private Map<String, String> otherPlatforms = new HashMap<>();

    @Autowired
    private AudioService audioService;
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private NeteaseMusicService neteaseMusicService;
    @Autowired
    private QQMusicService qqMusicService;
    @Autowired
    private KuGouMusicService kuGouMusicService;
    @Autowired
    private KuWoMusicService kuWoMusicService;
    @Autowired
    private NeteaseMusicNewService neteaseMusicNewService;
    @Autowired
    private OutputService outputService;

    /**
     * 返回歌曲信息
     * @return
     */
    public Query query() {
        // 从计时器中实时获取进度条时间
        int progressSec = Math.toIntExact(stopWatch.getTime(TimeUnit.SECONDS));
        int duration = track.getDuration();

        if (duration == 0) {  // 防止未知异常情况，不要除以 0 就行
            duration = 5 * 60;
        }

        player.setSeekbarCurrentPosition(progressSec);
        player.setSeekbarCurrentPositionHuman(TimeUtil.getFormattedDuration(progressSec));
        player.setStatePercent((double) progressSec / duration);

        return new Query(player, track);
    }

    /**
     * 返回进度条毫秒值
     * @return
     */
    public QueryProgress queryProgress() {
        return new QueryProgress(stopWatch.getTime());
    }

    /**
     * 定时任务，用于更新音乐信息
     * 每隔 1 秒执行一次
     */
    @Scheduled(cron = "0/1 * * * * ?")
    public void updateMusicInfo() {
        SettingsGeneral settings = settingsService.getSettingsGeneral();

        // 获取音乐状态
        String status = audioService.getStatus();
        String windowTitle = audioService.getWindowTitle();

        // 为防止误判，要求连续 6 秒以上都为 None 才认为是真正关闭了音乐软件
        if ("None".equals(status)) {
            if ("None".equals(prevStatus)) {
                if (System.currentTimeMillis() - noneOccursTime > 6 * 1000) {
                    // 此时认为音乐软件真正被关闭了
                    player = new Player();
                    track = new Track();
                } else {
                    // 这种情况下乐观地认为音乐软件依然存在，更新进度条
                    increaseSeekbar();
                }
            } else {
                // 状态转为 None
                noneOccursTime = System.currentTimeMillis();
                // 这种情况下乐观地认为音乐软件依然存在，更新进度条
                increaseSeekbar();
            }
            prevStatus = status;
            return;
        }

        player.setHasSong(true);

        if ("Playing".equals(status)) {
            player.setIsPaused(false);
        } else if ("Paused".equals(status)) {
            player.setIsPaused(true);
        }

        if (windowTitle.equals(prevWindowTitle)) {  // 窗口标题不变（没切歌），无需查询歌曲信息
            // 如果状态是播放中，则增加进度条秒数；否则，暂停进度条
            if ("Playing".equals(status)) {
                increaseSeekbar();
            } else if ("Paused".equals(status)) {
                pauseSeekbar();
            }
        } else {  // 窗口标题改变（切歌了），需要查询歌曲信息
            log.info("切换歌曲为：" + windowTitle);
            stopWatch.reset();
            stopWatch.start();

            String platform = settings.getPlatform();

            try {
                if ("netease".equals(platform)) {
                    // 网易云音乐较为特殊，它实际上不支持 SMTC，但是能够读取本地数据文件来获取歌曲信息
                    if (settings.getSmtc()) {
                        track = neteaseMusicNewService.getTrackInfo(windowTitle);
                    } else {
                        track = neteaseMusicService.search(windowTitle);
                    }
                } else if ("qq".equals(platform)) {
                    track = qqMusicService.search(windowTitle);
                } else if ("kugou".equals(platform)) {
                    track = kuGouMusicService.search(windowTitle);
                } else if ("kuwo".equals(platform)) {
                    track = kuWoMusicService.search(windowTitle);
                } else {
                    log.info("当前平台为：" + otherPlatforms.get(platform) + "，借用网易云音乐搜索");
                    track = neteaseMusicService.search(windowTitle);
                }
            } catch (Exception e) {
                log.error("获取失败：" + e.getMessage());
                track = Track.builder()
                        .author("")
                        .title("")
                        .album("")
                        .cover("https://gitee.com/widdit/now-playing/raw/master/spotify_no_cover.jpg")
                        .duration(5 * 60)
                        .durationHuman("5:00")
                        .url("https://music.youtube.com/watch?v=dQw4w9WgXcQ")
                        .build();
            } finally {
                // Apple Music 的歌手名有时会包含专辑名，需要去除
                if ("apple".equals(platform)) {
                    int dashIndex = windowTitle.indexOf("—");
                    if (dashIndex != -1) {
                        windowTitle = windowTitle.substring(0, dashIndex);
                    }
                }

                // 使用窗口标题去覆盖歌曲信息，保证歌名、歌手名和音乐软件中的完全一致
                String pivot = " - ";
                if (windowTitle.contains(pivot)) {
                    int pos = windowTitle.lastIndexOf(pivot);

                    String title = windowTitle.substring(0, pos).trim();
                    String author = windowTitle.substring(pos + pivot.length()).trim();

                    track.setTitle(title);
                    track.setAuthor(author);
                } else {
                    track.setTitle(windowTitle);
                    track.setAuthor(" ");
                }
            }

            // 输出歌曲信息
            outputService.output(track);
        }

        prevWindowTitle = windowTitle;
        prevStatus = status;
    }

    /**
     * 监听通用设置被修改的事件
     * @param event
     */
    @EventListener
    public void handleSettingsGeneralChange(SettingsGeneralChangeEvent event) {
        // 如果通用设置被修改，则将歌曲信息和 prevWindowTitle 清空
        player = new Player();
        track = new Track();
        prevWindowTitle = "";
    }

    /**
     * 初始化操作。该方法会在该类实例被 Spring 创建时自动执行
     */
    @PostConstruct
    public void init() {
        otherPlatforms.put("spotify", "Spotify");
        otherPlatforms.put("ayna", "卡西米尔唱片机");
        otherPlatforms.put("apple", "Apple Music");
        otherPlatforms.put("potplayer", "PotPlayer");
        otherPlatforms.put("foobar", "Foobar2000");
        otherPlatforms.put("lx", "洛雪音乐");
        otherPlatforms.put("soda", "汽水音乐");
        otherPlatforms.put("huahua", "花花直播助手");
        otherPlatforms.put("musicfree", "MusicFree");
        otherPlatforms.put("bq", "BQ点歌姬");
    }

    /**
     * 进度条前进
     */
    private void increaseSeekbar() {
        if (stopWatch.isSuspended()) {
            stopWatch.resume();
        }

        int progressSec = Math.toIntExact(stopWatch.getTime(TimeUnit.SECONDS));
        int duration = track.getDuration();

        if (progressSec >= duration) {  // 一般发生在单曲循环的情况下
            stopWatch.reset();
            stopWatch.start();
        }
    }

    /**
     * 进度条暂停
     */
    private void pauseSeekbar() {
        if (!stopWatch.isSuspended()) {
            stopWatch.suspend();
        }
    }

}
