using System;
using Windows.Media.Control;
using WindowsMediaController;
using CSCore.CoreAudioAPI;

public class AppleMusicSMTC : MusicService
{
    private MediaManager mediaManager;
    private bool hasSession = false;
    private string sessionId;
    private string prevTitle = "";
    private string prevArtist = "";

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

        try
        {
            // 获取播放状态
            var playbackInfo = mediaSession.ControlSession.GetPlaybackInfo();
            if (playbackInfo != null)
            {
                if (playbackInfo.PlaybackStatus == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Playing)
                {
                    status = "Playing";
                }
                else
                {
                    status = "Paused";
                }
            }

            // 获取歌曲信息
            var songInfo = mediaSession.ControlSession.TryGetMediaPropertiesAsync().GetAwaiter().GetResult();
            if (songInfo != null)
            {
                title = songInfo.Title;
                artist = songInfo.Artist;

                // Apple Music 的歌手名会含有专辑名，例如 "Floating Points — Cascade"，需要把专辑名去除
                int separatorIndex = artist.IndexOf(" — ");
                if (separatorIndex >= 0)
                {
                    artist = artist.Substring(0, separatorIndex);
                }

                // Apple Music 在切歌时，歌名会变成 "正在连接…"，此时仍使用之前歌曲信息
                if (title.Contains("正在连接"))
                {
                    title = prevTitle;
                    artist = prevArtist;
                }

                // 如果切歌，则保存封面
                if (title != prevTitle || artist != prevArtist)
                {
                    ThumbnailHelper.SaveThumbnail(songInfo.Thumbnail);
                }

                prevTitle = title;
                prevArtist = artist;
            }
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
        return $"{status}\r\n{title + " - " + artist}";
    }

    private void MediaManager_OnAnySessionOpened(MediaManager.MediaSession session)
    {
        if (session.Id.Contains("AppleMusic"))
        {
            hasSession = true;
            sessionId = session.Id;
        }
    }

    private void MediaManager_OnAnySessionClosed(MediaManager.MediaSession session)
    {
        if (session.Id.Contains("AppleMusic"))
        {
            hasSession = false;
        }
    }
}