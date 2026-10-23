using System;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Net.Http;
using CSCore.CoreAudioAPI;
using Newtonsoft.Json.Linq;

public class SPlayerNextService : MusicService
{
    private readonly HttpClient client;
    private readonly string endpoint;
    private readonly Stopwatch clock = Stopwatch.StartNew();
    private long nextPoll;
    private int failures;
    private string output = "None";
    private string coverKey;
    // 切歌瞬间播放器 API 可能会短暂返回无曲目/非播放状态，
    // 这里做去抖动：连续多次判定为 None 才真正切换为 None，
    // 避免在两首歌之间输出一两帧虚假的 "None"。
    private int noneStreak;
    private const int NoneThreshold = 8;

    public SPlayerNextService(int port = 14558) : this(port, new HttpClientHandler { UseProxy = false }) { }

    // 可注入 HTTP handler，便于离线测试，不依赖真实播放器或音频设备。
    public SPlayerNextService(int port, HttpMessageHandler handler)
    {
        if (port < 1 || port > 65535) throw new ArgumentOutOfRangeException(nameof(port));
        client = new HttpClient(handler) { Timeout = TimeSpan.FromMilliseconds(1500),
            MaxResponseContentBufferSize = 5 * 1024 * 1024 };
        endpoint = $"http://127.0.0.1:{port}/api/now-playing";
    }

    public override string GetMusicStatus(AudioSessionManager2 sessionManager)
    {
        if (clock.ElapsedMilliseconds < nextPoll) return output;
        try
        {
            string json = client.GetStringAsync(endpoint).GetAwaiter().GetResult();
            var snapshot = JObject.Parse(json);
            string newOutput = FormatSnapshot(snapshot);
            if (newOutput == "None")
            {
                noneStreak = Math.Min(noneStreak + 1, NoneThreshold);
                if (noneStreak < NoneThreshold)
                {
                    // 可能只是切歌瞬间的短暂空档，先保留上一次的有效输出，不对外暴露 None。
                    failures = 0;
                    nextPoll = clock.ElapsedMilliseconds + 250;
                    return output;
                }
                output = "None";
                ResetCover();
            }
            else
            {
                noneStreak = 0;
                output = newOutput;
                var track = (JObject)snapshot["track"];
                string key = $"{track["source"]}|{track["id"]}|{track["cover"]}";
                if (coverKey != key)
                {
                    ResetCover();
                    SaveCover(track.Value<string>("cover"));
                    coverKey = key;
                }
            }
            failures = 0;
            nextPoll = clock.ElapsedMilliseconds + 250;
        }
        catch (Exception ex)
        {
            noneStreak = 0;
            output = "None";
            ResetCover();
            failures = Math.Min(failures + 1, 5);
            nextPoll = clock.ElapsedMilliseconds + Math.Min(5000, 500 * failures);
            if (failures == 1)
                Console.Error.WriteLine($"SPlayer-Next API unavailable: {ex.Message}. Check External API and port.");
        }
        return output;
    }

    public static string FormatSnapshot(JObject snapshot)
    {
        var track = snapshot["track"] as JObject;
        string state = snapshot.Value<string>("state");
        if (track == null || (state != "playing" && state != "paused")) return "None";
        string title = Clean(track.Value<string>("title"));
        if (string.IsNullOrWhiteSpace(title)) return "None";
        var artists = track["artists"] as JArray;
        string artist = artists == null ? "" : string.Join(" / ", artists
            .OfType<JObject>().Select(a => Clean(a.Value<string>("name"))).Where(a => a.Length > 0));
        double duration = Number(track["duration"]);
        double position = Number(snapshot["position"]);
        if (duration > 0) position = Math.Min(position, duration);
        int seconds = (int)Math.Min(int.MaxValue, position / 1000);
        int total = duration > 0 ? (int)Math.Min(int.MaxValue, duration / 1000) : -1;
        return $"{(state == "playing" ? "Playing" : "Paused")}\r\n{title} - {artist}\r\nProgress:{seconds}|{total}";
    }

    private static double Number(JToken token)
    {
        if (token == null || (token.Type != JTokenType.Integer && token.Type != JTokenType.Float)) return 0;
        double value = token.Value<double>();
        return double.IsFinite(value) ? Math.Max(0, value) : 0;
    }

    // stdout 是逐行协议，元数据里的换行不能成为伪造的状态/进度行。
    private static string Clean(string text) => (text ?? "").Replace('\r', ' ').Replace('\n', ' ').Trim();

    private void ResetCover()
    {
        coverKey = null;
        try { File.Delete("cover_base64.txt"); } catch (Exception ex) when (ex is IOException || ex is UnauthorizedAccessException) { }
    }

    private void SaveCover(string url)
    {
        if (string.IsNullOrEmpty(url)) return;
        if (url.StartsWith("cache://", StringComparison.OrdinalIgnoreCase))
        {
            SaveLocalCover(url);
            return;
        }
        if (!Uri.TryCreate(url, UriKind.Absolute, out var uri)
            || (uri.Scheme != "http" && uri.Scheme != "https")) return;
        try
        {
            using var response = client.GetAsync(uri).GetAwaiter().GetResult();
            response.EnsureSuccessStatusCode();
            string mime = response.Content.Headers.ContentType?.MediaType;
            if (mime == null || !mime.StartsWith("image/", StringComparison.OrdinalIgnoreCase)) return;
            byte[] bytes = response.Content.ReadAsByteArrayAsync().GetAwaiter().GetResult();
            File.WriteAllText("cover_base64.lock", "");
            try { File.WriteAllText("cover_base64.txt", $"data:{mime};base64,{Convert.ToBase64String(bytes)}"); }
            finally { File.Delete("cover_base64.lock"); }
        }
        catch (Exception ex) { Console.Error.WriteLine($"SPlayer-Next cover unavailable: {ex.Message}"); }
    }

    // 处理 "cache://covers/xxx.jpg" 形式的本地封面，映射到
    // %AppData%\SPlayer-Next\app-data\cache\covers\xxx.jpg
    private void SaveLocalCover(string url)
    {
        try
        {
            string relative = url.Substring("cache://".Length).Replace('/', Path.DirectorySeparatorChar);
            string appData = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
            string path = Path.Combine(appData, "SPlayer-Next", "app-data", "cache", relative);
            if (!File.Exists(path)) return;
            byte[] bytes = File.ReadAllBytes(path);
            string mime = GetMimeType(path);
            File.WriteAllText("cover_base64.lock", "");
            try { File.WriteAllText("cover_base64.txt", $"data:{mime};base64,{Convert.ToBase64String(bytes)}"); }
            finally { File.Delete("cover_base64.lock"); }
        }
        catch (Exception ex) { Console.Error.WriteLine($"SPlayer-Next local cover unavailable: {ex.Message}"); }
    }

    private static string GetMimeType(string path)
    {
        switch (Path.GetExtension(path).ToLowerInvariant())
        {
            case ".jpg":
            case ".jpeg": return "image/jpeg";
            case ".png": return "image/png";
            case ".gif": return "image/gif";
            case ".bmp": return "image/bmp";
            case ".webp": return "image/webp";
            default: return "application/octet-stream";
        }
    }
}