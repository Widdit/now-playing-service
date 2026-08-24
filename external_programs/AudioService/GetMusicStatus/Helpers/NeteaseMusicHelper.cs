using System;
using System.Diagnostics;
using System.Runtime.InteropServices;
using System.Threading;
using Interop.UIAutomationClient;

public static class NeteaseMusicHelper
{
    private const bool PRINT_EXCEPTION_LOG = false;
    private const int UIA_ProcessIdPropertyId = 30002;
    private const int UIA_ControlTypePropertyId = 30003;
    private const int UIA_TextControlTypeId = 50020;
    private const int PROGRESS_POLL_INTERVAL_MS = 300;
    private const int FULL_SCAN_RETRY_MS = 500;
    private const int TRACK_CHANGE_GRACE_MS = 2000;

    private static readonly object StateLock = new object();
    private static readonly AutoResetEvent WorkSignal = new AutoResetEvent(false);
    private static Thread _workerThread;
    private static string _requestedTrack;
    private static long _requestedGeneration;
    private static long _resultGeneration = -1;
    private static int _currentSec = -1;
    private static int _totalSec = -1;

    /// <summary>
    /// 仅更新内存状态，不执行 UIA。worker 会等待 2 秒，确保切歌时先完成标题和初始进度跳转。
    /// </summary>
    public static void SetTrack(string track)
    {
        lock (StateLock)
        {
            if (string.Equals(_requestedTrack, track, StringComparison.Ordinal)) return;

            _requestedTrack = track;
            _requestedGeneration++;
            _resultGeneration = -1;
            _currentSec = -1;
            _totalSec = -1;

            if (!string.IsNullOrEmpty(track)) EnsureWorkerStarted();
        }
        WorkSignal.Set();
    }

    /// <summary>非阻塞读取 worker 最近一次成功抓到的当前歌曲进度。</summary>
    public static bool TryGetCachedProgress(out int currentSec, out int totalSec)
    {
        lock (StateLock)
        {
            currentSec = _currentSec;
            totalSec = _totalSec;
            return _resultGeneration == _requestedGeneration && currentSec >= 0 && totalSec > 0;
        }
    }

    private static void EnsureWorkerStarted()
    {
        if (_workerThread != null) return;

        _workerThread = new Thread(ProgressWorker)
        {
            IsBackground = true,
            Name = "NeteaseProgressUIA"
        };
        _workerThread.SetApartmentState(ApartmentState.STA);
        _workerThread.Start();
    }

