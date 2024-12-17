using System;
using System.Text;
using System.Diagnostics;
using System.Collections.Generic;
using CSCore.CoreAudioAPI;

public class KuGouMusicService
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

                if (processName.StartsWith("KuGou"))
                {
                    // 酷狗音乐部分情况下会存在多个进程，因此不能 break
                    musicAppRunning = true;
                    volume += session.QueryInterface<AudioMeterInformation>().PeakValue;

                    string mainWindowTitle = sessionControl.Process.MainWindowTitle;
                    if (!string.IsNullOrEmpty(mainWindowTitle))
                    {
                        if (mainWindowTitle.Contains('-'))
                        {
                            windowTitle = FixTitleKuGou(mainWindowTitle);
                        }
                        else if (mainWindowTitle.Contains("桌面歌词"))
                        {
                            windowTitle = mainWindowTitle;
                        }
                    }
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

                List<string> allTitles = WindowDetector.GetWindowTitles("KuGou");
                foreach (string title in allTitles)
                {
                    if (!title.Contains("桌面歌词") && title.Contains('-'))
                    {
                        windowTitle = FixTitleKuGou(title);
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
        Console.WriteLine(status + " " + "KuGou");
        Console.WriteLine(windowTitle);
    }

    /*
        修正酷狗音乐标题
        酷狗音乐标题会滚动，例如 "还是他 - 酷狗音乐 陶喆 - 爱我"，需要修正为 "酷狗音乐 陶喆 - 爱我还是他 - "
    */
    static string FixTitleKuGou(string windowTitle)
    {
        if (!windowTitle.Contains("酷狗"))  // 酷狗两个字被拆开了
        {
            windowTitle = windowTitle.Substring(1) + windowTitle.Substring(0, 1);
        }
        int pos = windowTitle.IndexOf("酷狗");
        windowTitle = windowTitle.Substring(pos) + windowTitle.Substring(0, pos);

        // 去除无关信息（"酷狗音乐 陶喆 - 爱我还是他 - " ==> "陶喆 - 爱我还是他"）
        windowTitle = windowTitle.Substring(5, windowTitle.Length - 8);
        windowTitle = windowTitle.Replace("、", " ");

        // 把歌名放前面，歌手放后面
        if (!string.IsNullOrEmpty(windowTitle) && windowTitle.Contains('-'))
        {
            string[] split = windowTitle.Split('-');
            windowTitle = split[1].Trim() + " - " + split[0].Trim();
        }

        return windowTitle;
    }
}