using System;
using System.Text;
using System.Diagnostics;
using System.Collections.Generic;
using CSCore.CoreAudioAPI;

/*
    检测音乐软件的播放状态（Playing, Paused, None）、音乐平台（Netease, QQ, KuGou, KuWo）、窗口标题
    输出格式：
    "
        播放状态 音乐平台
        窗口标题
    "
    优先级顺序：网易云 > QQ > 酷狗 > 酷我
*/
class Program
{
    static void Main(string[] args)
    {
        /*
            1. 检测各音乐软件是否运行、音量、标题，并给数组赋值
        */
        Console.OutputEncoding = Encoding.UTF8;

        double[] volume = {0, 0, 0, 0};
        string[] platforms = {"Netease", "QQ", "KuGou", "KuWo"};
        bool[] musicAppRunning = {false, false, false, false};
        string[] windowTitles = {"", "", "", ""};

        try
        {
            // 获取默认音频会话管理器
            AudioSessionManager2 sessionManager = GetDefaultAudioSessionManager2(DataFlow.Render);
            AudioSessionEnumerator sessionEnumerator = sessionManager.GetSessionEnumerator();

            AudioSessionControl2 sessionControl = null;

            // 遍历所有会话，寻找匹配的进程
            foreach (AudioSessionControl session in sessionEnumerator)
            {
                sessionControl = session.QueryInterface<AudioSessionControl2>();
                string processName = sessionControl.Process.ProcessName;
                string windowTitle = sessionControl.Process.MainWindowTitle;

                // 一个音乐软件可能有多个进程，为避免部分进程不发出声音而造成误判，因此对音量进行累加
                if (processName.StartsWith("cloudmusic"))  // 网易云音乐
                {
                    musicAppRunning[0] = true;
                    volume[0] += session.QueryInterface<AudioMeterInformation>().PeakValue;
                    if (!string.IsNullOrEmpty(windowTitle) && (windowTitle.Contains('-') || windowTitle.Contains("桌面歌词")))
                    {
                        windowTitles[0] = windowTitle;
                    }
                }
                else if (processName.StartsWith("QQMusic"))  // QQ音乐
                {
                    musicAppRunning[1] = true;
                    volume[1] += session.QueryInterface<AudioMeterInformation>().PeakValue;
                    if (!string.IsNullOrEmpty(windowTitle) && (windowTitle.Contains('-') || windowTitle.Contains("桌面歌词")))
                    {
                        windowTitles[1] = windowTitle;
                    }
                }
                else if (processName.StartsWith("KuGou"))  // 酷狗音乐
                {
                    musicAppRunning[2] = true;
                    volume[2] += session.QueryInterface<AudioMeterInformation>().PeakValue;
                    if (!string.IsNullOrEmpty(windowTitle) && (windowTitle.Contains('-') || windowTitle.Contains("桌面歌词")))
                    {
                        windowTitles[2] = windowTitle.Contains("桌面歌词") ? windowTitle : fixTitleKuGou(windowTitle);
                    }
                }
                else if (processName.StartsWith("KwService"))  // 酷我音乐
                {
                    musicAppRunning[3] = true;
                    volume[3] += session.QueryInterface<AudioMeterInformation>().PeakValue;
                    // 酷我音乐的有窗口标题的进程和发出声音的进程不是同一个，需另行获取
                }
            }
        }
        catch (Exception)
        {
            Console.WriteLine("None");
            return;
        }

        /*
            2. 确定把哪个音乐平台作为目标对象
        */
        int targetIndex = GetTargetIndex(musicAppRunning, volume);

        /*
            3. 输出结果
        */
        // 没有检测到任何音乐软件
        if (targetIndex == -1)
        {
            Console.WriteLine("None");
            return;
        }

        // 由于酷我音乐的 KwService 进程会常驻，因此需要检查酷我音乐的窗口是否真正开启
        try
        {
            if (targetIndex == 3)
            {
                string windowTitle = WindowDetector.GetWindowTitle("kwmusic");
                if (string.IsNullOrEmpty(windowTitle) || !windowTitle.Contains('-'))  // 未检测到酷我音乐窗口
                {
                    Console.WriteLine("None");
                    return;
                }

                windowTitles[3] = fixTitleKuWo(windowTitle);
            }
        }
        catch (Exception)
        {
            Console.WriteLine("None");
            return;
        }
        

        // 如果开启了桌面歌词，那么主窗口标题就不是歌曲信息了，需要遍历该进程的所有窗口来获取真正的歌曲信息
        try
        {
            if (windowTitles[targetIndex].Contains("桌面歌词"))
            {
                windowTitles[targetIndex] = "";

                string[] procs = {"cloudmusic", "QQMusic", "KuGou"};
                List<string> allTitles = WindowDetector.GetWindowTitles(procs[targetIndex]);
                foreach (string title in allTitles)
                {
                    if (!title.Contains("桌面歌词") && title.Contains('-'))
                    {
                        windowTitles[targetIndex] = title;
                        if (targetIndex == 2)  // 酷狗音乐需要修正标题
                        {
                            windowTitles[targetIndex] = fixTitleKuGou(title);
                        }
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

        // 这种情况说明音乐软件的窗口被隐藏到后台，当作未开启处理
        if (string.IsNullOrEmpty(windowTitles[targetIndex]))
        {
            Console.WriteLine("None");
            return;
        }

        // 输出目标结果
        string status = volume[targetIndex] > 0 ? "Playing" : "Paused";
        Console.WriteLine(status + " " + platforms[targetIndex]);
        Console.WriteLine(windowTitles[targetIndex]);
    }

    /*
        获取目标音乐软件的 index
        如果开启了多个音乐软件，处理逻辑为：
        - 如果至少有一个在放歌，那么选定正在放歌之中优先级最高的那个
        - 否则就意味着都没在放歌，那么选定正在运行之中优先级最高的那个
    */
    static int GetTargetIndex(bool[] musicAppRunning, double[] volume)
    {
        int a = -1;
        for (int i = 0; i < musicAppRunning.Length; i++)
        {
            if (musicAppRunning[i])
            {
                a = i;
                break;
            }
        }

        int b = -1;
        for (int i = 0; i < volume.Length; i++)
        {
            if (volume[i] > 0)
            {
                b = i;
                break;
            }
        }

        if (a == -1)
        {
            return -1;
        }
        else
        {
            if (b == -1)
            {
                return a;
            }
            else
            {
                return b;
            }
        }
    }

    /*
        修正酷狗音乐标题
        酷狗音乐标题会滚动，例如 "还是他 - 酷狗音乐 陶喆 - 爱我"，需要修正为 "酷狗音乐 陶喆 - 爱我还是他 - "
    */
    static string fixTitleKuGou(string windowTitle)
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

    /*
        修正酷我音乐标题
        酷我音乐标题过长会滚动，例如 "nd&Daft Punk-酷我音乐 Starboy -The Week"，需要修正为 "酷我音乐 Starboy -The Weeknd&Daft Punk-"
    */
    static string fixTitleKuWo(string windowTitle)
    {
        if (!windowTitle.Contains("酷我"))  // 酷我两个字被拆开了
        {
            windowTitle = windowTitle.Substring(1) + windowTitle.Substring(0, 1);
        }
        int pos = windowTitle.IndexOf("酷我");
        windowTitle = windowTitle.Substring(pos) + windowTitle.Substring(0, pos);

        // 去除无关信息（"酷我音乐 Starboy -The Weeknd&Daft Punk-" ==> "Starboy -The Weeknd&Daft Punk"）
        windowTitle = windowTitle.Substring(5, windowTitle.Length - 6);

        return windowTitle;
    }

    static AudioSessionManager2 GetDefaultAudioSessionManager2(DataFlow dataFlow)
    {
        using (var enumerator = new MMDeviceEnumerator())
        {
            using (var device = enumerator.GetDefaultAudioEndpoint(dataFlow, Role.Multimedia))
            {
                var sessionManager = AudioSessionManager2.FromMMDevice(device);
                return sessionManager;
            }
        }
    }
}