    private static void ProgressWorker()
    {
        IUIAutomation automation = null;
        IUIAutomationElement cachedWindow = null;
        IUIAutomationElement cachedProgressElement = null;
        long observedGeneration = -1;
        DateTime scanAllowedAt = DateTime.MinValue;
        DateTime lastFullScanAt = DateTime.MinValue;

        try
        {
            automation = CreateAutomation();
            while (true)
            {
                string track;
                long generation;
                lock (StateLock)
                {
                    track = _requestedTrack;
                    generation = _requestedGeneration;
                }

                if (generation != observedGeneration)
                {
                    // Chromium 切歌会重建内部元素；顶层窗口通常稳定，继续缓存它。
                    ReleaseComObject(cachedProgressElement);
                    cachedProgressElement = null;
                    observedGeneration = generation;
                    scanAllowedAt = DateTime.UtcNow.AddMilliseconds(TRACK_CHANGE_GRACE_MS);
                    lastFullScanAt = DateTime.MinValue;
                }

                if (string.IsNullOrEmpty(track))
                {
                    ReleaseComObject(cachedProgressElement);
                    cachedProgressElement = null;
                    ReleaseComObject(cachedWindow);
                    cachedWindow = null;
                    WorkSignal.WaitOne();
                    continue;
                }

                int graceRemaining = (int)Math.Ceiling((scanAllowedAt - DateTime.UtcNow).TotalMilliseconds);
                if (graceRemaining > 0)
                {
                    WorkSignal.WaitOne(Math.Min(graceRemaining, PROGRESS_POLL_INTERVAL_MS));
                    continue;
                }

                int currentSec = -1;
                int totalSec = -1;

                // 热路径：直接读已命中的 Text 元素，避免重复遍历 UIA 树。
                if (cachedProgressElement != null)
                {
                    try
                    {
                        if (!TryParseProgressText(cachedProgressElement.CurrentName, out currentSec, out totalSec))
                        {
                            ReleaseComObject(cachedProgressElement);
                            cachedProgressElement = null;
                        }
                    }
                    catch
                    {
                        ReleaseComObject(cachedProgressElement);
                        cachedProgressElement = null;
                    }
                }

                // 缓存失效才慢扫描，并限频；长尾只会阻塞本 worker，不会阻塞歌曲检测。
                if (cachedProgressElement == null &&
                    (DateTime.UtcNow - lastFullScanAt).TotalMilliseconds >= FULL_SCAN_RETRY_MS)
                {
                    lastFullScanAt = DateTime.UtcNow;
                    if (!IsUsablePlayerWindow(cachedWindow))
                    {
                        ReleaseComObject(cachedWindow);
                        cachedWindow = FindNeteasePlayerWindow(automation);
                    }
                    if (cachedWindow != null)
                    {
                        cachedProgressElement = FindProgressElement(
                            automation, cachedWindow, out currentSec, out totalSec);
                    }
                }

                if (currentSec >= 0 && totalSec > 0)
                {
                    PublishProgress(generation, currentSec, totalSec);
                }
                WorkSignal.WaitOne(PROGRESS_POLL_INTERVAL_MS);
            }
        }
        catch (Exception ex)
        {
            if (PRINT_EXCEPTION_LOG) Console.Error.WriteLine($"【网易云】进度 UIA worker 异常：{ex}");
        }
        finally
        {
            ReleaseComObject(cachedProgressElement);
            ReleaseComObject(cachedWindow);
            ReleaseComObject(automation);
        }
    }

    private static void PublishProgress(long generation, int currentSec, int totalSec)
    {
        lock (StateLock)
        {
            if (generation != _requestedGeneration || string.IsNullOrEmpty(_requestedTrack)) return;
            _currentSec = currentSec;
            _totalSec = totalSec;
            _resultGeneration = generation;
        }
    }

    private static bool IsUsablePlayerWindow(IUIAutomationElement window)
    {
        if (window == null) return false;
        try
        {
            string name = window.CurrentName;
            return !string.IsNullOrEmpty(name) && name.Contains(" - ") && !name.Contains("MediaPlayer");
        }
        catch { return false; }
    }

    /// <summary>只在窗口缓存失效时从桌面 UIA 根节点查找网易云主窗口。</summary>
    private static IUIAutomationElement FindNeteasePlayerWindow(IUIAutomation automation)
    {
        IUIAutomationElement result = null;
        IUIAutomationElement desktop = null;
        IUIAutomationCondition pidCondition = null;
        IUIAutomationElementArray windows = null;
        try
        {
            Process[] processes = Process.GetProcessesByName("cloudmusic");
            if (processes.Length == 0) return null;
            desktop = automation.GetRootElement();

            foreach (Process proc in processes)
            {
                int pid = proc.Id;
                proc.Dispose();
                if (result != null) continue;
                try
                {
                    ReleaseComObject(pidCondition);
                    pidCondition = null;
                    ReleaseComObject(windows);
                    windows = null;
                    pidCondition = automation.CreatePropertyCondition(UIA_ProcessIdPropertyId, pid);
                    windows = desktop.FindAll(TreeScope.TreeScope_Children, pidCondition);
                    for (int i = 0; i < windows.Length; i++)
                    {
                        IUIAutomationElement win = null;
                        try
                        {
                            win = windows.GetElement(i);
                            if (IsUsablePlayerWindow(win))
                            {
                                result = win;
                                win = null;
                                break;
                            }
                        }
                        catch { }
                        finally { ReleaseComObject(win); }
                    }
                }
                catch { }
            }
        }
        catch (Exception ex)
        {
            if (PRINT_EXCEPTION_LOG) Console.Error.WriteLine($"【网易云】查找播放窗口异常：{ex}");
        }
        finally
        {
            ReleaseComObject(windows);
            ReleaseComObject(pidCondition);
            ReleaseComObject(desktop);
        }
        return result;
    }

