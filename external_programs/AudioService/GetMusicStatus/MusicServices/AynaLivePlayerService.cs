using System;
using System.IO;
using System.Net.Http;
using System.Net.WebSockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Newtonsoft.Json.Linq;
using CSCore.CoreAudioAPI;

public class AynaLivePlayerService : MusicService
{
    private string title = "";
    private string artist = "";
    private bool paused = true;
    private bool webSocketConnected = false;

    public override void Init()
    {
        FetchMusicStatus();
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

    private async Task FetchMusicStatus()
    {
        Uri serverUri = new Uri("ws://localhost:29629/wsinfo");

        while (true)
        {
            using (ClientWebSocket clientWebSocket = new ClientWebSocket())
            {
                try
                {
                    // 连接 WebSocket 服务器
                    await clientWebSocket.ConnectAsync(serverUri, CancellationToken.None);

                    webSocketConnected = true;

                    // 接收消息
                    await ReceiveMessages(clientWebSocket);
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

                    // 仅处理短的消息（长的消息是歌单列表，是无关信息）
                    if (message.Length < 1024 * 2)
                    {
                        // 处理消息
                        ProcessMessage(message);
                    }

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
            // 解析 JSON 数据
            JObject json = JObject.Parse(message);

            if (json.ContainsKey("EventID"))
            {
                string eventID = json["EventID"].ToString();

                if (eventID == "update.player.playing" || eventID == "cmd.player.op.play")  // 歌曲切换了
                {
                    JToken info = json["Data"]["Media"]["Info"];

                    if (info["Title"] != null)  // 返回结果为大驼峰字段（兼容不同版本）
                    {
                        if (!string.IsNullOrEmpty(info["Title"].ToString()))
                        {
                            title = info["Title"].ToString();
                            artist = info["Artist"].ToString();
                            artist = artist.Replace(",", " / ");

                            string coverUrl = info["Cover"]["Url"].ToString();
                            SaveThumbnail(coverUrl);
                        }
                    }
                    else  // 返回结果为小驼峰字段（兼容不同版本）
                    {
                        if (!string.IsNullOrEmpty(info["title"].ToString()))
                        {
                            title = info["title"].ToString();
                            artist = info["artist"].ToString();
                            artist = artist.Replace(",", " / ");

                            string coverUrl = info["cover"]["url"].ToString();
                            SaveThumbnail(coverUrl);
                        }
                    }
                }
                else if (eventID == "update.player.property.pause")  // 播放状态改变了
                {
                    paused = json["Data"]["Paused"].ToObject<bool>();
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
