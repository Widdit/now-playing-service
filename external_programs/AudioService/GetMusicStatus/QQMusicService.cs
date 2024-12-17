using System;
using System.Text;
using System.Diagnostics;
using System.Collections.Generic;
using CSCore.CoreAudioAPI;

public class QQMusicService
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

                if (processName.StartsWith("QQMusic"))
                {
                    musicAppRunning = true;
                    volume = session.QueryInterface<AudioMeterInformation>().PeakValue;
                    windowTitle = sessionControl.Process.MainWindowTitle;
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
        // 1. 如果开启了桌面歌词，那么主窗口标题就不是歌曲信息了，需要遍历该进程的所有窗口来获取真正的歌曲信息
        // 2. 如果音乐软件最小化到托盘，那么主窗口标题会变为空，需要遍历该进程的所有窗口来获取有效窗口标题
        try
        {
            if (windowTitle.Contains("桌面歌词") || string.IsNullOrEmpty(windowTitle))
            {
                windowTitle = "";

                List<string> allTitles = WindowDetector.GetWindowTitles("QQMusic");
                foreach (string title in allTitles)
                {
                    if (!title.Contains("桌面歌词") && title.Contains('-'))
                    {
                        windowTitle = title;
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

        // 输出结果
        string status = volume > 0.00001 ? "Playing" : "Paused";
        Console.WriteLine(status + " " + "QQ");
        Console.WriteLine(windowTitle);
    }
}