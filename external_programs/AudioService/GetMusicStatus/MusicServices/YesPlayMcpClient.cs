using System;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Threading;
using System.Linq;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

internal sealed class YesPlayMcpClient
{
    private const string ProtocolVersion = "2025-03-26";
    private const string SessionHeader = "Mcp-Session-Id";
    private const string NowPlayingUri = "ypm://now-playing";
    private static readonly TimeSpan RequestTimeout = TimeSpan.FromSeconds(2);
    private static readonly TimeSpan ArtworkTimeout = TimeSpan.FromSeconds(4);
    private readonly HttpClient http;
    private readonly Uri endpoint;
    private string sessionId;
    private string protocolVersion;
    private int requestId;

    internal YesPlayMcpClient(int port, string token)
    {
        endpoint = new Uri($"http://127.0.0.1:{port}/mcp");
        http = new HttpClient(new HttpClientHandler { AllowAutoRedirect = false, UseProxy = false })
        {
            Timeout = Timeout.InfiniteTimeSpan
        };
        http.DefaultRequestHeaders.Accept.ParseAdd("application/json, text/event-stream");
        if (!string.IsNullOrEmpty(token))
            http.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);
    }

    internal JObject ReadNowPlaying()
    {
        if (protocolVersion == null)
            Initialize();

        var response = Call("resources/read", new { uri = NowPlayingUri });
        var resource = (response?["contents"] as JArray)?.FirstOrDefault(item =>
            (string)item["uri"] == NowPlayingUri);
        var text = (string)resource?["text"];
        if (text == null)
            throw new InvalidOperationException("MCP 响应中没有当前播放快照");
        return JObject.Parse(text);
    }

    internal (JToken Track, byte[] Image, string MimeType) ReadArtwork()
    {
        var result = Call("tools/call", new
        {
            name = "get_now_playing",
            arguments = new { includeArtwork = true }
        }, timeout: ArtworkTimeout);
        var content = result?["content"] as JArray;
        var text = (string)content?.FirstOrDefault(item => (string)item["type"] == "text")?["text"];
        if ((bool?)result?["isError"] == true || text == null)
            throw new InvalidOperationException("MCP get_now_playing 未返回有效快照");

        var snapshot = JObject.Parse(text);
        if (snapshot["artworkError"] != null)
            Console.Error.WriteLine("[YesPlayMusic MCP] 播放器无法读取当前封面");
        var image = content.FirstOrDefault(item => (string)item["type"] == "image" &&
            (string)item["mimeType"] is "image/jpeg" or "image/png");
        var base64 = (string)image?["data"];
        return (snapshot["track"], string.IsNullOrEmpty(base64) ? null : Convert.FromBase64String(base64),
            (string)image?["mimeType"]);
    }

    private void Initialize()
    {
        var result = Call("initialize", new
        {
            protocolVersion = ProtocolVersion,
            capabilities = new { },
            clientInfo = new { name = "now-playing-service", version = "1.0" }
        });
        if ((string)result?["serverInfo"]?["name"] != "yesplaymusic" ||
            (string)result?["protocolVersion"] != ProtocolVersion)
        {
            ResetSession();
            throw new InvalidOperationException("连接的服务不是兼容的 YesPlayMusic MCP 服务");
        }
        Call("notifications/initialized", new { }, notification: true);
        protocolVersion = ProtocolVersion;
    }

    private JToken Call(string method, object parameters, bool notification = false, TimeSpan? timeout = null)
    {
        var payload = new JObject
        {
            ["jsonrpc"] = "2.0",
            ["method"] = method,
            ["params"] = JObject.FromObject(parameters)
        };
        if (!notification)
            payload["id"] = ++requestId;

        using var request = new HttpRequestMessage(HttpMethod.Post, endpoint);
        request.Content = new StringContent(payload.ToString(Formatting.None), Encoding.UTF8, "application/json");
        if (sessionId != null)
            request.Headers.Add(SessionHeader, sessionId);
        if (method != "initialize")
            request.Headers.Add("MCP-Protocol-Version", ProtocolVersion);

        using var cancellation = new CancellationTokenSource(timeout ?? RequestTimeout);
        using var response = http.SendAsync(request, cancellation.Token).GetAwaiter().GetResult();
        if (response.StatusCode == HttpStatusCode.NotFound && sessionId != null)
            ResetSession();
        response.EnsureSuccessStatusCode();
        if (notification)
            return null;

        var body = JObject.Parse(response.Content.ReadAsStringAsync().GetAwaiter().GetResult());
        if (body["error"] != null)
            throw new InvalidOperationException($"MCP {method} 返回 JSON-RPC 错误 ({(int?)body["error"]["code"]})");
        if (!JToken.DeepEquals(body["id"], payload["id"]) || body["result"] == null)
            throw new InvalidOperationException("MCP 返回了不匹配的 JSON-RPC 响应");
        if (method == "initialize" && response.Headers.TryGetValues(SessionHeader, out var values))
            sessionId = values.FirstOrDefault();
        return body["result"];
    }

    private void ResetSession()
    {
        sessionId = null;
        protocolVersion = null;
    }
}
