using System;
using System.Text;
using System.Diagnostics;
using CSCore.CoreAudioAPI;
using System.Collections.Generic;
using System.Threading;

/*
    检测音乐软件的播放状态（Playing, Paused, None）、歌曲信息。
    输出条件：当播放状态变化（播放/暂停、切歌）时，立即输出；此外，即使状态无变化，每秒也输出一次。
    输出格式：
    "
        播放状态
        歌名 - 歌手名
    "
    接收命令行参数：
        --device-id  音频设备 ID。仅检测该音频设备，默认值为 "default"，检测默认音频设备。
        --platform  音乐平台。期望检测的音乐软件平台，默认值为 "netease"，检测网易云音乐。
        --smtc  是否优先使用 SMTC。默认值为 true，优先通过 SMTC 识别歌曲信息。
        --poll-interval  轮询间隔（ms）。建议取值范围为 100~1000 ms，默认值为 100 ms（最快）。
*/
class Program
{
    private const int HEARTBEAT_INTERVAL_MS = 1000;

    static void Main(string deviceId = "default", string platform = "netease", bool smtc = true, int pollInterval = 100)
    {
        Console.OutputEncoding = Encoding.UTF8;

        // 启动守护线程：当父进程退出（stdin 关闭）时，自动退出本进程
        var parentWatchThread = new Thread(() =>
        {
            try
            {
                // stdin 被关闭时 Read() 返回 -1，说明父进程已退出
                while (Console.In.Read() != -1) { }
            }
            catch { }
            Environment.Exit(0);
        });
        parentWatchThread.IsBackground = true;
        parentWatchThread.Start();

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
            Console.WriteLine($"Failed to get AudioSessionManager. Device ID: {deviceId}");
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
            { "ayna", (smtc) => new AynaLivePlayerService() },
            { "potplayer", (smtc) => smtc ? new PotPlayerSMTC() : new PotPlayerService() },
            { "foobar", (smtc) => smtc ? new FoobarSMTC() : new FoobarService() },
            { "lx", (smtc) => smtc ? new LxMusicSMTC() : new LxMusicService() },
            { "huahua", (smtc) => new HuaHuaLiveService() },
            { "musicfree", (smtc) => smtc ? new MusicFreeSMTC() : new MusicFreeService() },
            { "bq", (smtc) => new BQLivePlayerService() },
            { "aimp", (smtc) => smtc ? new AIMPSMTC() : new AIMPService() },
            { "youtube", (smtc) => new YouTubeMusicSMTC() },
            { "miebo", (smtc) => new MieboService() },
            { "yesplay", (smtc) => new YesPlayMusicService() },
            { "cider", (smtc) => smtc ? new CiderSMTC() : new CiderService() },
            { "kg", (smtc) => new WeSingService() },
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

        string prevOutput = "";

        Stopwatch globalTimer = Stopwatch.StartNew();
        long lastHeartbeatTime = 0; // 记录上次心跳的时间点
        
        // 不断轮询音乐状态
        while (true)
        {
            string currentOutput = musicService.GetMusicStatus(sessionManager);

            // 判断状态是否改变
            bool statusChanged = currentOutput != prevOutput;
            
            // 计算当前时间与上次心跳时间的差值
            long currentTime = globalTimer.ElapsedMilliseconds;
            bool heartbeatDue = (currentTime - lastHeartbeatTime) >= HEARTBEAT_INTERVAL_MS;

            // 判断是否需要输出
            if (statusChanged || heartbeatDue)
            {
                Console.WriteLine(currentOutput);
                prevOutput = currentOutput;

                if (heartbeatDue)
                {
                    lastHeartbeatTime = currentTime; 
                }
            }

            Thread.Sleep(pollInterval);
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