using System;
using System.Text;
using System.Diagnostics;
using System.Collections.Generic;
using System.Threading;
using CSCore.CoreAudioAPI;

class Program
{
    static string defaultDeviceId = null;
    static List<string> deviceIds = new List<string>();

    static void Main(string platform = "netease")
    {
        Console.OutputEncoding = Encoding.UTF8;

        try
        {
            GetDefaultAudioSessionManager2(DataFlow.Render);

            List<AudioSessionManager2> sessionManagers = GetAllAudioSessionManager2(DataFlow.Render);
            int i = 0;

            // 满足以下条件的音频设备 ID 列表：存在指定平台音乐进程，并且进程音量 > 0
            List<string> satisfiedDeviceIds = new List<string>();

            foreach (AudioSessionManager2 sessionManager in sessionManagers)
            {
                if (sessionManager == null)
                {
                    continue;
                }

                AudioSessionEnumerator sessionEnumerator = sessionManager.GetSessionEnumerator();
                if (sessionEnumerator == null)
                {
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

                    if (volume > 0.00001)
                    {
                        // 判断是否是音乐进程
                        if (Helper.IsMusicProcess(processName, platform))
                        {
                            // 加入满足条件的音频设备 ID 列表
                            satisfiedDeviceIds.Add(deviceIds[i]);

                            // 释放对象
                            meter?.Dispose();
                            sessionControl?.Dispose();
                            session.Dispose();

                            break;
                        }
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

            // 开始决定输出哪个音频设备
            foreach (string deviceId in satisfiedDeviceIds)
            {
                // 如果包含默认音频设备，则优先输出默认音频设备
                if (deviceId == defaultDeviceId)
                {
                    Console.WriteLine("default");
                    return;
                }
            }

            if (satisfiedDeviceIds.Count >= 1)
            {
                Console.WriteLine(satisfiedDeviceIds[0]);
            }
            else
            {
                Console.WriteLine("None");
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Fail! {ex.Message}");
        }
    }

    static AudioSessionManager2 GetDefaultAudioSessionManager2(DataFlow dataFlow)
    {
        using (var enumerator = new MMDeviceEnumerator())
        using (var device = enumerator.GetDefaultAudioEndpoint(dataFlow, Role.Multimedia))
        {
            defaultDeviceId = device.DeviceID;
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
            var sessionManager = AudioSessionManager2.FromMMDevice(device);
            sessionManagers.Add(sessionManager);
            deviceIds.Add(device.DeviceID);
        }

        return sessionManagers;
    }
}
