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

        // 获取媒体会话
        var mediaSessions = mediaManager.CurrentMediaSessions;
        var mediaSession = mediaSessions[sessionId];
        if (mediaSession == null)
        {
            Log($"session \"{sessionId}\" disappeared from CurrentMediaSessions");
            return "None";
        }

        string status = null;
        string title = null;
        string artist = null;

        try
        {
            // 获取播放状态
            var playbackInfo = mediaSession.ControlSession.GetPlaybackInfo();
            var playbackStatus = playbackInfo.PlaybackStatus;

            status = playbackStatus == Windows.Media.Control.GlobalSystemMediaTransportControlsSessionPlaybackStatus.Playing
                ? "Playing"
                : "Paused";

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

        // 输出结果（格式：歌名 - 歌手）
        string songTitle = string.IsNullOrEmpty(artist) ? title : $"{title} - {artist}";
        Log($"detected: {status} / {songTitle}");
        return $"{status}\r\n{songTitle}";
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

        if (IsSaltPlayerSession(session.Id))
        {
            hasSession = false;
        }
    }
}
