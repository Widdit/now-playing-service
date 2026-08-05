using System;
using WindowsMediaController;
using CSCore.CoreAudioAPI;

public class SaltPlayerSMTC : MusicService
{
    private MediaManager mediaManager;
    private bool hasSession = false;
    private string sessionId;
    private string prevTitle = "";
    private string prevArtist = "";

    // 设置 NOWPLAYING_DEBUG=1 输出诊断信息到 stderr（stdout 被后端解析，不能污染）
    private static readonly bool Debug =
        Environment.GetEnvironmentVariable("NOWPLAYING_DEBUG") == "1";

    private static void Log(string msg)
    {
        if (Debug)
        {
            Console.Error.WriteLine($"[SaltPlayerSMTC] {msg}");
        }
    }

    private static bool IsSaltPlayerSession(string id)
    {
        if (string.IsNullOrEmpty(id))
        {
            return false;
        }

        return id.IndexOf("saltplayer", StringComparison.OrdinalIgnoreCase) >= 0
            || id.IndexOf("salt player", StringComparison.OrdinalIgnoreCase) >= 0;
    }

    public override void Init()
    {
        mediaManager = new MediaManager();

        mediaManager.OnAnySessionOpened += MediaManager_OnAnySessionOpened;
        mediaManager.OnAnySessionClosed += MediaManager_OnAnySessionClosed;

        mediaManager.Start();
        Log("MediaManager started, waiting for SMTC sessions");
    }

    public override string GetMusicStatus(AudioSessionManager2 sessionManager)
    {
        if (!hasSession)
        {
            // 兜底：如果 SPW 在本程序启动前就已在播放，OnAnySessionOpened 可能不会触发，
            // 这里主动扫一遍当前会话列表
            if (!TryFindExistingSession())
            {
                Log("no matching SMTC session yet");
                return "None";
            }
        }

        // 会话可能在轮询期间关闭，使用安全查找避免字典索引异常终止检测进程
        MediaManager.MediaSession mediaSession;
        try
        {
            var mediaSessions = mediaManager.CurrentMediaSessions;
            if (!mediaSessions.TryGetValue(sessionId, out mediaSession) || mediaSession == null)
            {
                Log($"session \"{sessionId}\" disappeared from CurrentMediaSessions");
                hasSession = false;
                sessionId = null;

                if (!TryFindExistingSession())
                {
                    return "None";
                }

                mediaSessions = mediaManager.CurrentMediaSessions;
                if (!mediaSessions.TryGetValue(sessionId, out mediaSession) || mediaSession == null)
                {
                    return "None";
                }
            }
        }
        catch (Exception e)
        {
            Log($"failed to resolve session: {e.GetType().Name}: {e.Message}");
            hasSession = false;
            sessionId = null;
            return "None";
        }

        string status = null;
        string title = null;
        string artist = null;
        int currentSec = -1;
        int totalSec = -1;

        try
        {
            // 获取播放状态
            var playbackInfo = mediaSession.ControlSession.GetPlaybackInfo();
            var playbackStatus = playbackInfo.PlaybackStatus;

            status = playbackStatus == Windows.Media.Control.GlobalSystemMediaTransportControlsSessionPlaybackStatus.Playing
                ? "Playing"
                : "Paused";

            // 获取 SMTC 时间轴，使后端能在用户拖动进度后立即校准
            var timeline = mediaSession.ControlSession.GetTimelineProperties();
            if (timeline != null)
            {
                currentSec = Math.Max(0, (int)timeline.Position.TotalSeconds);
                totalSec = Math.Max(0, (int)timeline.EndTime.TotalSeconds);
            }

            // 获取歌曲信息
            var songInfo = mediaSession.ControlSession.TryGetMediaPropertiesAsync().GetAwaiter().GetResult();
            if (songInfo != null)
            {
                title = songInfo.Title;
                artist = songInfo.Artist;

                // 如果切歌，则保存封面
                if (title != prevTitle || artist != prevArtist)
                {
                    ThumbnailHelper.SaveThumbnail(songInfo.Thumbnail);
                }

                prevTitle = title;
                prevArtist = artist;
            }
        }
        catch (Exception e)
        {
            Log($"failed to read playback info: {e.GetType().Name}: {e.Message}");
            return "None";
        }

        if (string.IsNullOrEmpty(title))
        {
            Log("title is empty");
            return "None";
        }

        // 输出结果（格式：歌名 - 歌手；第三行为精确进度）
        string songTitle = string.IsNullOrEmpty(artist) ? title : $"{title} - {artist}";
        Log($"detected: {status} / {songTitle} / progress={currentSec}|{totalSec}");

        if (currentSec >= 0 && totalSec > 0)
        {
            return $"{status}\r\n{songTitle}\r\nProgress:{currentSec}|{totalSec}";
        }

        // 即使时间轴暂时不可用，也显式通知 Java 清空旧进度，避免继续沿用拖动前的位置
        return $"{status}\r\n{songTitle}\r\nProgress:-1|-1";
    }

    private bool TryFindExistingSession()
    {
        try
        {
            foreach (var id in mediaManager.CurrentMediaSessions.Keys)
            {
                if (IsSaltPlayerSession(id))
                {
                    Log($"found pre-existing session via fallback scan: \"{id}\"");
                    hasSession = true;
                    sessionId = id;
                    return true;
                }
            }
        }
        catch (Exception e)
        {
            Log($"fallback scan failed: {e.GetType().Name}: {e.Message}");
        }

        return false;
    }

    private void MediaManager_OnAnySessionOpened(MediaManager.MediaSession session)
    {
        Log($"session opened: \"{session.Id}\" -> match={IsSaltPlayerSession(session.Id)}");

        // 匹配 Salt Player for Windows 的 SMTC 会话
        if (IsSaltPlayerSession(session.Id))
        {
            hasSession = true;
            sessionId = session.Id;
        }
    }

    private void MediaManager_OnAnySessionClosed(MediaManager.MediaSession session)
    {
        Log($"session closed: \"{session.Id}\"");

        if (IsSaltPlayerSession(session.Id) && session.Id == sessionId)
        {
            hasSession = false;
            sessionId = null;
        }
    }
}
