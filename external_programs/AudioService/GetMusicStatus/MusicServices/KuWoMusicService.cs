using System;
using System.Collections.Generic;
using CSCore.CoreAudioAPI;

public class KuWoMusicService : MusicService
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

                if (processName.StartsWith("KwService"))
                {
                    musicAppRunning = true;
                    meter = session.QueryInterface<AudioMeterInformation>();
                    volume += meter.PeakValue;
                    // 酷我音乐的有窗口标题的进程和发出声音的进程不是同一个，需另行获取
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

        // 获取酷我音乐的窗口标题
        try
        {
            List<string> allTitles = WindowDetector.GetWindowTitles("kwmusic");
            foreach (string title in allTitles)
            {
                if (title.Contains('-'))
                {
                    windowTitle = FixTitleKuWo(title);
                    break;
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

        // 输出结果
        string status = volume > 0.00001 ? "Playing" : "Paused";
        return $"{status}\r\n{windowTitle}";
    }

    /*
        修正酷我音乐标题
        酷我音乐标题过长会滚动，例如 "nd&Daft Punk-酷我音乐 Starboy -The Week"，需要修正为 "酷我音乐 Starboy -The Weeknd&Daft Punk-"
    */
    private string FixTitleKuWo(string windowTitle)
    {
        if (!windowTitle.Contains("酷我"))  // 酷我两个字被拆开了
        {
            windowTitle = windowTitle.Substring(1) + windowTitle.Substring(0, 1);
        }
        int pos = windowTitle.IndexOf("酷我");
        windowTitle = windowTitle.Substring(pos) + windowTitle.Substring(0, pos);

        // 去除无关信息（"酷我音乐 Starboy -The Weeknd&Daft Punk-" ==> "Starboy -The Weeknd&Daft Punk"）
        windowTitle = windowTitle.Substring(5, windowTitle.Length - 6);

        windowTitle = windowTitle.Replace("-", " - ");
        windowTitle = windowTitle.Replace("&", " / ");

        return windowTitle;
    }
}