using System;
using System.Diagnostics;
using System.Net.Http;
using System.Threading.Tasks;
using CSCore.CoreAudioAPI;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

internal sealed class YesPlayMcpService : MusicService
{
    private const long PollIntervalMs = 500;
    private readonly YesPlayMcpClient client;
    private readonly Stopwatch clock = Stopwatch.StartNew();
    private long nextPoll;
    private string output = "None";
    private string lastError;
    private JToken artworkTrack;

    internal YesPlayMcpService(YesPlayMcpClient client) => this.client = client;

    public override void Init() => YesPlayCover.Reset();

    public override string GetMusicStatus(AudioSessionManager2 sessionManager)
    {
        if (clock.ElapsedMilliseconds < nextPoll)
            return output;
        try
        {
            var snapshot = client.ReadNowPlaying();
            var status = FormatSnapshot(snapshot);
            output = status;
            UpdateArtwork(status == "None" ? null : snapshot["track"]);
            lastError = null;
        }
        catch (Exception error) when (error is HttpRequestException or TaskCanceledException
                                     or InvalidOperationException or JsonException or FormatException or OverflowException)
        {
            var message = error is HttpRequestException { StatusCode: System.Net.HttpStatusCode.Unauthorized }
                ? "身份验证失败 (401)，请检查 YesPlayMusic MCP Token。"
                : error.Message;
            if (message != lastError)
                Console.Error.WriteLine($"[YesPlayMusic MCP] {message}");
            lastError = message;
            output = "None";
            UpdateArtwork(null);
        }
        finally
        {
            nextPoll = clock.ElapsedMilliseconds + PollIntervalMs;
        }
        return output;
    }

    private void UpdateArtwork(JToken track)
    {
        if (JToken.DeepEquals(artworkTrack, track))
            return;

        YesPlayCover.Write();
        artworkTrack = track;
        if (track == null)
            return;

        try
        {
            var artwork = client.ReadArtwork();
            // A track may have changed between the snapshot and the artwork call.
            if (!JToken.DeepEquals(track, artwork.Track))
                artworkTrack = null;
            else if (artwork.Image != null)
                YesPlayCover.Write(artwork.Image);
        }
        catch (Exception error) when (error is HttpRequestException or TaskCanceledException
                                     or InvalidOperationException or JsonException or FormatException)
        {
            Console.Error.WriteLine($"[YesPlayMusic MCP] 封面读取失败: {error.Message}");
        }
    }

    private static string FormatSnapshot(JObject snapshot)
    {
        if (snapshot["hasQueue"]?.Type != JTokenType.Boolean || snapshot["playing"]?.Type != JTokenType.Boolean)
            throw new InvalidOperationException("MCP 播放快照缺少有效的播放状态");
        if (!(bool)snapshot["hasQueue"] || string.IsNullOrWhiteSpace((string)snapshot["track"]?["title"]))
            return "None";

        var track = snapshot["track"];
        string title = Clean((string)track["title"]);
        string artist = Clean((string)track["artist"]);
        string status = (bool)snapshot["playing"] ? "Playing" : "Paused";
        int duration = (int)((double?)snapshot["duration"] ?? 0);
        int progress = duration > 0 && snapshot["position"] != null
            ? (int)Math.Clamp((double)snapshot["position"], 0, duration)
            : -1;
        return $"{status}\r\n{title} - {artist}\r\nProgress:{progress}|{(progress < 0 ? -1 : duration)}";
    }

    private static string Clean(string value) => (value ?? "").Replace('\r', ' ').Replace('\n', ' ').Trim();
}