    /// <summary>扫描一次 Text 后代；命中后返回元素供 worker 跨 tick 缓存。</summary>
    private static IUIAutomationElement FindProgressElement(
        IUIAutomation automation, IUIAutomationElement root, out int currentSec, out int totalSec)
    {
        currentSec = -1;
        totalSec = -1;
        IUIAutomationElement result = null;
        IUIAutomationCondition textCondition = null;
        IUIAutomationElementArray textElements = null;
        try
        {
            textCondition = automation.CreatePropertyCondition(UIA_ControlTypePropertyId, UIA_TextControlTypeId);
            textElements = root.FindAll(TreeScope.TreeScope_Descendants, textCondition);
            for (int i = 0; i < textElements.Length; i++)
            {
                IUIAutomationElement elem = null;
                try
                {
                    elem = textElements.GetElement(i);
                    if (TryParseProgressText(elem.CurrentName, out currentSec, out totalSec))
                    {
                        result = elem;
                        elem = null;
                        break;
                    }
                }
                catch { }
                finally { ReleaseComObject(elem); }
            }
        }
        catch (Exception ex)
        {
            if (PRINT_EXCEPTION_LOG) Console.Error.WriteLine($"【网易云】解析进度 UIA 异常：{ex}");
        }
        finally
        {
            ReleaseComObject(textElements);
            ReleaseComObject(textCondition);
        }
        return result;
    }

    private static bool TryParseProgressText(string text, out int currentSec, out int totalSec)
    {
        currentSec = -1;
        totalSec = -1;
        if (string.IsNullOrEmpty(text)) return false;
        string cleaned = text.Replace(" ", "");
        int sepIndex = cleaned.IndexOf('/');
        if (sepIndex < 0) sepIndex = cleaned.IndexOf('|');
        if (sepIndex <= 0 || sepIndex >= cleaned.Length - 1) return false;
        if (!TryParseTimeString(cleaned.Substring(0, sepIndex), out int c) ||
            !TryParseTimeString(cleaned.Substring(sepIndex + 1), out int t)) return false;
        if (t <= 0 || c < 0 || c > t + 2) return false;
        currentSec = c;
        totalSec = t;
        return true;
    }

    private static bool TryParseTimeString(string timeStr, out int seconds)
    {
        seconds = 0;
        string[] parts = timeStr.Split(':');
        if (parts.Length != 2) return false;
        if (!int.TryParse(parts[0], out int minutes) || !int.TryParse(parts[1], out int secs)) return false;
        if (minutes < 0 || secs < 0 || secs >= 60) return false;
        seconds = minutes * 60 + secs;
        return true;
    }

    private static IUIAutomation CreateAutomation()
    {
        try { return (IUIAutomation)new CUIAutomation8(); } catch { }
        try { return (IUIAutomation)new CUIAutomation(); } catch { }
        foreach (Guid clsid in new[]
        {
            new Guid("E22AD333-B25F-460C-83D0-0581107395C9"),
            new Guid("FF48DBA4-60EF-4201-AA87-54103EEF594E")
        })
        {
            try
            {
                Type type = Type.GetTypeFromCLSID(clsid);
                if (type != null && Activator.CreateInstance(type) is IUIAutomation automation) return automation;
            }
            catch { }
        }
        throw new InvalidOperationException("无法创建 UI Automation COM 对象（CUIAutomation8/CUIAutomation）。");
    }

    private static void ReleaseComObject(object comObject)
    {
        if (comObject == null || !Marshal.IsComObject(comObject)) return;
        try { Marshal.FinalReleaseComObject(comObject); } catch { }
    }
}
