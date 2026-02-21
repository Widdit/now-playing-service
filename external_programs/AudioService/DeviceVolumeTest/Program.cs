using System;
using System.Text;
using System.Diagnostics;
using System.Collections.Generic;
using System.Threading;
using CSCore.CoreAudioAPI;

class Program
{
    static List<string> deviceNames = new List<string>();
    static readonly object _writeLock = new object();

    static void Main(string[] args)
    {
        Console.OutputEncoding = Encoding.UTF8;

        while (true)
        {
            Console.Clear(); // 清空控制台
            deviceNames.Clear(); // 清空设备名缓存

            try
            {
                GetDefaultAudioSessionManager2(DataFlow.Render);
                Console.WriteLine();

                List<AudioSessionManager2> sessionManagers = GetAllAudioSessionManager2(DataFlow.Render);
                int i = 0;

                foreach (AudioSessionManager2 sessionManager in sessionManagers)
                {
                    Console.WriteLine();
                    WriteLineColor($"以下为音频设备：{deviceNames[i]} 的结果：", ConsoleColor.Green);

                    if (sessionManager == null)
                    {
                        WriteLineColor("音频会话管理器为空，跳过该设备。", ConsoleColor.Magenta);
                        continue;
                    }

                    AudioSessionEnumerator sessionEnumerator = sessionManager.GetSessionEnumerator();
                    if (sessionEnumerator == null)
                    {
                        WriteLineColor("无法获取音频会话列表，跳过该设备。", ConsoleColor.Magenta);
                        continue;
                    }

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
                        meter = session.QueryInterface<AudioMeterInformation>();
                        double volume = meter.PeakValue;

                        if (volume > 0)
                        {
                            // 蓝色文字 + 黄色高亮进程名和音量
                            lock (_writeLock)
                            {
                                Console.ForegroundColor = ConsoleColor.Cyan;
                                Console.Write("[" + DateTime.Now.ToString("HH:mm:ss.fff") + "] 检测到进程 [");

                                Console.ForegroundColor = ConsoleColor.Yellow;
                                Console.Write(processName);

                                Console.ForegroundColor = ConsoleColor.Cyan;
                                Console.Write("] 的音量为：");

                                Console.ForegroundColor = ConsoleColor.Yellow;
                                Console.WriteLine(volume);

                                Console.ResetColor();
                            }
                        }
                        else
                        {
                            WriteLineColor($"检测到进程 [{processName}] 的音量为：{volume}", ConsoleColor.Cyan);
                        }

                        // 释放对象
                        meter?.Dispose();
                        sessionControl?.Dispose();
                        session.Dispose();
                    }

                    // 释放对象
                    sessionEnumerator?.Dispose();

                    i++;
                }
            }
            catch (Exception ex)
            {
                WriteLineColor($"运行失败！错误信息：{ex.Message}", ConsoleColor.Red);
            }

            // 根据设备数量决定刷新时间
            int sleepTime = 2000;
            int count = deviceNames.Count;
            if (count > 8)
                sleepTime = 4000;
            else if (count > 5)
                sleepTime = 3000;
            Thread.Sleep(sleepTime);
        }
    }

    static AudioSessionManager2 GetDefaultAudioSessionManager2(DataFlow dataFlow)
    {
        using (var enumerator = new MMDeviceEnumerator())
        using (var device = enumerator.GetDefaultAudioEndpoint(dataFlow, Role.Multimedia))
        {
            WriteLineColor("默认音频设备为：" + device.FriendlyName, ConsoleColor.Magenta);
            return AudioSessionManager2.FromMMDevice(device);
        }
    }

    static List<AudioSessionManager2> GetAllAudioSessionManager2(DataFlow dataFlow)
    {
        var sessionManagers = new List<AudioSessionManager2>();
        var enumerator = new MMDeviceEnumerator();
        var devices = enumerator.EnumAudioEndpoints(dataFlow, DeviceState.Active);

        foreach (var device in devices)
        {
            WriteLineColor("检测到音频设备：" + device.FriendlyName, ConsoleColor.Magenta);
            var sessionManager = AudioSessionManager2.FromMMDevice(device);
            sessionManagers.Add(sessionManager);
            deviceNames.Add(device.FriendlyName);
        }

        return sessionManagers;
    }

    public static void WriteLineColor(object toprint, ConsoleColor color = ConsoleColor.White)
    {
        lock (_writeLock)
        {
            Console.ForegroundColor = color;
            Console.WriteLine("[" + DateTime.Now.ToString("HH:mm:ss.fff") + "] " + toprint);
            Console.ResetColor();
        }
    }
}
