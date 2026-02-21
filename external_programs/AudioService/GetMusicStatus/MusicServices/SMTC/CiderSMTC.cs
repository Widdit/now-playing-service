using System;
using WindowsMediaController;
using CSCore.CoreAudioAPI;

public class CiderSMTC : MusicService
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
            // 对于 Cider，如果不打开 Windows 的媒体控制面板，则 SMTC 中的播放状态可能不会刷新，因此还是需要通过检测音量来判断播放/暂停
            status = GetVolume(sessionManager) > 0.00001 ? "Playing" : "Paused";

            // 获取歌曲信息
            var songInfo = mediaSession.ControlSession.TryGetMediaPropertiesAsync().GetAwaiter().GetResult();
            if (songInfo != null && !string.IsNullOrEmpty(songInfo.Title))
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
        if (session.Id.Contains("msedgewebview2") || session.Id.Contains("Cider") || session.Id.Contains("cider"))
        {
            hasSession = true;
            sessionId = session.Id;
        }
    }

    private void MediaManager_OnAnySessionClosed(MediaManager.MediaSession session)
    {
        if (session.Id.Contains("msedgewebview2") || session.Id.Contains("Cider") || session.Id.Contains("cider"))
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

            if (processName.StartsWith("msedgewebview2"))
            {
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