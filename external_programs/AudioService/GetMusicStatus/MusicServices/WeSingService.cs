using System;
using System.Collections.Generic;
using System.Windows.Automation;
using CSCore.CoreAudioAPI;

public class WeSingService : MusicService
{
    // 上次读取到的进度秒数，用于判断播放/暂停
    private int _lastProgressSeconds = -1;
    // 上次进度变化的时间戳
    private DateTime _lastProgressChangeTime = DateTime.MinValue;
    // 缓存的播放状态
    private string _cachedStatus = "None";

    public override string GetMusicStatus(AudioSessionManager2 sessionManager)
    {
        // 1. 获取窗口标题（WeSing 有多个可见窗口，需要找到主窗口）
        string windowTitle = "";
        List<string> titles = WindowDetector.GetWindowTitles("WeSing");
        foreach (string t in titles)
        {
            if (t.StartsWith("全民K歌 - ") && t.Length > "全民K歌 - ".Length)
            {
                windowTitle = t;
                break;
            }
        }

        if (string.IsNullOrEmpty(windowTitle))
        {
            return "None";
        }

        // 2. 通过 UI Automation 读取进度
        int currentSec = -1;
        int totalSec = -1;

        try
        {
            AutomationElement root = FindWeSingWindow();
            if (root != null)
            {
                ParseProgress(root, out currentSec, out totalSec);
            }
        }
        catch (Exception)
        {
            // UI Automation 失败时忽略，退化为无进度模式
        }

        // 3. 通过进度是否变化来判断播放/暂停
        string status = DetermineStatus(currentSec);

        // 4. 构建输出
        if (currentSec >= 0 && totalSec > 0)
        {
            return $"{status}\r\n{windowTitle}\r\nProgress:{currentSec}|{totalSec}";
        }
        else
        {
            return $"{status}\r\n{windowTitle}";
        }
    }

    /// <summary>
    /// 查找全民K歌播放窗口的 AutomationElement（标题为 "全民K歌 - {歌曲名}" 的窗口）
    /// </summary>
    private AutomationElement FindWeSingWindow()
    {
        try
        {
            var processes = System.Diagnostics.Process.GetProcessesByName("WeSing");
            if (processes.Length == 0) return null;

            int pid = processes[0].Id;

            // 释放所有 Process 对象，防止句柄泄漏
            foreach (var proc in processes)
            {
                proc.Dispose();
            }

            AutomationElement desktop = AutomationElement.RootElement;
            var pidCondition = new PropertyCondition(AutomationElement.ProcessIdProperty, pid);
            AutomationElementCollection windows = desktop.FindAll(TreeScope.Children, pidCondition);

            // 找到标题以 "全民K歌 - " 开头的播放窗口（包含进度控件）
            foreach (AutomationElement win in windows)
            {
                try
                {
                    string name = win.Current.Name;
                    if (name != null && name.StartsWith("全民K歌 - ") && name.Length > "全民K歌 - ".Length)
                    {
                        return win;
                    }
                }
                catch (Exception) { }
            }
        }
        catch (Exception)
        {
            // 忽略
        }

        return null;
    }

    /// <summary>
    /// 从 UI Automation 树中解析进度文本 (格式: "00:08 | 04:16")
    /// </summary>
    private void ParseProgress(AutomationElement root, out int currentSec, out int totalSec)
    {
        currentSec = -1;
        totalSec = -1;

        try
        {
            // 搜索所有文本元素，查找匹配进度格式的
            var textCondition = new PropertyCondition(
                AutomationElement.ControlTypeProperty, ControlType.Text);

            AutomationElementCollection textElements = root.FindAll(TreeScope.Descendants, textCondition);

            foreach (AutomationElement elem in textElements)
            {
                string name = elem.Current.Name;
                if (string.IsNullOrEmpty(name)) continue;

                // 尝试匹配 "00:08 | 04:16" 或 "00:08|04:16" 格式
                if (TryParseProgressText(name, out currentSec, out totalSec))
                {
                    return;
                }
            }
        }
        catch (Exception)
        {
            // 忽略 UI Automation 异常
        }
    }

    /// <summary>
    /// 解析进度文本，支持 "MM:SS | MM:SS" 和 "MM:SS|MM:SS" 格式
    /// </summary>
    private bool TryParseProgressText(string text, out int currentSec, out int totalSec)
    {
        currentSec = -1;
        totalSec = -1;

        // 移除空格
        string cleaned = text.Replace(" ", "");

        // 查找 "|" 分隔符
        int pipeIndex = cleaned.IndexOf('|');
        if (pipeIndex < 0) return false;

        string currentPart = cleaned.Substring(0, pipeIndex);
        string totalPart = cleaned.Substring(pipeIndex + 1);

        if (TryParseTimeString(currentPart, out currentSec) &&
            TryParseTimeString(totalPart, out totalSec))
        {
            return true;
        }

        currentSec = -1;
        totalSec = -1;
        return false;
    }

    /// <summary>
    /// 解析 "MM:SS" 格式的时间字符串为秒数
    /// </summary>
    private bool TryParseTimeString(string timeStr, out int seconds)
    {
        seconds = 0;
        string[] parts = timeStr.Split(':');
        if (parts.Length != 2) return false;

        if (int.TryParse(parts[0], out int minutes) && int.TryParse(parts[1], out int secs))
        {
            seconds = minutes * 60 + secs;
            return true;
        }

        return false;
    }

    /// <summary>
    /// 通过进度变化判断播放状态
    /// 如果进度在变化 → Playing；如果进度停滞 → Paused
    /// </summary>
    private string DetermineStatus(int currentSec)
    {
        if (currentSec < 0)
        {
            // 无法获取进度，返回缓存状态
            return _cachedStatus == "None" ? "Playing" : _cachedStatus;
        }

        if (currentSec != _lastProgressSeconds)
        {
            _lastProgressSeconds = currentSec;
            _lastProgressChangeTime = DateTime.Now;
            _cachedStatus = "Playing";
            return "Playing";
        }

        // 进度未变化，超过 1.5 秒认为暂停
        if ((DateTime.Now - _lastProgressChangeTime).TotalMilliseconds > 1500)
        {
            _cachedStatus = "Paused";
            return "Paused";
        }

        return _cachedStatus;
    }
}
