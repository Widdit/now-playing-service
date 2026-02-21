using System;
using System.IO;
using System.Net.Http;
using Newtonsoft.Json.Linq;
using CSCore.CoreAudioAPI;

public class YesPlayMusicService : MusicService
{

    private string prevCoverUrl = "";

    public override string GetMusicStatus(AudioSessionManager2 sessionManager)
    {
        try
        {
            // 获取播放状态
            string status = GetVolume(sessionManager) > 0.00001 ? "Playing" : "Paused";

            // 获取歌曲信息
            string url = "http://127.0.0.1:27232/player";
            using (HttpClient client = new HttpClient())
            {
                var request = new HttpRequestMessage(HttpMethod.Get, url);
                var response = client.Send(request);
                string responseBody = response.Content.ReadAsStringAsync().Result;

                JObject json = JObject.Parse(responseBody);

                string title = json["currentTrack"]?["name"]?.ToString() ?? "";

                JArray artistsArray = (JArray?)json["currentTrack"]?["ar"];
                string artist = "";
                if (artistsArray != null && artistsArray.Count > 0)
                {
                    string[] names = new string[artistsArray.Count];
                    for (int i = 0; i < artistsArray.Count; i++)
                    {
                        names[i] = artistsArray[i]?["name"]?.ToString() ?? "";
                    }
                    artist = string.Join(" / ", names);
                }

                string coverUrl = json["currentTrack"]?["al"]?["picUrl"]?.ToString() ?? "";
                if (coverUrl != prevCoverUrl)
                {
                    prevCoverUrl = coverUrl;
                    SaveThumbnail(coverUrl);
                }

                // 输出结果
                return $"{status}\r\n{title + " - " + artist}";
            }
        }
        catch (Exception)
        {
            return "None";
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

    private double GetVolume(AudioSessionManager2 sessionManager)
    {
        double volume = 0;

        AudioSessionEnumerator sessionEnumerator = sessionManager.GetSessionEnumerator();

        // 遍历所有会话，寻找匹配的进程
        foreach (AudioSessionControl session in sessionEnumerator)
        {
            if (session == null)
            {
                continue;
            }

            AudioSessionControl2 sessionControl = session.QueryInterface<AudioSessionControl2>();
            if (sessionControl == null || sessionControl.Process == null)
            {
                continue;
            }

            string processName = sessionControl.Process.ProcessName;
            AudioMeterInformation meter = null;

            if (processName.StartsWith("YesPlayMusic"))
            {
                meter = session.QueryInterface<AudioMeterInformation>();
                volume += meter.PeakValue;
            }

            // 释放对象
            meter?.Dispose();
            sessionControl?.Dispose();
            session.Dispose();
        }

        sessionEnumerator?.Dispose();

        return volume;
    }

}