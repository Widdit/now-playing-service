using System;
using System.IO;
using Windows.Storage.Streams;

public static class ThumbnailHelper
{
    public static void SaveThumbnail(IRandomAccessStreamReference thumbnail)
    {
        if (thumbnail == null)
        {
            return;
        }

        const string lockFilePath = "cover_base64.lock";
        const string outputFilePath = "cover_base64.txt";

        // 创建锁文件（表明正在保存新封面。如果外部程序检测到锁文件存在，就暂时先不要访问）
        File.WriteAllTextAsync(lockFilePath, "").GetAwaiter().GetResult();

        IRandomAccessStreamWithContentType thumbnailStream = null;

        try
        {
            // 从流中读取封面图片
            thumbnailStream = thumbnail.OpenReadAsync().GetAwaiter().GetResult();

            byte[] thumbnailBytes = new byte[thumbnailStream.Size];
            using (DataReader reader = new DataReader(thumbnailStream))
            {
                reader.LoadAsync((uint)thumbnailStream.Size).GetAwaiter().GetResult();
                reader.ReadBytes(thumbnailBytes);
            }

            // 转为 Base64 格式字符串，并写入文件
            string base64String = "data:image/jpeg;base64," + Convert.ToBase64String(thumbnailBytes);

            File.WriteAllTextAsync(outputFilePath, base64String).GetAwaiter().GetResult();
        }
        catch (Exception)
        {
            // ignored
        }
        finally
        {
            thumbnailStream?.Dispose();
        }

        // 删除锁文件
        File.Delete(lockFilePath);
    }
}
