using System;
using System.IO;
using System.Security.Cryptography;
using System.Threading;
using System.Threading.Tasks;
using Windows.Storage.Streams;

public static class ThumbnailHelper
{
    private const string OutputFilePath = "cover_base64.txt";
    private const string LockFilePath = "cover_base64.lock";

    // SMTC 的标题/歌手信息与封面图片由播放器异步上报，二者并不同步更新。
    // 在切歌时，标题往往先于封面完成更新，如果仅在标题变化的瞬间读取封面，
    // 极易拿到尚未刷新的旧封面（即 "封面残留"）；若此次读取恰好失败且无重试，
    // 封面则会长期停留在某首更早的歌曲上（即 "封面卡死"）。
    // 为此，采用带重试的后台更新策略，并通过哈希比对确认封面内容已完成切换。
    private const int MaxRetries = 10;
    private const int RetryDelayMs = 150;

    // 每次切歌时递增的版本号，用于识别并丢弃已过期的后台任务，
    // 防止旧任务晚于新任务完成时以旧封面覆盖新封面。
    private static long currentVersion = 0;

    // 上一次成功保存的封面哈希值，用于判断新获取的封面是否已完成切换。
    private static string lastSavedHash = null;
    private static readonly object hashLock = new object();

    /// <summary>
    /// 异步更新封面。
    /// 会在后台线程中通过 <paramref name="thumbnailProvider"/> 重新拉取最新的封面引用并重试多次，
    /// 直到获取到的封面内容与"上一首歌保存的封面"不同（即确认封面已经更新），或重试次数用尽为止，
    /// 从而尽量避免"封面残留"以及"封面长期卡在某一首歌不更新"的问题。
    /// </summary>
    /// <param name="thumbnailProvider">
    /// 获取最新缩略图引用的委托。注意：每次调用都必须重新从 SMTC 拉取一遍最新的媒体属性
    /// （而不是复用调用方之前已经拿到手的 Thumbnail 对象），
    /// 否则重试是没有意义的（拿到的会一直是同一个、可能过期的引用）。
    /// </param>
    public static void UpdateThumbnailAsync(Func<IRandomAccessStreamReference> thumbnailProvider)
    {
        if (thumbnailProvider == null)
        {
            return;
        }

        long myVersion = Interlocked.Increment(ref currentVersion);

        Task.Run(() =>
        {
            bool lockCreated = false;

            try
            {
                // 立即创建锁文件，防止 Java 端在封面确认更新完成之前读到"过渡阶段"的封面
                TryCreateLockFile();
                lockCreated = true;

                string previousHash;
                lock (hashLock)
                {
                    previousHash = lastSavedHash;
                }

                byte[] bestBytes = null;
                string bestHash = null;

                for (int attempt = 0; attempt < MaxRetries; attempt++)
                {
                    // 检测到更新版本的切歌任务，当前任务已过期，直接退出；
                    // 锁文件的清理由最新任务在完成后负责。
                    if (Interlocked.Read(ref currentVersion) != myVersion)
                    {
                        return;
                    }

                    byte[] bytes = TryReadThumbnailBytes(thumbnailProvider());
                    if (bytes != null && bytes.Length > 0)
                    {
                        string hash = ComputeHash(bytes);
                        bestBytes = bytes;
                        bestHash = hash;

                        // 封面内容与上一首歌不同，说明 SMTC 已经完成了封面更新，无需继续等待
                        if (hash != previousHash)
                        {
                            break;
                        }
                    }

                    Thread.Sleep(RetryDelayMs);
                }

                // 重新检查任务是否过期
                if (Interlocked.Read(ref currentVersion) != myVersion)
                {
                    return;
                }

                // 重试结束后，使用拿到的最后一次结果兜底保存
                // 即使它和上一首歌的封面相同，也好过完全不更新导致封面长期卡在很久之前的某一首歌上
                if (bestBytes != null)
                {
                    WriteCoverFile(bestBytes);

                    lock (hashLock)
                    {
                        lastSavedHash = bestHash;
                    }
                }
            }
            finally
            {
                // 只有当前任务仍然是最新任务时，才由它负责删除锁文件
                if (lockCreated && Interlocked.Read(ref currentVersion) == myVersion)
                {
                    TryDeleteLockFile();
                }
            }
        });
    }

    /// <summary>
    /// 尝试从缩略图引用中读取原始字节数据，失败时返回 null（不抛出异常，方便重试）。
    /// </summary>
    private static byte[] TryReadThumbnailBytes(IRandomAccessStreamReference thumbnail)
    {
        if (thumbnail == null)
        {
            return null;
        }

        IRandomAccessStreamWithContentType thumbnailStream = null;

        try
        {
            thumbnailStream = thumbnail.OpenReadAsync().GetAwaiter().GetResult();

            byte[] thumbnailBytes = new byte[thumbnailStream.Size];
            using (DataReader reader = new DataReader(thumbnailStream))
            {
                reader.LoadAsync((uint)thumbnailStream.Size).GetAwaiter().GetResult();
                reader.ReadBytes(thumbnailBytes);
            }

            return thumbnailBytes;
        }
        catch (Exception)
        {
            return null;
        }
        finally
        {
            thumbnailStream?.Dispose();
        }
    }

    /// <summary>
    /// 计算字节数组的哈希值，用于比较两次封面内容是否相同。
    /// </summary>
    private static string ComputeHash(byte[] bytes)
    {
        using (var sha256 = SHA256.Create())
        {
            byte[] hashBytes = sha256.ComputeHash(bytes);
            return Convert.ToBase64String(hashBytes);
        }
    }

    /// <summary>
    /// 将封面字节数据转为 Base64 并写入输出文件。
    /// </summary>
    private static void WriteCoverFile(byte[] thumbnailBytes)
    {
        try
        {
            string base64String = "data:image/jpeg;base64," + Convert.ToBase64String(thumbnailBytes);
            File.WriteAllTextAsync(OutputFilePath, base64String).GetAwaiter().GetResult();
        }
        catch (Exception)
        {
            // ignored
        }
    }

    private static void TryCreateLockFile()
    {
        try
        {
            File.WriteAllTextAsync(LockFilePath, "").GetAwaiter().GetResult();
        }
        catch (Exception)
        {
            // ignored
        }
    }

    private static void TryDeleteLockFile()
    {
        try
        {
            File.Delete(LockFilePath);
        }
        catch (Exception)
        {
            // ignored
        }
    }
}
