using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using Interop.UIAutomationClient;
using CSCore.CoreAudioAPI;

public class WeSingService : MusicService
{
    // 用于在开发阶段控制是否打印异常信息
    private const bool PRINT_EXCEPTION_LOG = false;

    // 上次读取到的进度秒数，用于判断播放/暂停
    private int _lastProgressSeconds = -1;
    // 上次进度变化的时间戳
    private DateTime _lastProgressChangeTime = DateTime.MinValue;
    // 缓存的播放状态
    private string _cachedStatus = "None";

    private IUIAutomation _automation;

    private const int UIA_ProcessIdPropertyId = 30002;
    private const int UIA_ControlTypePropertyId = 30003;
    private const int UIA_TextControlTypeId = 50020;

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
        IUIAutomationElement root = null;

        try
        {
            root = FindWeSingWindow();
            if (root != null)
            {
                ParseProgress(root, out currentSec, out totalSec);
            }
        }
        catch (Exception ex)
        {
            if (PRINT_EXCEPTION_LOG)
            {
                Console.WriteLine($"【全民K歌】读取进度时发生UI Automation异常：");
                Console.WriteLine($"异常消息：{ex.Message}");
                Console.WriteLine($"堆栈跟踪：{ex.StackTrace}");
                Console.WriteLine("----------------------------------------");
            }
        }
        finally
        {
            ReleaseComObject(root);
        }

        // 3. 通过进度是否变化来判断播放/暂停
        string status = DetermineStatus(currentSec);

        // 4. 构建输出
        string fixedTitle = FixTitleWeSing(windowTitle);
        if (currentSec >= 0 && totalSec > 0)
        {
            return $"{status}\r\n{fixedTitle}\r\nProgress:{currentSec}|{totalSec}";
        }
        else
        {
            return $"{status}\r\n{fixedTitle}";
        }
    }

    /// <summary>
    /// 查找全民K歌播放窗口的 IUIAutomationElement（标题为 "全民K歌 - {歌曲名}" 的窗口）
    /// </summary>
    private IUIAutomationElement FindWeSingWindow()
    {
        IUIAutomationElement result = null;
        IUIAutomationElement desktop = null;
        IUIAutomationCondition pidCondition = null;
        IUIAutomationElementArray windows = null;

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

            IUIAutomation automation = GetAutomation();
            desktop = automation.GetRootElement();
            pidCondition = automation.CreatePropertyCondition(UIA_ProcessIdPropertyId, pid);
            windows = desktop.FindAll(TreeScope.TreeScope_Children, pidCondition);

            // 找到标题以 "全民K歌 - " 开头的播放窗口（包含进度控件）
            int count = windows.Length;
            for (int i = 0; i < count; i++)
            {
                IUIAutomationElement win = null;

                try
                {
                    win = windows.GetElement(i);
                    string name = win.CurrentName;
                    if (name != null && name.StartsWith("全民K歌 - ") && name.Length > "全民K歌 - ".Length)
                    {
                        result = win;
                        win = null;
                        break;
                    }
                }
                catch (Exception ex)
                {
                    if (PRINT_EXCEPTION_LOG)
                    {
                        Console.WriteLine($"【全民K歌】获取窗口名称时发生异常：");
                        Console.WriteLine($"异常消息：{ex.Message}");
                        Console.WriteLine($"堆栈跟踪：{ex.StackTrace}");
                        Console.WriteLine("----------------------------------------");
                    }
                }
                finally
                {
                    ReleaseComObject(win);
                }
            }
        }
        catch (Exception ex)
        {
            if (PRINT_EXCEPTION_LOG)
            {
                Console.WriteLine($"【全民K歌】查找播放窗口时发生异常：");
                Console.WriteLine($"异常消息：{ex.Message}");
                Console.WriteLine($"堆栈跟踪：{ex.StackTrace}");
                Console.WriteLine("----------------------------------------");
            }
        }
        finally
        {
            ReleaseComObject(windows);
            ReleaseComObject(pidCondition);
            ReleaseComObject(desktop);
        }

        return result;
    }

    /// <summary>
    /// 从 UI Automation 树中解析进度文本 (格式: "00:08 | 04:16")
    /// </summary>
    private void ParseProgress(IUIAutomationElement root, out int currentSec, out int totalSec)
    {
        currentSec = -1;
        totalSec = -1;

        IUIAutomationCondition textCondition = null;
        IUIAutomationElementArray textElements = null;

        try
        {
            IUIAutomation automation = GetAutomation();

            // 搜索所有文本元素，查找匹配进度格式的
            textCondition = automation.CreatePropertyCondition(
                UIA_ControlTypePropertyId, UIA_TextControlTypeId);

            textElements = root.FindAll(TreeScope.TreeScope_Descendants, textCondition);

            int count = textElements.Length;
            for (int i = 0; i < count; i++)
            {
                IUIAutomationElement elem = null;

                try
                {
                    elem = textElements.GetElement(i);
                    string name = elem.CurrentName;
                    if (string.IsNullOrEmpty(name)) continue;

                    // 尝试匹配 "00:08 | 04:16" 或 "00:08|04:16" 格式
                    if (TryParseProgressText(name, out currentSec, out totalSec))
                    {
                        return;
                    }
                }
                finally
                {
                    ReleaseComObject(elem);
                }
            }
        }
        catch (Exception ex)
        {
            if (PRINT_EXCEPTION_LOG)
            {
                Console.WriteLine($"【全民K歌】解析进度文本时发生UI Automation异常：");
                Console.WriteLine($"异常消息：{ex.Message}");
                Console.WriteLine($"堆栈跟踪：{ex.StackTrace}");
                Console.WriteLine("----------------------------------------");
            }
        }
        finally
        {
            ReleaseComObject(textElements);
            ReleaseComObject(textCondition);
        }
    }

    private IUIAutomation GetAutomation()
    {
        if (_automation != null)
        {
            return _automation;
        }

        try
        {
            _automation = (IUIAutomation)new CUIAutomation8();
            return _automation;
        }
        catch
        {
        }

        try
        {
            _automation = (IUIAutomation)new CUIAutomation();
            return _automation;
        }
        catch
        {
        }

        try
        {
            Type automationType = Type.GetTypeFromCLSID(new Guid("E22AD333-B25F-460C-83D0-0581107395C9"));
            if (automationType != null)
            {
                _automation = Activator.CreateInstance(automationType) as IUIAutomation;
                if (_automation != null)
                {
                    return _automation;
                }
            }
        }
        catch
        {
        }

        try
        {
            Type automationType = Type.GetTypeFromCLSID(new Guid("FF48DBA4-60EF-4201-AA87-54103EEF594E"));
            if (automationType != null)
            {
                _automation = Activator.CreateInstance(automationType) as IUIAutomation;
                if (_automation != null)
                {
                    return _automation;
                }
            }
        }
        catch
        {
        }

        throw new InvalidOperationException("无法创建 UI Automation COM 对象（CUIAutomation8/CUIAutomation）。");
    }

    private void ReleaseComObject(object comObject)
    {
        if (comObject != null && Marshal.IsComObject(comObject))
        {
            try
            {
                Marshal.FinalReleaseComObject(comObject);
            }
            catch
            {
            }
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

    /*
        修正全民 K 歌标题
        "全民K歌 - 爱在西元前" → "爱在西元前"
    */
    private string FixTitleWeSing(string windowTitle)
    {
        const string prefix = "全民K歌 - ";

        if (!string.IsNullOrEmpty(windowTitle) && windowTitle.StartsWith(prefix))
        {
            return windowTitle.Substring(prefix.Length);
        }

        return windowTitle;
    }
}