using System;
using System.Text;
using System.Diagnostics;
using System.Collections.Generic;
using CSCore.CoreAudioAPI;

public class LxMusicService : MusicService
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

                if (processName.StartsWith("lx-music-desktop"))
                {
                    musicAppRunning = true;
                    volume = session.QueryInterface<AudioMeterInformation>().PeakValue;
                    // 洛雪音乐的窗口标题需另行获取
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

        // 获取洛雪音乐的窗口标题
        try
        {
            List<string> allTitles = WindowDetector.GetWindowTitles("lx-music-desktop");
            foreach (string title in allTitles)
            {
                if (title.Contains(" - "))
                {
                    windowTitle = FixTitleLx(title);
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
        修正洛雪音乐标题
    */
    private string FixTitleLx(string windowTitle)
    {
        windowTitle = windowTitle.Replace("、", " / ");

        return windowTitle;
    }
}