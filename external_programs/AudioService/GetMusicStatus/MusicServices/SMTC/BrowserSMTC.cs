using System;
using System.Linq;
using Windows.Media.Control;
using WindowsMediaController;
using CSCore.CoreAudioAPI;

/*
    浏览器网页播放器（Chrome / Edge / Firefox 等）的 SMTC 实现。
    浏览器会把网页的 MediaSession（navigator.mediaSession.metadata / setPositionState）
    桥接到 Windows SMTC，因此可以拿到完整的歌曲信息、播放状态和精确进度。
    与桌面播放器不同，浏览器的 SMTC 播放状态是实时刷新的，无需通过音量峰值判断播放/暂停。
*/
public class BrowserSMTC : MusicService
{
    // 常见浏览器及 Electron 网页播放器的 SMTC Session ID 关键字（统一转为小写后匹配）
    // electron.app 覆盖各类 Electron 打包的网页播放器（未自定义 AUMID 的 Electron 应用
    // 会话 ID 均为 electron.app.Xxx 形式，如 electron.app.Yinyun）
    private static readonly string[] BrowserKeywords =
    {
        "chrome", "msedge", "firefox", "brave", "opera", "vivaldi", "arc", "qqbrowser", "sogou", "360se", "360chrome",
        "electron.app"
    };

    private MediaManager mediaManager;
    private string prevTitle = "";
    private string prevArtist = "";

    public override void Init()
    {
        mediaManager = new MediaManager();
        mediaManager.Start();
    }

    public override string GetMusicStatus(AudioSessionManager2 sessionManager)
    {
        try
        {
            // 优先选取正在播放的浏览器会话（可能同时存在多个浏览器标签页会话）
            var mediaSession = FindBrowserSession(out bool isPlaying);
            if (mediaSession == null)
            {
                return "None";
            }

            // 获取歌曲信息
            var songInfo = mediaSession.ControlSession.TryGetMediaPropertiesAsync().GetAwaiter().GetResult();
            string title = songInfo?.Title;
            string artist = songInfo?.Artist ?? "";

            if (string.IsNullOrEmpty(title))
            {
                return "None";
            }

            // 如果切歌，则保存封面
            if (title != prevTitle || artist != prevArtist)
            {
                ThumbnailHelper.SaveThumbnail(songInfo.Thumbnail);
            }

            prevTitle = title;
            prevArtist = artist;

            string status = isPlaying ? "Playing" : "Paused";
            string result = $"{status}\r\n{title + " - " + artist}";

            // 通过 SMTC 时间线输出精确进度（需要网页定期调用 setPositionState）
            try
            {
                var timeline = mediaSession.ControlSession.GetTimelineProperties();
                double totalSec = timeline.EndTime.TotalSeconds;

                if (totalSec > 0)
                {
                    double currentSec = timeline.Position.TotalSeconds;

                    // Position 是 LastUpdatedTime 时刻的快照，播放中需要补偿之后流逝的时间
                    if (isPlaying)
                    {
                        currentSec += (DateTimeOffset.UtcNow - timeline.LastUpdatedTime).TotalSeconds;
                    }

                    currentSec = Math.Max(0, Math.Min(currentSec, totalSec));
                    result += $"\r\nProgress:{(int)currentSec}|{(int)totalSec}";
                }
            }
            catch (Exception)
            {
                // 部分页面不提供时间线信息，忽略进度输出
            }

            return result;
        }
        catch (Exception)
        {
            return "None";
        }
    }

    /*
        在所有 SMTC 会话中寻找浏览器会话，优先返回正在播放的那个。
    */
    private MediaManager.MediaSession FindBrowserSession(out bool isPlaying)
    {
        isPlaying = false;
        MediaManager.MediaSession fallback = null;

        foreach (var pair in mediaManager.CurrentMediaSessions)
        {
            string sessionId = pair.Key.ToLowerInvariant();
            if (!BrowserKeywords.Any(sessionId.Contains))
            {
                continue;
            }

            GlobalSystemMediaTransportControlsSessionPlaybackStatus playbackStatus;
            try
            {
                playbackStatus = pair.Value.ControlSession.GetPlaybackInfo().PlaybackStatus;
            }
            catch (Exception)
            {
                continue;
            }

            if (playbackStatus == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Playing)
            {
                isPlaying = true;
                return pair.Value;
            }

            if (fallback == null)
            {
                fallback = pair.Value;
            }
        }

        return fallback;
    }
}
