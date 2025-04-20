using System;
using System.Text;
using System.Diagnostics;
using CSCore.CoreAudioAPI;
using System.Collections.Generic;
using System.Threading;

/*
    检测音乐软件的播放状态（Playing, Paused, None）、歌曲信息
    每隔 1 秒输出一次
    输出格式：
    "
        播放状态
        歌名 - 歌手名
    "
    接收命令行参数：
        --device-id  音频设备 ID。仅检测该音频设备，默认值为 "default"，检测默认音频设备。
        --platform  音乐平台。期望检测的音乐软件平台，默认值为 "netease"，检测网易云音乐。
        --smtc  是否优先使用 SMTC。默认值为 true，优先通过 SMTC 识别歌曲信息。
*/
class Program
{
    static void Main(string deviceId = "default", string platform = "netease", bool smtc = true)
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

        var musicServiceMap = new Dictionary<string, Func<bool, MusicService>>()
        {
            { "netease", (smtc) => new NeteaseMusicService() },
            { "qq", (smtc) => smtc ? new QQMusicSMTC() : new QQMusicService() },
            { "kugou", (smtc) => smtc ? new KuGouMusicSMTC() : new KuGouMusicService() },
            { "kuwo", (smtc) => new KuWoMusicService() },
            { "soda", (smtc) => new SodaMusicSMTC() },
            { "spotify", (smtc) => smtc ? new SpotifyMusicSMTC() : new SpotifyMusicService() },
            { "apple", (smtc) => smtc ? new AppleMusicSMTC() : new AppleMusicService() },
            { "ayna", (smtc) => smtc ? new AynaLivePlayerSMTC() : new AynaLivePlayerService() },
            { "potplayer", (smtc) => smtc ? new PotPlayerSMTC() : new PotPlayerService() },
            { "foobar", (smtc) => smtc ? new FoobarSMTC() : new FoobarService() },
            { "lx", (smtc) => smtc ? new LxMusicSMTC() : new LxMusicService() },
            { "huahua", (smtc) => new HuaHuaLiveService() },
            { "musicfree", (smtc) => smtc ? new MusicFreeSMTC() : new MusicFreeService() },
            { "bq", (smtc) => new BQLivePlayerService() }
        };

        MusicService musicService;
        if (musicServiceMap.TryGetValue(platform, out var createService))
        {
            musicService = createService(smtc);
        }
        else
        {
            Console.WriteLine($"Unsupported platform: {platform}");
            return;
        }

        musicService.Init();
        
        while (true)
        {
            musicService.PrintMusicStatus(sessionManager);
            Thread.Sleep(1000);
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