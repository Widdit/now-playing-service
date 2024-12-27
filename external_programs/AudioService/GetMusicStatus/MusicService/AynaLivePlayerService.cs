using System;
using System.Net.WebSockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Newtonsoft.Json.Linq;
using CSCore.CoreAudioAPI;

public class AynaLivePlayerService
{
    private static string title = "";
    private static string artist = "";
    private static bool paused = true;
    private static bool webSocketConnected = false;

    public static void PrintMusicStatus(AudioSessionManager2 sessionManager)
    {
        Console.OutputEncoding = Encoding.UTF8;

        if (!webSocketConnected || string.IsNullOrEmpty(title))
        {
            Console.WriteLine("None");
            return;
        }

        // 输出结果
        string status = paused ? "Paused" : "Playing";
        Console.WriteLine(status);
        Console.WriteLine(title + " - " + artist);
    }

    public static async Task StartMusicStatusMonitor()
    {
        while (true)
        {
            await GetMusicStatus();
            await Task.Delay(1000);  // 连接中断或失败，等待 1 秒后重试
        }
    }

    private static async Task GetMusicStatus()
    {
        Uri serverUri = new Uri("ws://localhost:29629/wsinfo");

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
    }

    // 接收服务器消息
    private static async Task ReceiveMessages(ClientWebSocket clientWebSocket)
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

    // 处理接收到的 JSON 数据
    private static void ProcessMessage(string message)
    {
        try
        {
            // 解析 JSON 数据
            JObject json = JObject.Parse(message);

            if (json.ContainsKey("EventID"))
            {
                string eventID = json["EventID"].ToString();

                if (eventID == "update.player.playing")
                {
                    title = json["Data"]["Media"]["Info"]["Title"].ToString();
                    artist = json["Data"]["Media"]["Info"]["Artist"].ToString();
                }
                else if (eventID == "update.player.property.pause")
                {
                    paused = json["Data"]["Paused"].ToObject<bool>();
                }
            }
        }
        catch (Exception) {}
    }
}
