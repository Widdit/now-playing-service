using System;
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
            { "ayna", "AynaLivePlayer" },
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
            { "cider", "Cider" }
        };

        static void Main(string platform = "netease")
        {
            Console.OutputEncoding = Encoding.UTF8;

            if (PlatformProcessMap.TryGetValue(platform, out string targetProcessName))
            {
                Process[] processes = Process.GetProcessesByName(targetProcessName);

                if (processes.Length > 0)
                {
                    Console.WriteLine("true");
                }
                else
                {
                    Console.WriteLine("false");
                }
            }
            else
            {
                Console.WriteLine("false");
            }
        }
    }
}
