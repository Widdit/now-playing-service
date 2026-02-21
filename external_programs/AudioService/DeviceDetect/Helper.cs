using System;
using System.Collections.Generic;

public static class Helper
{
    private static readonly Dictionary<string, string[]> PlatformProcesses = new Dictionary<string, string[]>(StringComparer.OrdinalIgnoreCase)
    {
        { "netease", new[] { "cloudmusic" } },
        { "qq", new[] { "qqmusic" } },
        { "kugou", new[] { "kugou" } },
        { "kuwo", new[] { "kwservice" } },
        { "soda", new[] { "sodamusic" } },
        { "spotify", new[] { "spotify" } },
        { "apple", new[] { "amplibraryagent", "cider" } },
        { "ayna", new[] { "aynaliveplayer", "start", "ayna" } },
        { "potplayer", new[] { "potplayer" } },
        { "foobar", new[] { "foobar2000" } },
        { "lx", new[] { "lx-music-desktop" } },
        { "huahua", new[] { "花花", "huahua" } },
        { "musicfree", new[] { "musicfree" } },
        { "bq", new[] { "bq", "start" } },
        { "aimp", new[] { "aimp" } },
        { "youtube", new[] { "youtube-music-desktop-app" } },
        { "miebo", new[] { "咩播", "miebo" } },
        { "yesplay", new[] { "yesplaymusic" } },
        { "cider", new[] { "msedgewebview2" } }
    };

    public static bool IsMusicProcess(string processName, string platform)
    {
        if (string.IsNullOrWhiteSpace(processName) || string.IsNullOrWhiteSpace(platform))
        {
            return false;
        }
        
        processName = processName.ToLower();

        if (!PlatformProcesses.TryGetValue(platform, out string[] processes))
        {
            processes = Array.Empty<string>();
        }

        foreach (var prefix in processes)
        {
            if (processName.StartsWith(prefix, StringComparison.OrdinalIgnoreCase))
            {
                return true;
            }
        }

        return false;
    }
}
