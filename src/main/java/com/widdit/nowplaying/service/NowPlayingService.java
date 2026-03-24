package com.widdit.nowplaying.service;

import com.widdit.nowplaying.component.Timer;
import com.widdit.nowplaying.entity.*;
import com.widdit.nowplaying.event.*;
import com.widdit.nowplaying.service.kugou.KuGouMusicService;
import com.widdit.nowplaying.service.kuwo.KuWoMusicService;
import com.widdit.nowplaying.service.netease.NeteaseMusicNewService;
import com.widdit.nowplaying.service.netease.NeteaseMusicService;
import com.widdit.nowplaying.service.qq.QQMusicService;
import com.widdit.nowplaying.util.SongUtil;
import com.widdit.nowplaying.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class NowPlayingService {

    // 播放器信息
    private Player player = new Player();
    // 歌曲信息
    private Track track = new Track();

    // 计时器
    private Timer timer = new Timer();

    // 前一个窗口标题
    private String prevWindowTitle = "";
    // 前 1 秒的播放状态
    private String prevStatus = "None";
    // 前一个是否暂停状态
    private boolean prevIsPaused = true;
    // 状态转为 None 时的时间戳（毫秒）
    private long noneOccursTime = 0;

    // C# 端上一次的播放进度（秒），用于检测重唱和触发同步（-1 表示自然失去进度（C# 端不再报告进度），-2 表示由切歌/重置清零）
    private static final int CSHARP_PROGRESS_NONE = -1;
    private static final int CSHARP_PROGRESS_RESET = -2;
    private int prevCSharpProgressSeconds = CSHARP_PROGRESS_RESET;

    private final Map<String, String> otherPlatforms = new HashMap<>();

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
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    @Lazy
    private LyricService lyricService;

    /**
     * 监听音乐状态被更新的事件
     * @param event
     */
    @EventListener
    public void updateMusicInfo(MusicStatusUpdatedEvent event) {
        SettingsGeneral settings = settingsService.getSettingsGeneral();

        // 获取音乐状态
        String status = audioService.getStatus();
        String windowTitle = audioService.getWindowTitle();

        // 为防止误判，要求连续 3 秒以上都为 None 才认为是真正关闭了音乐软件
        if ("None".equals(status)) {
            if ("None".equals(prevStatus)) {
                if (System.currentTimeMillis() - noneOccursTime > 3 * 1000) {
                    // 此时认为音乐软件真正被关闭了
                    boolean hadSong = player.getHasSong();
                    player = new Player();
                    track = new Track();
                    timer.reset();
                    prevWindowTitle = "";
                    prevIsPaused = true;
                    prevCSharpProgressSeconds = CSHARP_PROGRESS_RESET;

                    // 如果之前有歌曲，发布事件通知前端清除歌词和播放状态
                    if (hadSong) {
                        eventPublisher.publishEvent(new TrackChangedEvent(this, "音乐软件已关闭"));
                        eventPublisher.publishEvent(new PlayerPauseStateChangedEvent(this, "播放器暂停状态改变"));
                    }
                } else {
                    // 这种情况下乐观地认为音乐软件依然存在，更新进度条
                    advanceSeekbar();
                }
            } else {
                // 状态转为 None
                noneOccursTime = System.currentTimeMillis();
                // 这种情况下乐观地认为音乐软件依然存在，更新进度条
                advanceSeekbar();
            }
            prevStatus = status;
            return;
        }

        player.setHasSong(true);

        boolean isPaused = !"Playing".equals(status);
        player.setIsPaused(isPaused);

        if (prevIsPaused != isPaused) {
            // 发布事件，通知变化
            eventPublisher.publishEvent(new PlayerPauseStateChangedEvent(this, "播放器暂停状态改变"));
        }

        if (windowTitle.equals(prevWindowTitle)) {  // 窗口标题不变（没切歌），无需查询歌曲信息
            // 处理 C# 端精确进度更新（检测重唱、同名歌曲恢复、进度同步）
            handleCSharpProgressUpdate();

            // 如果状态是暂停，则让进度条暂停；否则，让进度条前进
            if (isPaused) {
                pauseSeekbar();
            } else {
                advanceSeekbar();
            }
        } else {  // 窗口标题改变（切歌了），需要查询歌曲信息
            timer.reset();
            timer.start();
            prevCSharpProgressSeconds = CSHARP_PROGRESS_RESET;

            log.info("切换歌曲为：" + windowTitle);

            String platform = audioService.getCurrentPlatform();

            try {
                Track searchedTrack;

                if ("netease".equals(platform)) {
                    // 网易云音乐较为特殊，它实际上不支持 SMTC，但是能够读取本地数据库文件来获取歌曲信息
                    if (settings.getSmtc()) {
                        searchedTrack = neteaseMusicNewService.getTrackInfo(windowTitle);
                    } else {
                        searchedTrack = neteaseMusicService.search(windowTitle);
                    }
                } else if ("qq".equals(platform)) {
                    searchedTrack = qqMusicService.search(windowTitle);
                } else if ("kugou".equals(platform)) {
                    searchedTrack = kuGouMusicService.search(windowTitle);
                } else if ("kuwo".equals(platform)) {
                    searchedTrack = kuWoMusicService.search(windowTitle);
                } else if ("wesing".equals(platform)) {
                    searchedTrack = qqMusicService.search(windowTitle);
                } else {
                    log.info("当前平台为：" + otherPlatforms.get(platform) + "，借用网易云音乐搜索");
                    searchedTrack = neteaseMusicService.search(windowTitle);
                }

                track = copyTrack(searchedTrack);

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
                // 使用窗口标题去覆盖歌曲信息，保证歌名、歌手名和音乐软件中的完全一致
                String[] parseResult = SongUtil.parseWindowTitle(windowTitle);

                if (parseResult[0] != null && !parseResult[0].isBlank()) {
                    track.setTitle(parseResult[0]);
                }

                if (parseResult[1] != null && !parseResult[1].isBlank()) {
                    track.setAuthor(parseResult[1]);
                }
            }

            // 发布事件，通知变化
            eventPublisher.publishEvent(new TrackChangedEvent(this, "歌曲发生改变"));

            // 输出歌曲信息
            outputService.outputAsync(track);
        }

        prevWindowTitle = windowTitle;
        prevStatus = status;
        prevIsPaused = isPaused;
    }

    /**
     * 获取歌曲当前播放进度的毫秒值（统一的进度读取入口）
     * @return
     */
    public long getCurrentProgressMs() {
        return timer.getTime();
    }

    /**
     * 返回歌曲信息
     * @return
     */
    public Query query() {
        return new Query(queryPlayer(), track);
    }

    /**
     * 返回播放器信息
     * @return
     */
    public Player queryPlayer() {
        // 从计时器中实时获取进度条时间
        long progressMs = getCurrentProgressMs();
        int progressSec = (int) (progressMs / 1000);
        int duration = track.getDuration();

        // 如果 C# 端提供了歌曲总时长，则使用 C# 端的值
        if (audioService.getTotalSeconds() > 0) {
            duration = audioService.getTotalSeconds();
        }

        // 防止未知异常情况，不要除以 0 就行
        if (duration <= 0) {
            duration = 5 * 60;
        }

        player.setSeekbarCurrentPosition(progressSec);
        player.setSeekbarCurrentPositionHuman(TimeUtil.getFormattedDuration(progressSec));
        player.setStatePercent((double) progressSec / duration);

        return player;
    }

    /**
     * 返回歌曲信息
     * @return
     */
    public Track queryTrack() {
        return track;
    }

    /**
     * 返回进度条毫秒值
     * @return
     */
    public QueryProgress queryProgress() {
        return new QueryProgress(getCurrentProgressMs());
    }

    /**
     * 获取当前是否有歌曲
     * @return
     */
    public RespData<Boolean> hasSong() {
        return new RespData<>(player.getHasSong());
    }

    /**
     * 获取是否成功连接平台
     * @return
     */
    public RespData<Boolean> isConnected() {
        return new RespData<>(player.getHasSong());
    }

    /**
     * 监听通用设置被修改的事件
     * @param event
     */
    @EventListener
    public void handleSettingsGeneralChange(SettingsGeneralChangedEvent event) {
        // 清空歌曲状态
        player = new Player();
        track = new Track();
        prevWindowTitle = "";
        prevCSharpProgressSeconds = CSHARP_PROGRESS_RESET;
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
        otherPlatforms.put("aimp", "AIMP");
        otherPlatforms.put("youtube", "YouTube Music");
        otherPlatforms.put("miebo", "咩播");
        otherPlatforms.put("yesplay", "YesPlayMusic");
        otherPlatforms.put("cider", "Cider");
    }

    /**
     * 处理 C# 端精确进度的更新（检测重唱、同名歌曲恢复、进度同步）
     * 适用于所有支持 C# 端精确进度的平台，对于不提供精确进度的平台会自动跳过
     */
    private void handleCSharpProgressUpdate() {
        int csharpProgress = audioService.getProgressSeconds();

        // 检测重唱（进度大幅回跳）
        if (csharpProgress >= 0 && prevCSharpProgressSeconds > 3 && csharpProgress < prevCSharpProgressSeconds - 2) {
            log.info("检测到重唱/切歌，进度从 {}s 回跳到 {}s", prevCSharpProgressSeconds, csharpProgress);
            timer.reset();
            timer.start();
            eventPublisher.publishEvent(new PlayerProgressReplayEvent(this, "播放器重新播放"));
            lyricService.forceRefreshLyric();
        }

        // 从自然失去进度恢复到有进度（如关闭演唱窗口后选同名歌曲），强制刷新歌词并清空封面
        // 仅 CSHARP_PROGRESS_NONE（自然失去进度）时触发，CSHARP_PROGRESS_RESET（切歌重置）不触发
        if (csharpProgress >= 0 && prevCSharpProgressSeconds == CSHARP_PROGRESS_NONE) {
            log.info("检测到同名歌曲恢复，强制刷新歌词并清空封面");
            timer.reset();
            timer.start();
            // 同名歌曲切换时窗口标题不变，无法重新搜索，旧封面可能是错的，清空
            track.setCover("");
            eventPublisher.publishEvent(new TrackChangedEvent(this, "同名歌曲封面重置"));
            eventPublisher.publishEvent(new PlayerProgressReplayEvent(this, "播放器重新播放"));
            lyricService.forceRefreshLyric();
        }

        // C# 秒数变化时，用 C# 的实际进度校准计时器，并向前端推送进度同步
        if (csharpProgress >= 0 && csharpProgress != prevCSharpProgressSeconds) {
            timer.setTime(csharpProgress * 1000L);
            eventPublisher.publishEvent(new PlayerProgressSyncEvent(this, "播放器进度同步"));
        }

        prevCSharpProgressSeconds = csharpProgress;
    }

    /**
     * 进度条前进
     */
    private void advanceSeekbar() {
        if (!timer.isRunning()) {
            timer.start();
        }

        long progressMs = timer.getTime();
        long durationMs = track.getDuration() * 1000L;

        // 一般发生在单曲循环情况下
        if (progressMs >= durationMs - 300 && durationMs > 0) {
            timer.reset();
            timer.start();

            // 发布事件，通知变化
            eventPublisher.publishEvent(new PlayerProgressReplayEvent(this, "播放器重新播放"));
        }
    }

    /**
     * 进度条暂停
     */
    private void pauseSeekbar() {
        if (timer.isRunning()) {
            timer.pause();
        }
    }

    /**
     * 复制 Track 对象（浅拷贝）
     * @param source
     * @return
     */
    private Track copyTrack(Track source) {
        if (source == null) {
            return null;
        }
        Track target = new Track();
        BeanUtils.copyProperties(source, target);
        return target;
    }

}