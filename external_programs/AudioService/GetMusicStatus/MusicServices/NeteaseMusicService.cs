using System;
using System.Collections.Generic;
using CSCore.CoreAudioAPI;

public class NeteaseMusicService : MusicService
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

                if (processName.StartsWith("cloudmusic") || processName.StartsWith("CloudMusic"))
                {
					// 网易云音乐会存在多个进程，因此不能 break，并且需要累加音量
                    musicAppRunning = true;
                    meter = session.QueryInterface<AudioMeterInformation>();
                    volume += meter.PeakValue;
                    windowTitle = WindowDetector.GetWindowTitleByHandle(sessionControl.Process.MainWindowHandle);
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

        // 这段代码处理三种特殊情况：
        // 1. 如果音乐软件最小化到托盘，那么主窗口标题会变为空
        // 2. 如果开启了桌面歌词，那么主窗口标题就不是歌曲信息了
        // 3. 如果开启了迷你模式，那么主窗口标题就不是歌曲信息了
        // 此时，需要遍历该进程的所有窗口来获取有效窗口标题
        try
        {
            if (string.IsNullOrEmpty(windowTitle) || !windowTitle.Contains(" - "))
            {
                windowTitle = "";

                List<string> allTitles = WindowDetector.GetWindowTitles("cloudmusic");
                foreach (string title in allTitles)
                {
                    if (title.Contains(" - ") && !title.Contains("MediaPlayer"))
                    {
                        windowTitle = title;
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

        windowTitle = FixTitleNetease(windowTitle);

        // 输出结果
        string status = volume > 0.00001 ? "Playing" : "Paused";
        return $"{status}\r\n{windowTitle}";
    }

    /*
        修正网易云音乐标题
    */
    private string FixTitleNetease(string windowTitle)
    {
        return windowTitle.Replace("/", " / ");
    }
}