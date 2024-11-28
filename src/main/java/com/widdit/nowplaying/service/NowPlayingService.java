package com.widdit.nowplaying.service;

import com.widdit.nowplaying.entity.Player;
import com.widdit.nowplaying.entity.Query;
import com.widdit.nowplaying.entity.Track;
import com.widdit.nowplaying.service.kugou.KuGouMusicService;
import com.widdit.nowplaying.service.kuwo.KuWoMusicService;
import com.widdit.nowplaying.service.netease.NeteaseMusicService;
import com.widdit.nowplaying.service.qq.QQMusicService;
import com.widdit.nowplaying.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NowPlayingService {

    // 播放器信息
    private Player player = new Player();
    // 歌曲信息
    private Track track = new Track();

    // 前一个有效窗口标题
    private String prevWindowTitle = "";
    // 前 1 秒的播放状态
    private String prevStatus = "None";
    // 状态转为 None 时的时间戳（毫秒）
    private long noneOccursTime = 0;

    @Autowired
    private MusicStatusService musicStatusService;
    @Autowired
    private NeteaseMusicService neteaseMusicService;
    @Autowired
    private QQMusicService qqMusicService;
    @Autowired
    private KuGouMusicService kuGouMusicService;
    @Autowired
    private KuWoMusicService kuWoMusicService;

    /**
     * 返回音乐信息
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
        // 获取音乐状态
        String[] lines = musicStatusService.getMusicStatus().split("\n");
        String musicStatus = lines[0];
        String windowTitle = "";
        if (lines.length > 1) {
            windowTitle = lines[1];
        }

        // 在暂停、切歌时，由于系统 IO 调度原因，有概率会出现明明开启了音乐软件但检测结果为 None 的误判情况
        // 为了解决该问题，要求连续 10 秒以上都为 None 才认为是真正关闭了音乐软件
        if ("None".equals(musicStatus)) {
            if ("None".equals(prevStatus)) {
                if (System.currentTimeMillis() - noneOccursTime > 10 * 1000) {
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
            prevStatus = musicStatus;
            return;
        }

        // 当前没有检测到音乐软件
        if ("".equals(musicStatus) || "".equals(windowTitle)) {
            player = new Player();
            track = new Track();
            return;
        }

        player.setHasSong(true);

        String[] split = musicStatus.split(" ");
        String status = split[0];
        String platform = split[1];

        if ("Playing".equals(status)) {
            player.setIsPaused(false);
        } else if ("Paused".equals(status)) {
            player.setIsPaused(true);
        }

        if (windowTitle.equals(prevWindowTitle)) {  // 窗口标题不变，说明没切歌
            if ("Playing".equals(status)) {
                increaseSeekbar();
            }
        } else {  // 窗口标题改变，说明切歌了
            log.info("切换歌曲为：" + windowTitle);
            player.setSeekbarCurrentPosition(0);
            player.setSeekbarCurrentPositionHuman("0:00");
            player.setStatePercent(0.0);

            // 网易云没有周杰伦版权，借用 QQ 音乐搜索
            if ("Netease".equals(platform) && windowTitle.contains("周杰伦")) {
                platform = "QQ";
            }

            try {
                if ("Netease".equals(platform)) {
                    track = neteaseMusicService.search(windowTitle);
                    String cover = neteaseMusicService.getCover(track.getId());
                    track.setCover(cover);
                } else if ("QQ".equals(platform)) {
                    track = qqMusicService.search(windowTitle);
                } else if ("KuGou".equals(platform)) {
                    track = kuGouMusicService.search(windowTitle);
                } else if ("KuWo".equals(platform)) {
                    track = kuWoMusicService.search(windowTitle);
                }

                // 小概率情况下会出现搜到的第一首歌并不是实际播放的歌，这时利用窗口标题去更新歌曲信息，确保至少歌名和作者名是正确的
                if (!windowTitle.contains(track.getAuthor().split("/")[0].trim())) {
                    String[] titleSplit = windowTitle.split("-");
                    track.setTitle(titleSplit[0].trim());
                    track.setAuthor(titleSplit[1].trim());
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
     * 让 player 对象的进度条的秒数加 1
     */
    private void increaseSeekbar() {
        Integer progressSec = player.getSeekbarCurrentPosition();
        Integer duration = track.getDuration();
        if (duration == 0) {  // 防止未知异常情况，不要除以 0 就行
            duration = 5 * 60;
        }
        if (progressSec < 999 * 60) {  // 限制 999 分钟
            progressSec++;
            player.setSeekbarCurrentPosition(progressSec);
            player.setSeekbarCurrentPositionHuman(TimeUtil.getFormattedDuration(progressSec));
            player.setStatePercent((double) progressSec / duration);
        }
    }

}
