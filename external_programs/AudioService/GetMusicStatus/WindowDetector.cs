using System;
using System.Text;
using System.Diagnostics;
using System.Collections.Generic;
using System.Runtime.InteropServices;

public class WindowDetector
{
    [DllImport("user32.dll", SetLastError = true)]
    private static extern bool EnumWindows(EnumWindowsProc lpEnumFunc, IntPtr lParam);

    [DllImport("user32.dll", SetLastError = true, CharSet = CharSet.Unicode)]
    private static extern int GetWindowText(IntPtr hWnd, StringBuilder lpString, int nMaxCount);

    [DllImport("user32.dll", SetLastError = true)]
    private static extern int GetWindowTextLength(IntPtr hWnd);

    [DllImport("user32.dll", SetLastError = true)]
    private static extern uint GetWindowThreadProcessId(IntPtr hWnd, out uint processId);

    [DllImport("user32.dll", SetLastError = true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    private static extern bool IsWindowVisible(IntPtr hWnd);

    private delegate bool EnumWindowsProc(IntPtr hWnd, IntPtr lParam);

    /// <summary>
    /// 通过窗口句柄获取窗口标题（使用 Unicode，避免编码问题）
    /// </summary>
    public static string GetWindowTitleByHandle(IntPtr hWnd)
    {
        if (hWnd == IntPtr.Zero) return string.Empty;

        int length = GetWindowTextLength(hWnd);
        if (length <= 0) return string.Empty;

        StringBuilder sb = new StringBuilder(length + 1);
        GetWindowText(hWnd, sb, sb.Capacity);
        return sb.ToString();
    }

    /// <summary>
    /// 获取指定进程的主窗口标题（Unicode）
    /// </summary>
    public static string GetWindowTitle(string processName)
    {
        string windowTitle = "";
        bool found = false;

        EnumWindows(delegate (IntPtr hWnd, IntPtr lParam)
        {
            if (found) return false;  // Early exit if already found

            uint processId;
            GetWindowThreadProcessId(hWnd, out processId);

            if (IsWindowVisible(hWnd))
            {
                try
                {
                    string procName = Process.GetProcessById((int)processId).ProcessName;
                    if (string.Equals(procName, processName, StringComparison.OrdinalIgnoreCase))
                    {
                        found = true;
                        windowTitle = GetWindowTitleByHandle(hWnd);
                        return false;  // Stop enumerating
                    }
                }
                catch (Exception) { }  // Ignore errors
            }

            return true;  // Continue enumerating
        }, IntPtr.Zero);

        return windowTitle;
    }

    /// <summary>
    /// 获取指定进程的所有窗口标题（Unicode）
    /// </summary>
    public static List<string> GetWindowTitles(string processName)
    {
        List<string> windowTitles = new List<string>();
        Process[] processes = Process.GetProcessesByName(processName);
        if (processes.Length == 0) return windowTitles;

        uint targetProcessId = (uint)processes[0].Id;

        EnumWindows(new EnumWindowsProc((hWnd, lParam) =>
        {
            uint pid;
            GetWindowThreadProcessId(hWnd, out pid);

            if (pid == targetProcessId)
            {
                string title = GetWindowTitleByHandle(hWnd);
                if (!string.IsNullOrWhiteSpace(title))
                {
                    windowTitles.Add(title);
                }
            }

            return true;  // Continue enumerating
        }), IntPtr.Zero);

        return windowTitles;
    }

    /// <summary>
    /// 获取网易云主窗口句柄：枚举所有 cloudmusic 进程的可见顶层窗口，
    /// 返回标题形如"歌名 - 歌手"（网易云会把主窗口标题改成歌曲名）的窗口句柄。
    /// 显式屏蔽：桌面歌词窗口（标题含"歌词"）、迷你播放器（标题含"迷你"）、SMTC 窗口（标题含"MediaPlayer"）。
    /// </summary>
    public static IntPtr GetPlayerWindowHandle(string processName)
    {
        IntPtr result = IntPtr.Zero;

        Process[] processes = Process.GetProcessesByName(processName);
        if (processes.Length == 0) return IntPtr.Zero;

        HashSet<uint> pids = new HashSet<uint>();
        foreach (Process p in processes)
        {
            pids.Add((uint)p.Id);
            p.Dispose();
        }

        EnumWindows(new EnumWindowsProc((hWnd, lParam) =>
        {
            GetWindowThreadProcessId(hWnd, out uint pid);
            if (!pids.Contains(pid)) return true;
            if (!IsWindowVisible(hWnd)) return true;

            string title = GetWindowTitleByHandle(hWnd);
            if (string.IsNullOrEmpty(title)) return true;
            if (title.Contains("MediaPlayer")) return true;   // SMTC 窗口
            if (title.Contains("歌词")) return true;          // 桌面歌词窗口
            if (title.Contains("迷你")) return true;          // 迷你播放器
            if (!title.Contains(" - ")) return true;          // 主窗口标题形如"歌名 - 歌手"

            result = hWnd;
            return false;  // 已找到主窗口，停止枚举
        }), IntPtr.Zero);

        return result;
    }
}
