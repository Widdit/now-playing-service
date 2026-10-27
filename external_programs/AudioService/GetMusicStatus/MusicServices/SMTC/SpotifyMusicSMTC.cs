using System;
using WindowsMediaController;
using CSCore.CoreAudioAPI;
using Windows.Media.Control;

public class SpotifyMusicSMTC : MusicService
{
    private MediaManager mediaManager;
    private bool hasSession = false;
    private string sessionId;
    private string prevTitle = "";
    private string prevArtist = "";
    // 最近一次切歌的时间，用来丢弃切歌瞬间仍属于上一首的时间轴
    private DateTimeOffset trackChangedAt = DateTimeOffset.MinValue;

    public override void Init()
    {
        mediaManager = new MediaManager();

        mediaManager.OnAnySessionOpened += MediaManager_OnAnySessionOpened;
        mediaManager.OnAnySessionClosed += MediaManager_OnAnySessionClosed;

        mediaManager.Start();
    }

    public override string GetMusicStatus(AudioSessionManager2 sessionManager)
    {
        if (!hasSession)
        {
            return "None";
        }

        // 获取媒体会话
        var mediaSessions = mediaManager.CurrentMediaSessions;
        var mediaSession = mediaSessions[sessionId];
        if (mediaSession == null)
        {
            return "None";
        }

        string status = null;
        string title = null;
        string artist = null;
        string progress = null;

        try
        {
            // 获取播放状态
            // 对于 Spotify，如果不打开 Windows 的媒体控制面板，则 SMTC 中的播放状态可能不会刷新，因此还是需要通过检测音量来判断播放/暂停
            status = GetVolume(sessionManager) > 0.00001 ? "Playing" : "Paused";

            // 获取歌曲信息
            var songInfo = mediaSession.ControlSession.TryGetMediaPropertiesAsync().GetAwaiter().GetResult();
            if (songInfo != null)
            {
                title = songInfo.Title;
                artist = songInfo.Artist;

                // 检测到切歌时，在后台重新拉取并校验封面，确认内容已更新后再保存。
                // SMTC 的标题与封面由播放器异步上报，如果直接使用此刻的 songInfo.Thumbnail，
                // 可能会拿到尚未刷新的旧封面，因此交由 UpdateThumbnailAsync 处理。
                if (title != prevTitle || artist != prevArtist)
                {
                    trackChangedAt = DateTimeOffset.Now;
                    var controlSession = mediaSession.ControlSession;
                    ThumbnailHelper.UpdateThumbnailAsync(() =>
                    {
                        try
                        {
                            var latestSongInfo = controlSession.TryGetMediaPropertiesAsync().GetAwaiter().GetResult();
                            return latestSongInfo?.Thumbnail;
                        }
                        catch (Exception)
                        {
                            return null;
                        }
                    });
                }

                prevTitle = title;
                prevArtist = artist;
            }

            progress = GetProgress(mediaSession.ControlSession, status == "Playing");
        }
        catch (Exception)
        {
            return "None";
        }

        if (string.IsNullOrEmpty(title))
        {
            return "None";
        }

        // 输出结果
        return progress == null
            ? $"{status}\r\n{title + " - " + artist}"
            : $"{status}\r\n{title + " - " + artist}\r\n{progress}";
    }

    /// <summary>
    /// 从 SMTC 时间轴读取 Spotify 的真实进度，返回 "Progress:当前秒|总秒"，读不到时返回 null。
    ///
    /// Spotify 在播放中约每 4.5 秒、以及拖动进度条、切歌、暂停时更新一次时间轴（Position + LastUpdatedTime），
    /// 两次更新之间按 LastUpdatedTime 往后推算。这样拖动进度条后 1 秒内就能把新进度交给服务端，
    /// 服务端据此校准计时器并推送进度同步（与网易云的 UI Automation 进度共用同一条通道）。
    /// </summary>
    private string GetProgress(GlobalSystemMediaTransportControlsSession controlSession, bool audible)
    {
        try
        {
            var timeline = controlSession.GetTimelineProperties();
            double totalSec = timeline.EndTime.TotalSeconds;
            if (totalSec <= 0)
            {
                return null;
            }

            // 切歌后 Spotify 会很快重新上报时间轴；在那之前读到的仍是上一首的进度，不能输出
            if (timeline.LastUpdatedTime.AddSeconds(1) < trackChangedAt)
            {
                return null;
            }

            // 音量判断在安静段落会误判为暂停，所以只要 SMTC 或音量任一认为在播放，就继续推算
            bool playing = audible
                || controlSession.GetPlaybackInfo()?.PlaybackStatus == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Playing;
            double currentSec = timeline.Position.TotalSeconds;
            if (playing)
            {
                currentSec += (DateTimeOffset.Now - timeline.LastUpdatedTime).TotalSeconds;
            }
            currentSec = Math.Max(0, Math.Min(totalSec, currentSec));

            return $"Progress:{(int)currentSec}|{(int)Math.Round(totalSec)}";
        }
        catch (Exception)
        {
            return null;
        }
    }

    private void MediaManager_OnAnySessionOpened(MediaManager.MediaSession session)
    {
        if (session.Id.Contains("Spotify") || session.Id.Contains("spotify"))
        {
            hasSession = true;
            sessionId = session.Id;
        }
    }

    private void MediaManager_OnAnySessionClosed(MediaManager.MediaSession session)
    {
        if (session.Id.Contains("Spotify") || session.Id.Contains("spotify"))
        {
            hasSession = false;
        }
    }

    private double GetVolume(AudioSessionManager2 sessionManager)
    {
        double volume = 0;

        AudioSessionEnumerator sessionEnumerator = sessionManager.GetSessionEnumerator();

        // 遍历所有会话，寻找匹配的进程
        foreach (AudioSessionControl session in sessionEnumerator)
        {
            if (session == null)
            {
                continue;
            }

            AudioSessionControl2 sessionControl = session.QueryInterface<AudioSessionControl2>();
            if (sessionControl == null || sessionControl.Process == null)
            {
                continue;
            }

            string processName = sessionControl.Process.ProcessName;
            AudioMeterInformation meter = null;

            if (processName.StartsWith("Spotify"))
            {
                // Spotify 部分情况下会存在多个进程，因此不能 break，并且需要累加音量
                meter = session.QueryInterface<AudioMeterInformation>();
                volume += meter.PeakValue;
            }

            // 释放对象
            meter?.Dispose();
            sessionControl?.Dispose();
            session.Dispose();
        }

        sessionEnumerator?.Dispose();

        return volume;
    }
}