using System;
using System.IO;
using System.Text;
using System.Diagnostics;
using System.Collections.Generic;
using CSCore.CoreAudioAPI;

public class SpotifyMusicService
{
    public static void PrintMusicStatus(AudioSessionManager2 sessionManager)
    {
        Console.OutputEncoding = Encoding.UTF8;

        double volume = 0;
        bool musicAppRunning = false;
        string windowTitle = "";

        try
        {
            AudioSessionEnumerator sessionEnumerator = sessionManager.GetSessionEnumerator();
            
            AudioSessionControl2 sessionControl;

            // 遍历所有会话，寻找匹配的进程
            foreach (AudioSessionControl session in sessionEnumerator)
            {
                if (session == null)
                {
                    continue;
                }

                sessionControl = session.QueryInterface<AudioSessionControl2>();
                if (sessionControl == null || sessionControl.Process == null)
                {
                    continue;
                }

                string processName = sessionControl.Process.ProcessName;

                if (processName.StartsWith("Spotify"))
                {
                    musicAppRunning = true;
                    volume = session.QueryInterface<AudioMeterInformation>().PeakValue;
                    windowTitle = FixTitleSpotify(sessionControl.Process.MainWindowTitle);
                    break;
                }
            }
        }
        catch (Exception)
        {
            Console.WriteLine("None");
            return;
        }

        // 未检测到音乐软件进程
        if (!musicAppRunning)
        {
            Console.WriteLine("None");
            return;
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
            Console.WriteLine("None");
            return;
        }

        // 如果窗口标题为空（说明没成功获取到），则返回 None
        if (string.IsNullOrEmpty(windowTitle))
        {
            Console.WriteLine("None");
            return;
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
                Console.WriteLine("None");
                return;
            }
        }

        // 输出结果
        string status = volume > 0.00001 ? "Playing" : "Paused";
        Console.WriteLine(status);
        Console.WriteLine(windowTitle);
    }

    /*
        修正 Spotify 标题
        把歌名放前面，歌手放后面
    */
    static string FixTitleSpotify(string windowTitle)
    {
        if (!string.IsNullOrEmpty(windowTitle) && windowTitle.Contains('-'))
        {
            string[] split = windowTitle.Split('-');
            windowTitle = split[1].Trim() + " - " + split[0].Trim();
        }

        return windowTitle;
    }
}