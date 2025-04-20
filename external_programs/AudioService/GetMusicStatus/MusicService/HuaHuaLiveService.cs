using System;
using System.IO;
using System.Net.Http;
using System.Net.WebSockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Newtonsoft.Json.Linq;
using CSCore.CoreAudioAPI;

public class HuaHuaLiveService : MusicService
{
    private string title = "";
    private string artist = "";
    private bool paused = true;
    private bool webSocketConnected = false;
    private string prevCoverUrl = "";

    public override void Init()
    {
        GetMusicStatus();
    }

    public override void PrintMusicStatus(AudioSessionManager2 sessionManager)
    {
        Console.OutputEncoding = Encoding.UTF8;

        if (!webSocketConnected || string.IsNullOrEmpty(title))
        {
            Console.WriteLine("None");
            return;
        }

        UpdatePlayingStatus();

        // 输出结果
        string status = paused ? "Paused" : "Playing";
        Console.WriteLine(status);
        Console.WriteLine(title + " - " + artist);
    }

    private async Task GetMusicStatus()
    {
        Uri serverUri = new Uri("ws://127.0.0.1:9780/openapi/music/list/ws");

        while (true)
        {
            // 连接前需调用一次 HTTP 接口来获得初始歌曲信息（因为该软件的 WebSocket 仅在歌单变化时发送消息）
            UpdateSongInfo();

            using (ClientWebSocket clientWebSocket = new ClientWebSocket())
            {
                try
                {
                    // 连接 WebSocket 服务器
                    await clientWebSocket.ConnectAsync(serverUri, CancellationToken.None);

                    webSocketConnected = true;

                    // 启动定时器，每 30 秒发送一次心跳消息
                    Timer timer = new Timer(async _ =>
                    {
                        if (clientWebSocket.State == WebSocketState.Open)
                        {
                            string heartBeatMessage = "HeartBeat";
                            byte[] buffer = Encoding.UTF8.GetBytes(heartBeatMessage);
                            await clientWebSocket.SendAsync(new ArraySegment<byte>(buffer), WebSocketMessageType.Text, true, CancellationToken.None);
                        }
                    }, null, 0, 30000);

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

            // 等待 1 秒后重试
            await Task.Delay(1000);
        }
    }

    /*
        接收服务器消息
    */
    private async Task ReceiveMessages(ClientWebSocket clientWebSocket)
    {
        byte[] buffer = new byte[1024];
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
        if (message == "HeartBeat")
        {
            return;
        }

        try
        {
            // 解析 JSON 数据
            JArray jsonArray = JArray.Parse(message);

            JObject song = (JObject)jsonArray[0];

            title = song["name"].ToString();
            artist = song["artist"].ToString();
            artist = artist.Replace("&", " / ");

            string coverUrl = song["cover"].ToString();
            if (coverUrl != prevCoverUrl)
            {
                prevCoverUrl = coverUrl;
                SaveThumbnail(coverUrl);
            }
        }
        catch (Exception) {}
    }

    /*
        更新播放状态
    */
    private void UpdatePlayingStatus()
    {
        try
        {
            using (HttpClient client = new HttpClient())
            {
                // 发送 GET 请求
                HttpResponseMessage response = client.GetAsync("http://127.0.0.1:9780/openapi/music/play/status").GetAwaiter().GetResult();
                response.EnsureSuccessStatusCode();

                // 读取响应内容
                string responseBody = response.Content.ReadAsStringAsync().GetAwaiter().GetResult();

                // 解析 JSON 数据
                JObject jsonObject = JObject.Parse(responseBody);
                paused = jsonObject["data"]["paused"].Value<bool>();
            }
        }
        catch (Exception) {}
    }

    /*
        更新歌曲信息
    */
    private void UpdateSongInfo()
    {
        try
        {
            using (HttpClient client = new HttpClient())
            {
                // 发送 GET 请求
                HttpResponseMessage response = client.GetAsync("http://127.0.0.1:9780/openapi/music/play/list").GetAwaiter().GetResult();
                response.EnsureSuccessStatusCode();

                // 读取响应内容
                string responseBody = response.Content.ReadAsStringAsync().GetAwaiter().GetResult();

                // 解析 JSON 数据
                JObject jsonObject = JObject.Parse(responseBody);
                JArray jsonArray = (JArray)jsonObject["data"];
                JObject song = (JObject)jsonArray[0];

                title = song["name"].ToString();
                artist = song["artist"].ToString();
                artist = artist.Replace("&", " / ");

                string coverUrl = song["cover"].ToString();
                SaveThumbnail(coverUrl);
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

        // 限制封面尺寸大小
        if (coverUrl.Contains("music.126.net"))  // 网易云音乐
        {
            coverUrl += "?param=500y500";
        }
        else if (coverUrl.Contains("qq.com"))  // QQ 音乐
        {
            coverUrl = coverUrl.Replace("800x800", "500x500");
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
    }
}
