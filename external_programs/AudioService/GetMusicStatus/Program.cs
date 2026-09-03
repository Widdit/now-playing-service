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

    // 保护 currentSessionManager 的读写锁，避免多线程访问时出现竞态问题
    private static readonly object sessionManagerLock = new object();

    // 当前正在使用的音频会话管理器。当 device-id 为 default 时，会随系统默认输出设备的变化而动态更新
    private static AudioSessionManager2 currentSessionManager;

    // 默认音频设备变更通知客户端。需保持静态引用，防止被垃圾回收
    private static MMNotificationClient notificationClient;

    // 用于创建通知客户端的设备枚举器。需在程序运行期间保持存活
    private static MMDeviceEnumerator notificationDeviceEnumerator;

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

        try
        {
            if (deviceId == "default")
            {
                // 获取默认设备的音频会话管理器
                currentSessionManager = GetDefaultAudioSessionManager2(DataFlow.Render);

                // 监听系统默认输出设备的变更事件，以便在用户切换设备（如扬声器切换为耳机）时，自动更新音频会话管理器
                notificationDeviceEnumerator = new MMDeviceEnumerator();
                notificationClient = new MMNotificationClient(notificationDeviceEnumerator);
                notificationClient.DefaultDeviceChanged += OnDefaultDeviceChanged;
            }
            else
            {
                // 获取指定设备的音频会话管理器
                currentSessionManager = GetAudioSessionManager2(deviceId);
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
            { "wesing", (smtc) => new WeSingService() },
            { "browser", (smtc) => new BrowserSMTC() },
            { "salt", (smtc) => new SaltPlayerSMTC() }
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
            string currentOutput;
            lock (sessionManagerLock)
            {
                currentOutput = musicService.GetMusicStatus(currentSessionManager);
            }

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
        默认音频设备变更事件的回调方法，运行在音频引擎的通知线程上。
        仅关心输出设备（Render）在 Multimedia 角色下的变化，其余情况直接忽略。
        MMDevice API 不允许在通知线程中同步调用其自身接口（如枚举设备），
        因此这里只做过滤判断，具体的设备查询工作交由线程池异步完成。
    */
    static void OnDefaultDeviceChanged(object sender, DefaultDeviceChangedEventArgs e)
    {
        if (e.DataFlow != DataFlow.Render || e.Role != Role.Multimedia)
        {
            return;
        }

        ThreadPool.QueueUserWorkItem(_ => UpdateDefaultSessionManager());
    }

    /*
        重新获取当前默认音频设备的会话管理器，并替换掉旧的会话管理器。
        运行在线程池线程中，与音频引擎的通知线程相互独立。
    */
    static void UpdateDefaultSessionManager()
    {
        try
        {
            var newSessionManager = GetDefaultAudioSessionManager2(DataFlow.Render);

            lock (sessionManagerLock)
            {
                var oldSessionManager = currentSessionManager;
                currentSessionManager = newSessionManager;
                oldSessionManager?.Dispose();
            }
        }
        catch (Exception)
        {
            // 获取新的默认音频设备失败，忽略此次变化，继续使用原有的会话管理器
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
