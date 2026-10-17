using System;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Net.Http;
using CSCore.CoreAudioAPI;
using Newtonsoft.Json.Linq;

// 只读取元数据和进度；歌词仍由 Now Playing 原有逻辑处理。
public class SPlayerNextService : MusicService
{
    private readonly HttpClient client;
    private readonly string endpoint;
    private readonly Stopwatch clock = Stopwatch.StartNew();
    private long nextPoll;
    private int failures;
    private string output = "None";
    private string coverKey;

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
            output = FormatSnapshot(snapshot);
            if (output == "None")
            {
                ResetCover();
            }
            else
            {
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
}
