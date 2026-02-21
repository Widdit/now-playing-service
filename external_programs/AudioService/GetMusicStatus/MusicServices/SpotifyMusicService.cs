using System;
using System.IO;
using System.Text;
using System.Collections.Generic;
using CSCore.CoreAudioAPI;

public class SpotifyMusicService : MusicService
{
    public override string GetMusicStatus(AudioSessionManager2 sessionManager)
    {
        double volume = 0;
        bool musicAppRunning = false;
        string windowTitle = "";

        AudioSessionEnumerator sessionEnumerator = null;

        try
        {
            sessionEnumerator = sessionManager.GetSessionEnumerator();

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
                    musicAppRunning = true;
                    meter = session.QueryInterface<AudioMeterInformation>();
                    volume += meter.PeakValue;

                    string mainWindowTitle = WindowDetector.GetWindowTitleByHandle(sessionControl.Process.MainWindowHandle);
                    if (mainWindowTitle.Contains('-'))
                    {
                        windowTitle = FixTitleSpotify(mainWindowTitle);
                    }
                }

                // 释放对象
                meter?.Dispose();
                sessionControl?.Dispose();
                session.Dispose();
            }
        }
        catch (Exception)
        {
            return "None";
        }
        finally
        {
            // 释放对象
            sessionEnumerator?.Dispose();
        }

        // 未检测到音乐软件进程
        if (!musicAppRunning)
        {
            return "None";
        }

        // 这段代码处理两种特殊情况：
        // 1. 如果音乐软件最小化到托盘，那么主窗口标题会变为空
        // 2. 如果开启了迷你播放器，那么主窗口标题就不是歌曲信息了
        // 此时，需要遍历该进程的所有窗口来获取有效窗口标题
        try
        {
            if (string.IsNullOrEmpty(windowTitle) || windowTitle.Contains("Web Player"))
            {
                List<string> allTitles = WindowDetector.GetWindowTitles("Spotify");
                foreach (string title in allTitles)
                {
                    if (title.Contains("Web Player"))
                    {
                        continue;
                    }

                    if (title.Contains(" - ") || title.StartsWith("Spotify"))
                    {
                        windowTitle = FixTitleSpotify(title);
                        break;
                    }
                }
            }
        }
        catch (Exception)
        {
            return "None";
        }

        // 如果窗口标题为空（说明没成功获取到），则返回 None
        if (string.IsNullOrEmpty(windowTitle))
        {
            return "None";
        }

        // Spotify 在暂停音乐时，窗口标题会变成 "Spotify Free"（也可能是 "Premium"），因此需要对窗口标题进行缓存
        string cachePath = "spotify_title.cache";
        if (windowTitle.Contains(" - "))
        {
            // 写入缓存
            File.WriteAllText(cachePath, windowTitle, Encoding.UTF8);
        }
        else
        {
            // 读取缓存
            if (File.Exists(cachePath))
            {
                windowTitle = File.ReadAllText(cachePath, Encoding.UTF8).Trim();
            }
            else
            {
                return "None";
            }
        }

        // 输出结果
        string status = volume > 0.00001 ? "Playing" : "Paused";
        return $"{status}\r\n{windowTitle}";
    }

    /*
        修正 Spotify 标题
        把歌名放前面，歌手放后面
    */
    private string FixTitleSpotify(string windowTitle)
    {
        if (!string.IsNullOrEmpty(windowTitle) && windowTitle.Contains('-'))
        {
            string[] split = windowTitle.Split('-');
            windowTitle = split[1].Trim() + " - " + split[0].Trim();
        }

        return windowTitle;
    }
}