using System;
using System.Text;
using System.Diagnostics;
using System.Collections.Generic;
using CSCore.CoreAudioAPI;

public class KuWoMusicService : MusicService
{
    public override void PrintMusicStatus(AudioSessionManager2 sessionManager)
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

                if (processName.StartsWith("KwService"))
                {
                    musicAppRunning = true;
                    volume = session.QueryInterface<AudioMeterInformation>().PeakValue;
                    // 酷我音乐的有窗口标题的进程和发出声音的进程不是同一个，需另行获取
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
            Console.WriteLine("None");
            return;
        }

        // 如果窗口标题为空（说明没成功获取到），则返回 None
        if (string.IsNullOrEmpty(windowTitle))
        {
            Console.WriteLine("None");
            return;
        }

        // 输出结果
        string status = volume > 0.00001 ? "Playing" : "Paused";
        Console.WriteLine(status);
        Console.WriteLine(windowTitle);
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