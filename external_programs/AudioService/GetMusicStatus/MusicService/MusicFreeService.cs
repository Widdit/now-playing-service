using System;
using System.Text;
using System.Diagnostics;
using System.Collections.Generic;
using CSCore.CoreAudioAPI;

public class MusicFreeService : MusicService
{
    public override void PrintMusicStatus(AudioSessionManager2 sessionManager)
    {
        Console.OutputEncoding = Encoding.UTF8;

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

                if (processName.StartsWith("MusicFree"))
                {
                    musicAppRunning = true;
                    meter = session.QueryInterface<AudioMeterInformation>();
                    volume = meter.PeakValue;
                    // MusicFree 的窗口标题需另行获取
                    break;
                }

                // 释放对象
                meter?.Dispose();
                sessionControl?.Dispose();
                session.Dispose();
            }
        }
        catch (Exception)
        {
            Console.WriteLine("None");
            return;
        }
        finally
        {
            // 释放对象
            sessionEnumerator?.Dispose();
        }

        // 未检测到音乐软件进程
        if (!musicAppRunning)
        {
            Console.WriteLine("None");
            return;
        }

        // 获取 MusicFree 的窗口标题
        try
        {
            List<string> allTitles = WindowDetector.GetWindowTitles("MusicFree");
            foreach (string title in allTitles)
            {
                if (title.Contains(" - "))
                {
                    windowTitle = FixTitleMusicFree(title);
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
        修正 MusicFree 标题
    */
    private string FixTitleMusicFree(string windowTitle)
    {
        windowTitle = windowTitle.Replace("、", " / ");
        windowTitle = windowTitle.Replace("&", " / ");

        return windowTitle;
    }
}