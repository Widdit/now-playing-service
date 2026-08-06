using System;
using System.Linq;
using System.Text;
using System.Collections.Generic;
using System.Diagnostics;

namespace ProcessChecker
{
    class Program
    {
        private static readonly IReadOnlyDictionary<string, string> PlatformProcessMap = new Dictionary<string, string>
        {
            { "netease", "cloudmusic" },
            { "qq", "QQMusic" },
            { "kugou", "KuGou" },
            { "kuwo", "kwmusic" },
            { "soda", "SodaMusic" },
            { "spotify", "Spotify" },
            { "apple", "AppleMusic" },
            { "ayna", "start" },
            { "potplayer", "PotPlayerMini64" },
            { "foobar", "foobar2000" },
            { "lx", "lx-music-desktop" },
            { "huahua", "花花直播助手" },
            { "musicfree", "MusicFree" },
            { "bq", "BQ_SongHime" },
            { "aimp", "AIMP" },
            { "youtube", "youtube-music-desktop-app" },
            { "miebo", "咩播" },
            { "yesplay", "YesPlayMusic" },
            { "cider", "Cider" },
            { "wesing", "WeSing" },
            // 浏览器平台可能使用多种浏览器，用 "|" 分隔多个进程名，任一存在即视为运行中
            { "browser", "chrome|msedge|firefox" }
        };

        static void Main(string platform = "netease")
        {
            Console.OutputEncoding = Encoding.UTF8;

            if (PlatformProcessMap.TryGetValue(platform, out string targetProcessNames))
            {
                bool isRunning = targetProcessNames.Split('|')
                    .Any(name => Process.GetProcessesByName(name).Length > 0);

                Console.WriteLine(isRunning ? "true" : "false");
            }
            else
            {
                Console.WriteLine("false");
            }
        }
    }
}
