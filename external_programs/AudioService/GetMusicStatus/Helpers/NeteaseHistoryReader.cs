using System;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;
using Newtonsoft.Json.Linq;

/// <summary>
/// 从网易云音乐 3.x 自己维护的播放历史数据库中读取最近一次开播时间。
///
/// 这里只在歌曲/开播时间变化时向 Now Playing 提供一次初始进度，之后仍由
/// Now Playing 原有计时器负责推进与暂停，避免把本地数据库当作高频时间轴。
/// </summary>
public static class NeteaseHistoryReader
{
    private const int SQLITE_OK = 0;
    private const int SQLITE_ROW = 100;
    private const int SQLITE_OPEN_READONLY = 0x00000001;
    private const int SQLITE_OPEN_NOMUTEX = 0x00008000;

    private static readonly string DatabasePath = Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
        "NetEase", "CloudMusic", "Library", "webdb.dat");

    private static readonly object CacheLock = new object();
    private static long _lastDatabaseStamp = long.MinValue;
    private static HistorySnapshot _cachedSnapshot;

    /// <summary>
    /// 在窗口标题与网易云最近播放记录匹配时，返回基于开播时间估算的初始进度。
    /// </summary>
    /// <param name="windowTitle">网易云当前播放窗口标题。</param>
    /// <param name="currentSeconds">估算的当前播放秒数。</param>
    /// <param name="totalSeconds">歌曲总时长（秒）。</param>
    /// <param name="playtimeMilliseconds">网易云记录的开播 Unix 时间戳（毫秒）。</param>
    /// <returns>记录有效且与当前窗口标题匹配时返回 true。</returns>
    public static bool TryGetInitialProgress(
        string windowTitle,
        out int currentSeconds,
        out int totalSeconds,
        out long playtimeMilliseconds)
    {
        currentSeconds = -1;
        totalSeconds = -1;
        playtimeMilliseconds = -1;

        HistorySnapshot snapshot = ReadLatestCached();
        if (snapshot == null || string.IsNullOrWhiteSpace(windowTitle))
        {
            return false;
        }

        // 防止数据库尚未切到新曲时，把上一首的进度套到新标题上。
        if (string.IsNullOrWhiteSpace(snapshot.Title)
            || windowTitle.IndexOf(snapshot.Title, StringComparison.OrdinalIgnoreCase) < 0)
        {
            return false;
        }

        long elapsedMilliseconds = DateTimeOffset.Now.ToUnixTimeMilliseconds() - snapshot.PlaytimeMilliseconds;
        if (elapsedMilliseconds < -1000 || snapshot.DurationMilliseconds <= 0)
        {
            return false;
        }

        // 允许少量时钟/取整误差；明显超出歌曲时长则视为陈旧记录。
        if (elapsedMilliseconds > snapshot.DurationMilliseconds + 3000)
        {
            return false;
        }

        elapsedMilliseconds = Math.Max(0, Math.Min(elapsedMilliseconds, snapshot.DurationMilliseconds));
        currentSeconds = (int)(elapsedMilliseconds / 1000);
        totalSeconds = Math.Max(1, (int)(snapshot.DurationMilliseconds / 1000));
        playtimeMilliseconds = snapshot.PlaytimeMilliseconds;
        return true;
    }

    private static HistorySnapshot ReadLatestCached()
    {
        try
        {
            if (!File.Exists(DatabasePath))
            {
                return null;
            }

            string walPath = DatabasePath + "-wal";
            long databaseStamp = File.GetLastWriteTimeUtc(DatabasePath).Ticks;
            if (File.Exists(walPath))
            {
                databaseStamp = Math.Max(databaseStamp, File.GetLastWriteTimeUtc(walPath).Ticks);
            }

            lock (CacheLock)
            {
                if (_cachedSnapshot != null && databaseStamp == _lastDatabaseStamp)
                {
                    return _cachedSnapshot;
                }

                HistorySnapshot latest = ReadLatestFromDatabase();
                if (latest != null)
                {
                    _cachedSnapshot = latest;
                    _lastDatabaseStamp = databaseStamp;
                }

                return _cachedSnapshot;
            }
        }
        catch
        {
            // 数据库读取只是校准快路径；失败时保持原有无 Progress 行行为。
            return null;
        }
    }

    private static HistorySnapshot ReadLatestFromDatabase()
    {
        IntPtr database = IntPtr.Zero;
        IntPtr statement = IntPtr.Zero;

        try
        {
            int openResult = sqlite3_open_v2(
                ToUtf8Z(DatabasePath),
                out database,
                SQLITE_OPEN_READONLY | SQLITE_OPEN_NOMUTEX,
                IntPtr.Zero);
            if (openResult != SQLITE_OK || database == IntPtr.Zero)
            {
                return null;
            }

            sqlite3_busy_timeout(database, 25);

            const string sql = "SELECT playtime, jsonStr FROM historyTracks ORDER BY playtime DESC LIMIT 1";
            int prepareResult = sqlite3_prepare_v2(
                database,
                ToUtf8Z(sql),
                -1,
                out statement,
                IntPtr.Zero);
            if (prepareResult != SQLITE_OK || statement == IntPtr.Zero || sqlite3_step(statement) != SQLITE_ROW)
            {
                return null;
            }

            long playtime = sqlite3_column_int64(statement, 0);
            IntPtr jsonPointer = sqlite3_column_text(statement, 1);
            string json = jsonPointer == IntPtr.Zero ? null : Marshal.PtrToStringUTF8(jsonPointer);
            if (playtime <= 0 || string.IsNullOrWhiteSpace(json))
            {
                return null;
            }

            JObject track = JObject.Parse(json);
            string title = track.Value<string>("name");
            long duration = track.Value<long?>("duration") ?? 0;
            if (string.IsNullOrWhiteSpace(title) || duration <= 0)
            {
                return null;
            }

            return new HistorySnapshot
            {
                Title = title,
                PlaytimeMilliseconds = playtime,
                DurationMilliseconds = duration
            };
        }
        finally
        {
            if (statement != IntPtr.Zero)
            {
                sqlite3_finalize(statement);
            }
            if (database != IntPtr.Zero)
            {
                sqlite3_close(database);
            }
        }
    }

    private static byte[] ToUtf8Z(string value)
    {
        return Encoding.UTF8.GetBytes(value + "\0");
    }

    private sealed class HistorySnapshot
    {
        public string Title { get; set; }
        public long PlaytimeMilliseconds { get; set; }
        public long DurationMilliseconds { get; set; }
    }

    [DllImport("winsqlite3.dll", CallingConvention = CallingConvention.Cdecl)]
    private static extern int sqlite3_open_v2(byte[] filename, out IntPtr database, int flags, IntPtr vfs);

    [DllImport("winsqlite3.dll", CallingConvention = CallingConvention.Cdecl)]
    private static extern int sqlite3_busy_timeout(IntPtr database, int milliseconds);

    [DllImport("winsqlite3.dll", CallingConvention = CallingConvention.Cdecl)]
    private static extern int sqlite3_prepare_v2(
        IntPtr database,
        byte[] sql,
        int byteCount,
        out IntPtr statement,
        IntPtr tail);

    [DllImport("winsqlite3.dll", CallingConvention = CallingConvention.Cdecl)]
    private static extern int sqlite3_step(IntPtr statement);

    [DllImport("winsqlite3.dll", CallingConvention = CallingConvention.Cdecl)]
    private static extern long sqlite3_column_int64(IntPtr statement, int column);

    [DllImport("winsqlite3.dll", CallingConvention = CallingConvention.Cdecl)]
    private static extern IntPtr sqlite3_column_text(IntPtr statement, int column);

    [DllImport("winsqlite3.dll", CallingConvention = CallingConvention.Cdecl)]
    private static extern int sqlite3_finalize(IntPtr statement);

    [DllImport("winsqlite3.dll", CallingConvention = CallingConvention.Cdecl)]
    private static extern int sqlite3_close(IntPtr database);
}

