using System;
using System.IO;
using System.Net.Http;
using System.Threading.Tasks;
using Newtonsoft.Json.Linq;
using CSCore.CoreAudioAPI;

public class BQLivePlayerService : MusicService
{
    private string title = "";
    private string artist = "";
    private bool paused = true;
    private string prevCoverUrl = "";

    public override void Init()
    {
        FetchMusicStatus();
    }

    public override string GetMusicStatus(AudioSessionManager2 sessionManager)
    {
        if (string.IsNullOrEmpty(title))
        {
            return "None";
        }

        // 输出结果
        string status = paused ? "Paused" : "Playing";
        return $"{status}\r\n{title + " - " + artist}";
    }

    private async Task FetchMusicStatus()
    {
        while (true)
        {
            try
            {
                using (HttpClient client = new HttpClient())
                {
                    // 发送 GET 请求
                    HttpResponseMessage response = client.GetAsync("http://127.0.0.1:62233/api/playinfo").GetAwaiter().GetResult();
                    response.EnsureSuccessStatusCode();

                    // 读取响应内容
                    string responseBody = response.Content.ReadAsStringAsync().GetAwaiter().GetResult();

                    // 解析 JSON 数据
                    JObject jsonObject = JObject.Parse(responseBody);

                    title = jsonObject["title"].ToString();

                    JArray artistNameArray = (JArray)jsonObject["artistName"];
                    artist = artistNameArray[0].ToString();
                    for (int i = 1; i < artistNameArray.Count; i++)
                    {
                        artist += ", " + artistNameArray[i].ToString();
                    }

                    paused = !(jsonObject["playStatus"].Value<bool>());

                    // 保存封面
                    string coverUrl = jsonObject["coverUrl"].ToString();
                    if (coverUrl != prevCoverUrl)
                    {
                        prevCoverUrl = coverUrl;
                        SaveThumbnail(coverUrl);
                    }
                }
            }
            catch (Exception) {
                title = "";
                artist = "";
                paused = true;
            }

            // 间隔 200 ms
            await Task.Delay(200);
        }
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
        if (coverUrl.EndsWith(".jpg") || coverUrl.EndsWith(".png"))
        {
            coverUrl += "@500w_500h";
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
