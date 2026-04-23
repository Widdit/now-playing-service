using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Runtime.InteropServices;
using Interop.UIAutomationClient;
using CSCore.CoreAudioAPI;

public class NeteaseMusicService : MusicService
{
    private const bool PRINT_EXCEPTION_LOG = false;

    // 拖动进度条时，网易云可能短暂断开音频会话 / UIA 读取瞬时失败，
    // 这会让本服务返回 "None" —— 进而触发 Java 端 NowPlayingService.java:338
    // 的"同名歌曲恢复"分支（cover 清空 + TrackChanged + PlayerProgressReplay），
    // 在前端表现为进度条归零 + 歌曲信息重刷。
    // 解决思路：若某次轮询的结果为 "None"，但最近 1.5s 内曾经输出过有效结果，
    // 就继续沿用那份旧输出，等真正闭屏再返回 None，避免把瞬时失联放大成可见事件。
    private const int NONE_HOLDOVER_MS = 1500;
    private string _lastGoodOutput = null;
    private DateTime _lastGoodOutputAt = DateTime.MinValue;

    private const int UIA_ProcessIdPropertyId = 30002;
    private const int UIA_ControlTypePropertyId = 30003;
    private const int UIA_TextControlTypeId = 50020;

    private IUIAutomation _automation;

    // 用于"音量为 0 时通过进度变化判断播放/暂停"的兜底
    private int _lastProgressSeconds = -1;
    private DateTime _lastProgressChangeTime = DateTime.MinValue;

    public override string GetMusicStatus(AudioSessionManager2 sessionManager)
    {
        double volume = 0;
        bool musicAppRunning = false;
        string windowTitle = "";

        AudioSessionEnumerator sessionEnumerator = null;

        try
        {
            sessionEnumerator = sessionManager.GetSessionEnumerator();

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

                if (processName.Contains("cloudmusic") || processName.Contains("CloudMusic"))
                {
					// 网易云音乐会存在多个进程，因此不能 break，并且需要累加音量
                    musicAppRunning = true;
                    meter = session.QueryInterface<AudioMeterInformation>();
                    volume += meter.PeakValue;
                    windowTitle = WindowDetector.GetWindowTitleByHandle(sessionControl.Process.MainWindowHandle);
                }

                // 释放对象
                meter?.Dispose();
                sessionControl?.Dispose();
                session.Dispose();
            }
        }
        catch (Exception)
        {
            return ApplyHoldover("None");
        }
        finally
        {
            // 释放对象
            sessionEnumerator?.Dispose();
        }

        // 未检测到音乐软件进程
        if (!musicAppRunning)
        {
            return ApplyHoldover("None");
        }

        // 这段代码处理三种特殊情况：
        // 1. 如果音乐软件最小化到托盘，那么主窗口标题会变为空
        // 2. 如果开启了桌面歌词，那么主窗口标题就不是歌曲信息了
        // 3. 如果开启了迷你模式，那么主窗口标题就不是歌曲信息了
        // 此时，需要遍历该进程的所有窗口来获取有效窗口标题
        try
        {
            if (string.IsNullOrEmpty(windowTitle) || !windowTitle.Contains(" - "))
            {
                windowTitle = "";

                List<string> allTitles = WindowDetector.GetWindowTitles("cloudmusic");
                foreach (string title in allTitles)
                {
                    if (title.Contains(" - ") && !title.Contains("MediaPlayer"))
                    {
                        windowTitle = title;
                        break;
                    }
                }
            }
        }
        catch (Exception)
        {
            return ApplyHoldover("None");
        }

        // 如果窗口标题为空（说明没成功获取到），则返回 None
        if (string.IsNullOrEmpty(windowTitle))
        {
            return ApplyHoldover("None");
        }

        windowTitle = FixTitleNetease(windowTitle);

        // 通过 UI Automation 读取进度（失败时保持 -1，不输出 Progress 行）
        int currentSec = -1;
        int totalSec = -1;
        try
        {
            ReadProgressViaUIA(out currentSec, out totalSec);
        }
        catch (Exception ex)
        {
            if (PRINT_EXCEPTION_LOG)
            {
                Console.WriteLine($"【网易云】读取进度时发生UI Automation异常：");
                Console.WriteLine($"异常消息：{ex.Message}");
                Console.WriteLine($"堆栈跟踪：{ex.StackTrace}");
                Console.WriteLine("----------------------------------------");
            }
        }

