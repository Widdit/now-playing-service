using System;
using System.Text;
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
        Console.OutputEncoding = Encoding.UTF8;

        double[] volume = {0, 0, 0, 0};
        string[] platforms = {"Netease", "QQ", "KuGou", "KuWo"};
        bool[] musicAppRunning = {false, false, false, false};
        string[] windowTitles = {"", "", "", ""};

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
                if (!string.IsNullOrEmpty(windowTitle) && windowTitle.Contains('-'))  // 保存有效窗口标题
                {
                    windowTitles[0] = windowTitle;
                }
            }
            else if (processName.StartsWith("QQMusic"))  // QQ音乐
            {
                musicAppRunning[1] = true;
                volume[1] += session.QueryInterface<AudioMeterInformation>().PeakValue;
                if (!string.IsNullOrEmpty(windowTitle) && windowTitle.Contains('-'))  // 保存有效窗口标题
                {
                    windowTitles[1] = windowTitle;
                }
            }
            else if (processName.StartsWith("KuGou"))  // 酷狗音乐
            {
                musicAppRunning[2] = true;
                volume[2] += session.QueryInterface<AudioMeterInformation>().PeakValue;
                if (!string.IsNullOrEmpty(windowTitle) && windowTitle.Contains('-'))  // 保存有效窗口标题
                {
                    // 酷狗音乐标题会滚动，例如 "还是他 - 酷狗音乐 陶喆 - 爱我"，需要修正为 "酷狗音乐 陶喆 - 爱我还是他 - "
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

                    windowTitles[2] = windowTitle;
                }
            }
            else if (processName.StartsWith("KwService"))  // 酷我音乐
            {
                musicAppRunning[3] = true;
                volume[3] += session.QueryInterface<AudioMeterInformation>().PeakValue;
                // 酷我音乐的有窗口标题的进程和发出声音的进程不是同一个，需另行获取
            }
        }

        // 输出结果
        for (int i = 0; i < musicAppRunning.Length; i++)
        {
            if (musicAppRunning[i])
            {
                // 由于酷我音乐的 KwService 进程会常驻，因此需要检查酷我音乐的窗口是否真正开启
                if (i == 3)
                {
                    WindowDetector windowDetector = new WindowDetector();
                    string windowTitle = windowDetector.GetWindowTitle("kwmusic");
                    if (string.IsNullOrEmpty(windowTitle) || !windowTitle.Contains('-'))
                    {
                        Console.WriteLine("None");
                        return;
                    }

                    // 酷我音乐标题过长会滚动，例如 "nd&Daft Punk-酷我音乐 Starboy -The Week"，需要修正为 "酷我音乐 Starboy -The Weeknd&Daft Punk-"
                    if (!windowTitle.Contains("酷我"))  // 酷我两个字被拆开了
                    {
                        windowTitle = windowTitle.Substring(1) + windowTitle.Substring(0, 1);
                    }
                    int pos = windowTitle.IndexOf("酷我");
                    windowTitle = windowTitle.Substring(pos) + windowTitle.Substring(0, pos);

                    // 去除无关信息（"酷我音乐 Starboy -The Weeknd&Daft Punk-" ==> "Starboy -The Weeknd&Daft Punk"）
                    windowTitle = windowTitle.Substring(5, windowTitle.Length - 6);

                    windowTitles[3] = windowTitle;
                }

                if (string.IsNullOrEmpty(windowTitles[i]))  // 这种情况说明音乐软件的窗口被隐藏到后台，当作未开启处理
                {
                    Console.WriteLine("None");
                    return;
                }

                string status = volume[i] > 0 ? "Playing" : "Paused";
                Console.WriteLine(status + " " + platforms[i]);
                Console.WriteLine(windowTitles[i]);
                return;
            }
        }

        // 电脑上未开启音乐软件
        Console.WriteLine("None");
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