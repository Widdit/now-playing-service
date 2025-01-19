using System;
using System.Text;
using System.Text.RegularExpressions;
using System.Diagnostics;
using System.Collections.Generic;
using CSCore.CoreAudioAPI;

public class FoobarService : MusicService
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

                if (processName.StartsWith("foobar2000"))
                {
                    // Foobar2000 部分情况下会存在多个进程，因此不能 break，并且需要累加音量
                    musicAppRunning = true;
                    volume += session.QueryInterface<AudioMeterInformation>().PeakValue;
                    windowTitle = sessionControl.Process.MainWindowTitle;
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

        // 如果 Foobar2000 开启了其它窗口，那么主窗口标题就不是歌曲信息了
        // 此时，需要遍历该进程的所有窗口来获取有效窗口标题
        try
        {
            if (string.IsNullOrEmpty(windowTitle) || !windowTitle.Contains("foobar2000"))
            {
                windowTitle = "";

                List<string> allTitles = WindowDetector.GetWindowTitles("foobar2000");
                foreach (string title in allTitles)
                {
                    if (title.Contains("foobar2000"))
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

        // 修正窗口标题
        windowTitle = FixTitleFoobar(windowTitle);

        // 输出结果
        string status = volume > 0.00001 ? "Playing" : "Paused";
        Console.WriteLine(status);
        Console.WriteLine(windowTitle);
    }

    /*
        修正 Foobar2000 标题
        "Christopher Cross - [Christopher Cross #08] Sailing  [foobar2000]" → "Sailing - Christopher Cross"
    */
    private string FixTitleFoobar(string windowTitle)
    {
        // 删除所有 [] 的内容
        windowTitle = Regex.Replace(windowTitle, @"\[[^\]]*\]", "");

        // 将所有连续的多个空格替换为单个空格
        windowTitle = Regex.Replace(windowTitle, @"\s+", " ");

        // 把歌名放前面，歌手放后面
        if (!string.IsNullOrEmpty(windowTitle) && windowTitle.Contains('-'))
        {
            string[] split = windowTitle.Split('-');
            windowTitle = split[1].Trim() + " - " + split[0].Trim();
        }

        return windowTitle.Trim();
    }
}