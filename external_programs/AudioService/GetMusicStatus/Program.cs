using System;
using System.Text;
using System.Diagnostics;
using CSCore.CoreAudioAPI;

/*
    检测音乐软件的播放状态（Playing, Paused, None）、音乐平台（Netease, QQ, KuGou, KuWo）、窗口标题
    输出格式：
    "
        播放状态 音乐平台
        窗口标题
    "
    接收命令行参数：
        --device-id  音频设备 ID。仅检测该音频设备，默认值为 "default"，检测默认音频设备。
        --platform  音乐平台。期望检测的音乐软件平台，默认值为 "netease"，检测网易云音乐。
*/
class Program
{
    static void Main(string deviceId = "default", string platform = "netease")
    {
        Console.OutputEncoding = Encoding.UTF8;

        AudioSessionManager2 sessionManager;

        try
        {
            if (deviceId == "default")
            {
                // 获取默认设备的音频会话管理器
                sessionManager = GetDefaultAudioSessionManager2(DataFlow.Render);
            }
            else
            {
                // 获取指定设备的音频会话管理器
                sessionManager = GetAudioSessionManager2(deviceId);
            }
        }
        catch (Exception)
        {
            Console.WriteLine("None");
            return;
        }

        // 检测并输出相应音乐平台的播放状态
        if (platform == "netease")
        {
            NeteaseMusicService.PrintMusicStatus(sessionManager);
        }
        else if (platform == "qq")
        {
            QQMusicService.PrintMusicStatus(sessionManager);
        }
        else if (platform == "kugou")
        {
            KuGouMusicService.PrintMusicStatus(sessionManager);
        }
        else if (platform == "kuwo")
        {
            KuWoMusicService.PrintMusicStatus(sessionManager);
        }
        else if (platform == "spotify")
        {
            SpotifyMusicService.PrintMusicStatus(sessionManager);
        }
    }

    /*
        获取默认音频设备的音频会话管理器
    */
    static AudioSessionManager2 GetDefaultAudioSessionManager2(DataFlow dataFlow)
    {
        using (var enumerator = new MMDeviceEnumerator())
        {
            using (var device = enumerator.GetDefaultAudioEndpoint(dataFlow, Role.Multimedia))
            {
                // Console.WriteLine("默认音频设备为：" + device.DeviceID + " " + device.FriendlyName);

                var sessionManager = AudioSessionManager2.FromMMDevice(device);
                return sessionManager;
            }
        }
    }

    /*
        根据音频设备 ID 获取该设备的音频会话管理器
    */
    static AudioSessionManager2 GetAudioSessionManager2(string id)
    {
        using (var enumerator = new MMDeviceEnumerator())
        {
            using (var device = enumerator.GetDevice(id))
            {
                // Console.WriteLine("根据 ID 获取到音频设备：" + device.DeviceID + " " + device.FriendlyName);

                var sessionManager = AudioSessionManager2.FromMMDevice(device);
                return sessionManager;
            }
        }
    }
}