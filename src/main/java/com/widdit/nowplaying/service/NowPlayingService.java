package com.widdit.nowplaying.service;

import com.widdit.nowplaying.entity.Player;
import com.widdit.nowplaying.entity.Query;
import com.widdit.nowplaying.entity.SettingsGeneral;
import com.widdit.nowplaying.entity.Track;
import com.widdit.nowplaying.event.SettingsGeneralChangeEvent;
import com.widdit.nowplaying.service.kugou.KuGouMusicService;
import com.widdit.nowplaying.service.kuwo.KuWoMusicService;
import com.widdit.nowplaying.service.netease.NeteaseMusicService;
import com.widdit.nowplaying.service.qq.QQMusicService;
import com.widdit.nowplaying.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class NowPlayingService {

    // 播放器信息
    private Player player = new Player();
    // 歌曲信息
    private Track track = new Track();

    // 前一个窗口标题
    private String prevWindowTitle = "";
    // 前 1 秒的播放状态
    private String prevStatus = "None";
    // 状态转为 None 时的时间戳（毫秒）
    private long noneOccursTime = 0;

    private Map<String, String> otherPlatforms = new HashMap<>();
    private Set<String> localPlayers = new HashSet<>();

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

    /**
     * 返回歌曲信息
     * @return
     */
    public Query query() {
        return new Query(player, track);
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
            // 如果是播放状态，则增加进度条秒数
            if ("Playing".equals(status)) {
                increaseSeekbar();
            }
        } else {  // 窗口标题改变（切歌了），需要查询歌曲信息
            log.info("切换歌曲为：" + windowTitle);
            player.setSeekbarCurrentPosition(0);
            player.setSeekbarCurrentPositionHuman("0:00");
            player.setStatePercent(0.0);

            String originalPlatform = settings.getPlatform();
            String platform = originalPlatform;

            // 如果是其它平台，借用网易云音乐搜索
            if (otherPlatforms.containsKey(originalPlatform)) {
                log.info("当前平台为：" + otherPlatforms.get(originalPlatform) + "，借用网易云音乐搜索");
                platform = "netease";
            }

            // 网易云没有周杰伦版权，借用 QQ 音乐搜索
            if ("netease".equals(platform) && (windowTitle.contains("周杰伦") || windowTitle.contains("周杰倫"))) {
                log.info("当前歌手为周杰伦，借用 QQ 音乐搜索");
                platform = "qq";
            }

            try {
                if ("netease".equals(platform)) {
                    track = neteaseMusicService.search(windowTitle);
                } else if ("qq".equals(platform)) {
                    track = qqMusicService.search(windowTitle);
                } else if ("kugou".equals(platform)) {
                    track = kuGouMusicService.search(windowTitle);
                } else if ("kuwo".equals(platform)) {
                    track = kuWoMusicService.search(windowTitle);
                }

                if (!localPlayers.contains(originalPlatform)) {
                    // 小概率情况下会出现搜到的歌并不是实际播放的歌，这时利用窗口标题去更新歌曲信息，确保至少歌名和作者名是正确的
                    if (!windowTitle.contains(track.getAuthor().split("/")[0].trim())) {
                        String[] titleSplit = windowTitle.split("-");
                        track.setTitle(titleSplit[0].trim());
                        track.setAuthor(titleSplit[1].trim());
                    }
                }
            } catch (Exception e) {
                log.info("获取失败");
                // 查询歌曲失败，此情况下利用窗口标题去更新歌曲信息
                String[] titleSplit = windowTitle.split("-");
                track = Track.builder()
                        .author(titleSplit[1].trim())
                        .title(titleSplit[0].trim())
                        .album(titleSplit[0].trim())
                        .cover("https://gitee.com/widdit/now-playing/raw/master/spotify_no_cover.jpg")
                        .duration(5 * 60)
                        .durationHuman("5:00")
                        .url("https://music.youtube.com/watch?v=dQw4w9WgXcQ")
                        .build();
            }
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
     * 初始化操作。该方法会在该类被 Spring 创建时自动执行
     */
    @PostConstruct
    public void init() {
        otherPlatforms.put("spotify", "Spotify");
        otherPlatforms.put("ayna", "卡西米尔点歌机");
        otherPlatforms.put("apple", "Apple Music");
        otherPlatforms.put("potplayer", "PotPlayer");
        otherPlatforms.put("foobar", "Foobar2000");
        otherPlatforms.put("lx", "洛雪音乐");

        localPlayers.add("potplayer");
        localPlayers.add("foobar");
    }

    /**
     * 让 player 对象的进度条的秒数加 1
     */
    private void increaseSeekbar() {
        Integer progressSec = player.getSeekbarCurrentPosition();
        Integer duration = track.getDuration();

        if (duration == 0) {  // 防止未知异常情况，不要除以 0 就行
            duration = 5 * 60;
        }

        if (progressSec >= duration) {  // 一般发生在单曲循环的情况下
            player.setSeekbarCurrentPosition(0);
            player.setSeekbarCurrentPositionHuman("0:00");
            player.setStatePercent(0.0);
            return;
        }

        progressSec++;
        player.setSeekbarCurrentPosition(progressSec);
        player.setSeekbarCurrentPositionHuman(TimeUtil.getFormattedDuration(progressSec));
        player.setStatePercent((double) progressSec / duration);
    }

}
