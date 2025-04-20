using System;
using System.IO;
using System.Text;
using System.Drawing;
using System.Diagnostics;
using Windows.Media.Control;
using WindowsMediaController;
using Windows.Storage.Streams;
using CSCore.CoreAudioAPI;

public class AynaLivePlayerSMTC : MusicService
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

    public override void PrintMusicStatus(AudioSessionManager2 sessionManager)
    {
        Console.OutputEncoding = Encoding.UTF8;

        if (!hasSession)
        {
            Console.WriteLine("None");
            return;
        }

        // 获取媒体会话
        var mediaSessions = mediaManager.CurrentMediaSessions;
        var mediaSession = mediaSessions[sessionId];
        if (mediaSession == null)
        {
            Console.WriteLine("None");
            return;
        }

        string status = null;
        string title = null;
        string artist = null;

        try
        {
            // 获取播放状态
            // 对于卡西米尔唱片机，如果不打开 Windows 的媒体控制面板，则 SMTC 中的播放状态可能不会刷新，因此还是需要通过检测音量来判断播放/暂停
            status = GetVolume(sessionManager) > 0.00001 ? "Playing" : "Paused";

            // 获取歌曲信息
            var songInfo = mediaSession.ControlSession.TryGetMediaPropertiesAsync().GetAwaiter().GetResult();
            if (songInfo != null)
            {
                title = songInfo.Title;
                artist = songInfo.Artist;
                artist = artist.Replace(",", " / ");

                // 如果切歌，则保存封面
                if (title != prevTitle || artist != prevArtist)
                {
                    SaveThumbnail(songInfo.Thumbnail);
                }

                prevTitle = title;
                prevArtist = artist;
            }
        }
        catch (Exception)
        {
            Console.WriteLine("None");
            return;
        }

        if (string.IsNullOrEmpty(title))
        {
            Console.WriteLine("None");
            return;
        }

        // 输出结果
        Console.WriteLine(status);
        Console.WriteLine(title + " - " + artist);
    }

    private void MediaManager_OnAnySessionOpened(MediaManager.MediaSession session)
    {
        if (session.Id.Contains("Aynakeya") || session.Id.Contains("卡西米尔唱片机"))
        {
            hasSession = true;
            sessionId = session.Id;
        }
    }

    private void MediaManager_OnAnySessionClosed(MediaManager.MediaSession session)
    {
        if (session.Id.Contains("Aynakeya") || session.Id.Contains("卡西米尔唱片机"))
        {
            hasSession = false;
        }
    }

    private void SaveThumbnail(IRandomAccessStreamReference Thumbnail)
    {
        if (Thumbnail == null)
            return;

        IRandomAccessStreamWithContentType thumbnailStream = null;

        try
        {
            // 从流中读取封面图片
            thumbnailStream = Thumbnail.OpenReadAsync().GetAwaiter().GetResult();
            byte[] thumbnailBytes = new byte[thumbnailStream.Size];
            using (DataReader reader = new DataReader(thumbnailStream))
            {
                reader.LoadAsync((uint)thumbnailStream.Size).GetAwaiter().GetResult();
                reader.ReadBytes(thumbnailBytes);
            }

            // 转为 BASE64 格式字符串，并写到文件中
            string base64String = "data:image/jpeg;base64,";
            base64String += Convert.ToBase64String(thumbnailBytes);
            string filePath = "cover_base64.txt";
            File.WriteAllTextAsync(filePath, base64String).GetAwaiter().GetResult();
        }
        catch (Exception)
        {
            // ignored
        }
        finally
        {
            thumbnailStream?.Dispose();
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

            if (processName.StartsWith("AynaLivePlayer"))
            {
                // 卡西米尔唱片机会存在多个进程，因此不能 break，并且需要累加音量
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