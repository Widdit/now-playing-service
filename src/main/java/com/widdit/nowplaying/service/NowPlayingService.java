package com.widdit.nowplaying.service;

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
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
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
    // 前一个是否暂停状态
    private boolean prevIsPaused = true;
    // 状态转为 None 时的时间戳（毫秒）
    private long noneOccursTime = 0;
    // kg 平台上一次的播放进度（秒），用于检测重唱和触发同步
    // -1 表示自然失去进度（C# 端不再报告进度），-2 表示由切歌/重置清零
    private static final int KG_PROGRESS_NONE = -1;
    private static final int KG_PROGRESS_RESET = -2;
    private int prevKgProgressSeconds = KG_PROGRESS_RESET;

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
    @org.springframework.context.annotation.Lazy
    private LyricService lyricService;

    /**
     * 返回歌曲信息
     * @return
     */
    public Query query() {
        // 从计时器中实时获取进度条时间
        int progressSec;
        int duration = track.getDuration();

        // kg 平台使用 C# 端传递的精确进度
        String currentPlatform = getCurrentPlatform();

        if ("kg".equals(currentPlatform) && audioService.getProgressSeconds() >= 0) {
            progressSec = audioService.getProgressSeconds();
            if (audioService.getTotalSeconds() > 0) {
                duration = audioService.getTotalSeconds();
            }
        } else {
            progressSec = Math.toIntExact(stopWatch.getTime(TimeUnit.SECONDS));
        }

        // 防止未知异常情况，不要除以 0 就行
        if (duration <= 0) {
            duration = 5 * 60;
        }

        player.setSeekbarCurrentPosition(progressSec);
        player.setSeekbarCurrentPositionHuman(TimeUtil.getFormattedDuration(progressSec));
        player.setStatePercent((double) progressSec / duration);

        return new Query(player, track);
    }

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
        boolean primaryPlatformRunning = audioService.getPrimaryPlatformRunning();

        // 为防止误判，要求连续 3 秒以上都为 None 才认为是真正关闭了音乐软件
        if ("None".equals(status)) {
            if ("None".equals(prevStatus)) {
                if (System.currentTimeMillis() - noneOccursTime > 3 * 1000) {
                    // 此时认为音乐软件真正被关闭了
                    boolean hadSong = player.getHasSong();
                    player = new Player();
                    track = new Track();
                    stopWatch.reset();
                    prevWindowTitle = "";
                    prevIsPaused = true;
                    prevKgProgressSeconds = KG_PROGRESS_RESET;

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
            // kg 平台：检测重唱（进度大幅回跳）
            if (isKgPlatform()) {
                int kgProgress = audioService.getProgressSeconds();
                if (kgProgress >= 0 && prevKgProgressSeconds > 3 && kgProgress < prevKgProgressSeconds - 2) {
                    log.info("检测到全民K歌重唱/切歌，进度从 {}s 回跳到 {}s", prevKgProgressSeconds, kgProgress);
                    stopWatch.reset();
                    stopWatch.start();
                    eventPublisher.publishEvent(new PlayerProgressReplayEvent(this, "播放器重新播放"));
                    lyricService.forceRefreshLyric();
                }
                // 从自然失去进度恢复到有进度（如关闭演唱窗口后选同名歌曲），强制刷新歌词并清空封面
                // 仅 KG_PROGRESS_NONE（自然失去进度）时触发，KG_PROGRESS_RESET（切歌重置）不触发
                if (kgProgress >= 0 && prevKgProgressSeconds == KG_PROGRESS_NONE) {
                    log.info("检测到全民K歌同名歌曲恢复，强制刷新歌词并清空封面");
                    stopWatch.reset();
                    stopWatch.start();
                    // 同名歌曲切换时窗口标题不变，无法重新搜索，旧封面可能是错的，清空
                    track.setCover("");
                    eventPublisher.publishEvent(new TrackChangedEvent(this, "同名歌曲封面重置"));
                    eventPublisher.publishEvent(new PlayerProgressReplayEvent(this, "播放器重新播放"));
                    lyricService.forceRefreshLyric();
                }
                // C# 秒数变化时，向前端推送进度同步，让前端用 C# 的实际进度校准本地计时器
                if (kgProgress >= 0 && kgProgress != prevKgProgressSeconds) {
                    eventPublisher.publishEvent(new PlayerProgressSyncEvent(this, "kg进度同步"));
                }
                prevKgProgressSeconds = kgProgress;
            }

            // 如果状态是暂停，则让进度条暂停；否则，让进度条前进
            if (isPaused) {
                pauseSeekbar();
            } else {
                advanceSeekbar();
            }
        } else {  // 窗口标题改变（切歌了），需要查询歌曲信息
            stopWatch.reset();
            stopWatch.start();
            prevKgProgressSeconds = KG_PROGRESS_RESET;

            log.info("切换歌曲为：" + windowTitle);

            String platform = getCurrentPlatform();

            try {
                if ("netease".equals(platform)) {
                    // 网易云音乐较为特殊，它实际上不支持 SMTC，但是能够读取本地数据库文件来获取歌曲信息
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
                } else if ("kg".equals(platform)) {
                    // 全民K歌复用 QQ 音乐搜索获取封面/歌曲信息
                    track = qqMusicService.search(windowTitle);
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
                // 使用窗口标题去覆盖歌曲信息，保证歌名、歌手名和音乐软件中的完全一致
                String[] parseResult = SongUtil.parseWindowTitle(windowTitle);
                track.setTitle(parseResult[0]);
                track.setAuthor(parseResult[1]);
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
     * 返回进度条毫秒值
     * @return
     */
    public QueryProgress queryProgress() {
        // kg 平台使用 C# 端传递的精确进度
        String currentPlatform = getCurrentPlatform();

        if ("kg".equals(currentPlatform) && audioService.getProgressSeconds() >= 0) {
            return new QueryProgress(audioService.getProgressSeconds() * 1000L);
        }
        return new QueryProgress(stopWatch.getTime());
    }

    /**
     * 返回播放器信息
     * @return
     */
    public Player queryPlayer() {
        // 从计时器中实时获取进度条时间
        int progressSec;
        int duration = track.getDuration();

        // kg 平台使用 C# 端传递的精确进度
        String currentPlatform = getCurrentPlatform();

        if ("kg".equals(currentPlatform) && audioService.getProgressSeconds() >= 0) {
            progressSec = audioService.getProgressSeconds();
            if (audioService.getTotalSeconds() > 0) {
                duration = audioService.getTotalSeconds();
            }
        } else {
            progressSec = Math.toIntExact(stopWatch.getTime(TimeUnit.SECONDS));
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
        // 如果通用设置被修改，则将歌曲信息和 prevWindowTitle 清空
        player = new Player();
        track = new Track();
        prevWindowTitle = "";
        prevKgProgressSeconds = KG_PROGRESS_RESET;
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
        otherPlatforms.put("kg", "全民K歌");
    }

    /**
     * 进度条前进
     */
    private void advanceSeekbar() {
        // 如果没启动过（UNSTARTED），播放时应启动
        if (!stopWatch.isStarted()) {
            stopWatch.start();
        } else if (stopWatch.isSuspended()) {
            stopWatch.resume();
        }

        long progressMs = stopWatch.getTime();
        long durationMs = track.getDuration() * 1000;

        // 一般发生在单曲循环情况下
        if (progressMs >= durationMs - 300 && durationMs > 0) {
            stopWatch.reset();
            stopWatch.start();

            // 发布事件，通知变化
            eventPublisher.publishEvent(new PlayerProgressReplayEvent(this, "播放器重新播放"));
        }
    }

    /**
     * 进度条暂停
     */
    private void pauseSeekbar() {
        // StopWatch 只有在 RUNNING 时才能 suspend，否则会抛出异常
        // RUNNING 状态的判定：started && !stopped && !suspended
        if (stopWatch.isStarted() && !stopWatch.isStopped() && !stopWatch.isSuspended()) {
            stopWatch.suspend();
        }
    }

    /**
     * 获取当前实际生效的平台（考虑 fallback 逻辑）
     * @return 当前平台标识符
     */
    private String getCurrentPlatform() {
        SettingsGeneral settings = settingsService.getSettingsGeneral();
        String platform = settings.getPlatform();
        if (settings.getFallbackPlatformEnabled() && !audioService.getPrimaryPlatformRunning()) {
            platform = settings.getFallbackPlatform();
        }
        return platform;
    }

    /**
     * 当前平台是否为全民K歌
     */
    private boolean isKgPlatform() {
        return "kg".equals(getCurrentPlatform());
    }


}