        // 判断播放状态：
        // 优先用音量（反映是否真的在出声）；若音量为 0 但进度最近有变化，则仍视为 Playing
        // （用户拖动进度条、或系统静音等场景下，volume=0 但进度在跳动）
        string status;
        if (volume > 0.00001)
        {
            status = "Playing";
            UpdateProgressChangeTracker(currentSec);
        }
        else
        {
            status = IsProgressChangingRecently(currentSec) ? "Playing" : "Paused";
        }

        // 输出结果
        string result = (currentSec >= 0 && totalSec > 0)
            ? $"{status}\r\n{windowTitle}\r\nProgress:{currentSec}|{totalSec}"
            : $"{status}\r\n{windowTitle}";
        return ApplyHoldover(result);
    }

    /// <summary>
    /// 对候选输出做"短暂失联抑制"：
    /// - 有效输出（非 None）→ 直接返回，同时刷新缓存
    /// - 无效输出（None）→ 若缓存未过期则返回缓存，否则清空缓存并返回 None
    /// </summary>
    private string ApplyHoldover(string candidate)
    {
        if (candidate != "None")
        {
            _lastGoodOutput = candidate;
            _lastGoodOutputAt = DateTime.Now;
            return candidate;
        }

        if (_lastGoodOutput != null &&
            (DateTime.Now - _lastGoodOutputAt).TotalMilliseconds < NONE_HOLDOVER_MS)
        {
            return _lastGoodOutput;
        }

        _lastGoodOutput = null;
        return "None";
    }

    /*
        修正网易云音乐标题
    */
    private string FixTitleNetease(string windowTitle)
    {
        return windowTitle.Replace("/", " / ");
    }

    /// <summary>
    /// 通过 UI Automation 从网易云主播放窗口读取进度文本
    /// 优先匹配形如 "MM:SS / MM:SS" 或 "MM:SS | MM:SS" 的单个文本元素
    /// </summary>
    private void ReadProgressViaUIA(out int currentSec, out int totalSec)
    {
        currentSec = -1;
        totalSec = -1;

        IUIAutomationElement window = null;
        try
        {
            window = FindNeteasePlayerWindow();
            if (window == null)
            {
                return;
            }

            ParseProgressFromWindow(window, out currentSec, out totalSec);
        }
        finally
        {
            ReleaseComObject(window);
        }
    }

    /// <summary>
    /// 在所有 cloudmusic 进程中查找标题包含 " - " 的主播放窗口，返回其 UIA 根节点
    /// </summary>
    private IUIAutomationElement FindNeteasePlayerWindow()
    {
        IUIAutomationElement result = null;
        IUIAutomationElement desktop = null;
        IUIAutomationCondition pidCondition = null;
        IUIAutomationElementArray windows = null;

        try
        {
            Process[] processes = Process.GetProcessesByName("cloudmusic");
            if (processes.Length == 0)
            {
                return null;
            }

            IUIAutomation automation = GetAutomation();
            desktop = automation.GetRootElement();

            foreach (Process proc in processes)
            {
                int pid = proc.Id;
                proc.Dispose();

                if (result != null)
                {
                    continue;
                }

                try
                {
                    ReleaseComObject(pidCondition);
                    ReleaseComObject(windows);
                    pidCondition = automation.CreatePropertyCondition(UIA_ProcessIdPropertyId, pid);
                    windows = desktop.FindAll(TreeScope.TreeScope_Children, pidCondition);

                    int count = windows.Length;
                    for (int i = 0; i < count; i++)
                    {
                        IUIAutomationElement win = null;
                        try
                        {
                            win = windows.GetElement(i);
                            string name = win.CurrentName;
                            if (!string.IsNullOrEmpty(name) && name.Contains(" - ") && !name.Contains("MediaPlayer"))
                            {
                                result = win;
                                win = null;
                                break;
                            }
                        }
                        catch (Exception)
                        {
                        }
                        finally
                        {
                            ReleaseComObject(win);
                        }
                    }
                }
                catch (Exception)
                {
                }
            }
        }
        catch (Exception ex)
        {
            if (PRINT_EXCEPTION_LOG)
            {
                Console.WriteLine($"【网易云】查找播放窗口时发生异常：");
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
    /// 在窗口 UIA 子树内搜索 Text 控件，解析进度文本
    /// </summary>
    private void ParseProgressFromWindow(IUIAutomationElement root, out int currentSec, out int totalSec)
    {
        currentSec = -1;
        totalSec = -1;

        IUIAutomationCondition textCondition = null;
        IUIAutomationElementArray textElements = null;

        try
        {
            IUIAutomation automation = GetAutomation();
            textCondition = automation.CreatePropertyCondition(UIA_ControlTypePropertyId, UIA_TextControlTypeId);
            textElements = root.FindAll(TreeScope.TreeScope_Descendants, textCondition);

            int count = textElements.Length;
            for (int i = 0; i < count; i++)
            {
                IUIAutomationElement elem = null;
                try
                {
                    elem = textElements.GetElement(i);
                    string name = elem.CurrentName;
                    if (string.IsNullOrEmpty(name))
                    {
                        continue;
                    }

                    if (TryParseProgressText(name, out currentSec, out totalSec))
                    {
                        return;
                    }
                }
                catch (Exception)
                {
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
                Console.WriteLine($"【网易云】解析进度文本时发生UI Automation异常：");
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

    /// <summary>
    /// 解析形如 "MM:SS / MM:SS"、"MM:SS|MM:SS" 的进度字符串
    /// 要求：分隔符两侧都能解析为 MM:SS，且 current &lt;= total
    /// </summary>
    private bool TryParseProgressText(string text, out int currentSec, out int totalSec)
    {
        currentSec = -1;
        totalSec = -1;

        string cleaned = text.Replace(" ", "");

        int sepIndex = cleaned.IndexOf('/');
        if (sepIndex < 0)
        {
            sepIndex = cleaned.IndexOf('|');
        }
        if (sepIndex <= 0 || sepIndex >= cleaned.Length - 1)
        {
            return false;
        }

        string currentPart = cleaned.Substring(0, sepIndex);
        string totalPart = cleaned.Substring(sepIndex + 1);

        if (!TryParseTimeString(currentPart, out int c) || !TryParseTimeString(totalPart, out int t))
        {
            return false;
        }

        if (t <= 0 || c < 0 || c > t + 2)
        {
            // total 必须为正，current 不应显著大于 total（留 2 秒容差应对边界取整）
            return false;
        }

        currentSec = c;
        totalSec = t;
        return true;
    }

    private bool TryParseTimeString(string timeStr, out int seconds)
    {
        seconds = 0;
        string[] parts = timeStr.Split(':');
        if (parts.Length != 2) return false;

        if (int.TryParse(parts[0], out int minutes) && int.TryParse(parts[1], out int secs))
        {
            if (minutes < 0 || secs < 0 || secs >= 60) return false;
            seconds = minutes * 60 + secs;
            return true;
        }

        return false;
    }

    /// <summary>
    /// 记录进度变化时间戳（在音量 > 0 的情况下也要更新，保证从"有声播放"切到"静音播放"时不误判为暂停）
    /// </summary>
    private void UpdateProgressChangeTracker(int currentSec)
    {
        if (currentSec < 0) return;
        if (currentSec != _lastProgressSeconds)
        {
            _lastProgressSeconds = currentSec;
            _lastProgressChangeTime = DateTime.Now;
        }
    }

    /// <summary>
    /// 在音量为 0 的情况下用于兜底：若进度刚刚发生变化（1.5 秒内），仍认为在播放
    /// </summary>
    private bool IsProgressChangingRecently(int currentSec)
    {
        if (currentSec < 0) return false;

        if (currentSec != _lastProgressSeconds)
        {
            _lastProgressSeconds = currentSec;
            _lastProgressChangeTime = DateTime.Now;
            return true;
        }

        return (DateTime.Now - _lastProgressChangeTime).TotalMilliseconds <= 1500;
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
}
