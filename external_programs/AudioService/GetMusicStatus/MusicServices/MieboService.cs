using System;
using System.IO;
using System.Net.Http;
using System.Net.WebSockets;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Newtonsoft.Json.Linq;
using CSCore.CoreAudioAPI;

public class MieboService : MusicService
{
    private string title = "";
    private string artist = "";
    private bool paused = true;
    private bool webSocketConnected = false;
    private string prevCoverUrl = "";
    private string port = "";

    public override void Init()
    {
        Main();
    }

    public override string GetMusicStatus(AudioSessionManager2 sessionManager)
    {
        if (!webSocketConnected || string.IsNullOrEmpty(title))
        {
            return "None";
        }

        // 输出结果
        string status = paused ? "Paused" : "Playing";
        return $"{status}\r\n{title + " - " + artist}";
    }

    private async Task Main()
    {
        while (true)
        {
            // 找到咩播进程正在监听的网络端口
            await FindPort();

            // 连接咩播的本地 WebSocket，持续获取音乐状态
            await FetchMusicStatus();

            // 等待 3 秒后重试
            await Task.Delay(3000);
        }
    }

    private async Task FindPort()
    {
        while (true)
        {
            var processes = Process.GetProcessesByName("咩播").ToList();

            if (processes.Any())
            {
                string portFound = NetPortFinder.GetPortByProcessName("咩播");
                if (!string.IsNullOrEmpty(portFound))
                {
                    port = portFound;
                    return;
                }
            }

            // 等待 3 秒后重试
            await Task.Delay(3000);
        }
    }

    private async Task FetchMusicStatus()
    {
        Uri serverUri = new Uri($"ws://127.0.0.1:{port}");

        using (ClientWebSocket clientWebSocket = new ClientWebSocket())
        {
            try
            {
                // 连接 WebSocket 服务器
                await clientWebSocket.ConnectAsync(serverUri, CancellationToken.None);

                webSocketConnected = true;

                // 刚连接上时获取不到歌曲信息，因此显示提示信息
                title = "咩播连接成功";
                artist = "从下一首歌曲开始生效";
                prevCoverUrl = "https://gitee.com/widdit/now-playing/raw/master/spotify_no_cover.jpg";
                SaveThumbnail(prevCoverUrl);

                // 启动定时器，每 30 秒发送一次心跳消息
                Timer timer = new Timer(async _ =>
                {
                    if (clientWebSocket.State == WebSocketState.Open)
                    {
                        byte[] buffer = Encoding.UTF8.GetBytes("[\"ping\"]");
                        await clientWebSocket.SendAsync(new ArraySegment<byte>(buffer), WebSocketMessageType.Text, true, CancellationToken.None);
                    }
                }, null, 0, 30000);

                // 发送 load 消息以获取音乐点播信息
                byte[] buffer = Encoding.UTF8.GetBytes("[\"load\"]");
                await clientWebSocket.SendAsync(new ArraySegment<byte>(buffer), WebSocketMessageType.Text, true, CancellationToken.None);

                // 接收消息
                await ReceiveMessages(clientWebSocket);

                // 停止定时器
                timer.Dispose();
            }
            catch (Exception) {}
        }

        // WebSocket 连接失败或中断
        webSocketConnected = false;
        title = "";
        artist = "";
        paused = true;
    }

    /*
        接收服务器消息
    */
    private async Task ReceiveMessages(ClientWebSocket clientWebSocket)
    {
        byte[] buffer = new byte[2048];
        StringBuilder messageBuilder = new StringBuilder();
        
        while (clientWebSocket.State == WebSocketState.Open)
        {
            try
            {
                WebSocketReceiveResult result = await clientWebSocket.ReceiveAsync(new ArraySegment<byte>(buffer), CancellationToken.None);
                
                messageBuilder.Append(Encoding.UTF8.GetString(buffer, 0, result.Count));

                // 如果是完整的消息，则进行处理
                if (result.EndOfMessage)
                {
                    string message = messageBuilder.ToString();

                    // 处理消息
                    ProcessMessage(message);

                    // 清空 StringBuilder，准备接收下一条消息
                    messageBuilder.Clear();
                }
            }
            catch (Exception)
            {
                break;
            }
        }
    }

    /*
        处理接收到的 JSON 数据
    */
    private void ProcessMessage(string message)
    {
        try
        {
            // 解析为 JArray
            var arr = JArray.Parse(message);

            // 检查格式形如 ["toolkit", "mplayer", "next", {...}] 的消息（说明歌曲切换了）
            if (arr.Count == 4 && arr[0]?.ToString() == "toolkit" && arr[1]?.ToString() == "mplayer" && arr[2]?.ToString() == "next")
            {
                var data = arr[3] as JObject;
                if (data != null)
                {
                    title = data.Value<string>("title");
                    artist = data.Value<string>("artist");
                    paused = false;

                    string coverUrl = data.Value<string>("cover");
                    if (coverUrl != prevCoverUrl)
                    {
                        prevCoverUrl = coverUrl;
                        SaveThumbnail(coverUrl);
                    }
                }
            }
            // 处理歌曲播放/暂停状态的消息
            else if (arr.Count == 2 && arr[0]?.ToString() == "config")
            {
                var jsonString = arr[1]?.ToString();
                if (!string.IsNullOrEmpty(jsonString))
                {
                    var obj = JObject.Parse(jsonString);
                    if ((string)obj["act"] == "update" && (string)obj["store"] == "mplayer" && obj["data"]?["key"]?.ToString() == "system-play")
                    {
                        bool value = obj["data"]?.Value<bool>("value") ?? false;
                        paused = !value;
                    }
                }
            }
        }
        catch (Exception) {}
    }

    /*
        保存歌曲封面
    */
    private void SaveThumbnail(string coverUrl)
    {
        if (string.IsNullOrEmpty(coverUrl))
            return;

        // 创建锁文件（表明正在保存新封面，暂时不要访问）
        File.WriteAllTextAsync("cover_base64.lock", "").GetAwaiter().GetResult();

        // 限制封面尺寸大小
        if (coverUrl.Contains("music.126.net"))  // 网易云音乐
        {
            coverUrl += "?param=500y500";
        }
        
        try
        {
            using (HttpClient client = new HttpClient())
            {
                // 请求图片并获取其内容
                byte[] thumbnailBytes = client.GetByteArrayAsync(coverUrl).GetAwaiter().GetResult();

                // 转为 BASE64 格式字符串，并写到文件中
                string base64String = "data:image/jpeg;base64,";
                base64String += Convert.ToBase64String(thumbnailBytes);
                string filePath = "cover_base64.txt";
                File.WriteAllTextAsync(filePath, base64String).GetAwaiter().GetResult();
            }
        }
        catch (Exception) {}

        // 删除锁文件
        File.Delete("cover_base64.lock");
    }
}
