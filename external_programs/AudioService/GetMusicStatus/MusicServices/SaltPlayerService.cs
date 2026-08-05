using System;
using System.Collections.Generic;
using CSCore.CoreAudioAPI;

public class SaltPlayerService : MusicService
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

                // 匹配 Salt Player for Windows 进程
                if (processName.Contains("Salt Player") || processName.Contains("SaltPlayer"))
                {
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

        // 处理最小化、无主窗口或主窗口只显示应用名称的情况
        try
        {
            if (!IsSongTitle(windowTitle))
            {
                List<string> allTitles = WindowDetector.GetWindowTitles("Salt Player for Windows");
                foreach (string title in allTitles)
                {
                    if (IsSongTitle(title))
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

        if (!IsSongTitle(windowTitle))
        {
            return "None";
        }

        string status = volume > 0.00001 ? "Playing" : "Paused";
        return $"{status}\r\n{windowTitle}";
    }

    private static bool IsSongTitle(string title)
    {
        return !string.IsNullOrWhiteSpace(title)
            && !title.Equals("Salt Player for Windows", StringComparison.OrdinalIgnoreCase)
            && title.Contains(" - ");
    }
}
