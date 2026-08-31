using System;
using System.Diagnostics;
using System.Runtime.InteropServices;
using System.Threading;
using Interop.UIAutomationClient;

/// <summary>
/// 网易云音乐进度条的 UI Automation 抓取模块。
///
/// 设计概览：
/// 主线程（NeteaseMusicService）只做两件事——通知曲目变化（SetTrack）、
/// 读取最近一次抓取到的进度快照（TryGetCachedProgress），本身不直接触碰 UIA，
/// 避免 UIA 调用的不确定耗时拖慢主轮询节奏（尤其是切歌瞬间）。
/// 真正的 UIA 抓取工作放在独立的后台线程（ProgressWorker）中按固定间隔轮询，
/// 两者之间通过一份带 generation 版本号的共享状态解耦，防止读到"上一首歌"的进度。
/// </summary>
public static class NeteaseMusicHelper
{
    private const bool PRINT_EXCEPTION_LOG = false;

    private const int UIA_ProcessIdPropertyId = 30002;
    private const int UIA_ControlTypePropertyId = 30003;
    private const int UIA_TextControlTypeId = 50020;

    // worker 轮询间隔（毫秒）
    private const int PROGRESS_POLL_INTERVAL_MS = 300;
    // 切歌后的宽限期（毫秒）：网易云基于 Chromium/Electron，切歌时内部会重建一部分 UIA 元素，
    // 若在重建完成前就去读取进度文本，容易读到空值或上一首歌的残留内容，因此切歌后先等待片刻再开始抓取
    private const int TRACK_CHANGE_GRACE_MS = 2000;

    private static readonly object StateLock = new object();
    private static readonly AutoResetEvent WorkSignal = new AutoResetEvent(false);
    private static Thread _workerThread;
    private static string _requestedTrack;
    private static long _requestedGeneration;
    private static long _resultGeneration = -1;
    private static int _currentSec = -1;
    private static int _totalSec = -1;

    // 播放器主窗口句柄（主线程每轮轮询更新）。用于 ElementFromHandle 精确定位主窗口，
    // 进度条一定在主窗口里，从而避免桌面歌词等其它 cloudmusic 窗口被误选、导致读不到进度。
    private static IntPtr _playerWindowHandle = IntPtr.Zero;

    /// <summary>
    /// 主线程调用：通知 worker 当前曲目已切换（或播放已结束，track 传 null）。
    /// 这里只更新共享状态并唤醒 worker，不做任何 UIA 操作，因此对主线程而言是零成本的。
    /// generation 自增用于标记"这是一次新的曲目请求"：worker 据此判断需要等待宽限期、
    /// 丢弃与旧曲目相关的缓存元素与结果。
    /// </summary>
    public static void SetTrack(string track)
    {
        lock (StateLock)
        {
            if (string.Equals(_requestedTrack, track, StringComparison.Ordinal))
            {
                return;
            }

            _requestedTrack = track;
            _requestedGeneration++;
            _resultGeneration = -1;
            _currentSec = -1;
            _totalSec = -1;

            if (!string.IsNullOrEmpty(track))
            {
                EnsureWorkerStarted();
            }
        }

        WorkSignal.Set();
    }

    /// <summary>
    /// 主线程调用：更新播放器主窗口句柄（由音频会话的 MainWindowHandle 得到）。
    /// 句柄变化时唤醒 worker，让其在下一轮通过 ElementFromHandle 重新定位主窗口。
    /// </summary>
    public static void SetWindowHandle(IntPtr hwnd)
    {
        lock (StateLock)
        {
            if (_playerWindowHandle == hwnd)
            {
                return;
            }
            _playerWindowHandle = hwnd;
        }

        WorkSignal.Set();
    }

    /// <summary>
    /// 主线程调用：非阻塞地读取 worker 最近一次抓取到的进度快照。
    /// 只有当结果的 generation 与当前请求的 generation 一致时才视为有效——
    /// 这避免了"切歌瞬间，worker 还没来得及更新，主线程读到上一首歌进度"的情况。
    /// </summary>
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
        if (_workerThread != null)
        {
            return;
        }

        _workerThread = new Thread(ProgressWorker)
        {
            IsBackground = true,
            Name = "NeteaseProgressUIA"
        };
        _workerThread.SetApartmentState(ApartmentState.STA);
        _workerThread.Start();
    }

    /// <summary>
    /// 后台 worker 主循环。
    ///
    /// 缓存策略：
    /// 播放器主窗口的查找需要从桌面根节点按进程 PID 枚举子窗口，开销相对较大，
    /// 但窗口标题由操作系统维护、更新及时，因此一旦找到就跨轮询周期缓存（cachedWindow），
    /// 每轮只需用 IsUsablePlayerWindow 做一次轻量校验，失效了再重新查找。
    ///
    /// 而窗口内部的进度文本元素则不做跨轮询缓存：其可见性会随用户是否 hover 在进度条上
    /// 而变化（不 hover 时网易云不渲染该文本），查找它的开销仅限于在单个窗口的子树内查找
    /// Text 控件，相对于整棵桌面树的查找很轻量；同时进度是否"当前可见"这件事本身就需要
    /// 每一轮都重新判断，因此这里选择用轻微的重复查找换取结果的实时准确性。
    ///
    /// 曲目切换处理：
    /// generation 变化时说明切歌了，此时重置宽限期计时，等待 UIA 元素重建完成后再恢复抓取；
    /// 期间窗口缓存仍然保留（顶层窗口一般不会因为切歌而销毁重建）。
    /// </summary>
    private static void ProgressWorker()
    {
        IUIAutomation automation = null;
        IUIAutomationElement cachedWindow = null;
        long observedGeneration = -1;
        DateTime scanAllowedAt = DateTime.MinValue;

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
                    observedGeneration = generation;
                    scanAllowedAt = DateTime.UtcNow.AddMilliseconds(TRACK_CHANGE_GRACE_MS);
                }

                if (string.IsNullOrEmpty(track))
                {
                    // 没有正在播放的曲目，释放窗口缓存并挂起，等待下一次 SetTrack 唤醒
                    ReleaseComObject(cachedWindow);
                    cachedWindow = null;
                    WorkSignal.WaitOne();
                    continue;
                }

                // 宽限期内暂不抓取，避免读到切歌过程中尚未稳定的 UIA 元素
                int graceRemaining = (int)Math.Ceiling((scanAllowedAt - DateTime.UtcNow).TotalMilliseconds);
                if (graceRemaining > 0)
                {
                    WorkSignal.WaitOne(Math.Min(graceRemaining, PROGRESS_POLL_INTERVAL_MS));
                    continue;
                }

                // 本轮要扫描的窗口：优先用主窗口句柄精确定位（ElementFromHandle）。
                // 进度条一定在主窗口里，用它可避免桌面歌词等其它 cloudmusic 窗口被误选。
                IUIAutomationElement scanWindow = null;
                bool scanWindowIsTemp = false;

                IntPtr hwnd;
                lock (StateLock)
                {
                    hwnd = _playerWindowHandle;
                }

                if (hwnd != IntPtr.Zero)
                {
                    try
                    {
                        scanWindow = automation.ElementFromHandle(hwnd);
                    }
                    catch
                    {
                    }
                }

                if (scanWindow == null)
                {
                    // 回退：无可用句柄时，沿用缓存窗口 + PID 枚举
                    if (!IsUsablePlayerWindow(cachedWindow))
                    {
                        ReleaseComObject(cachedWindow);
                        cachedWindow = FindNeteasePlayerWindow(automation);
                    }
                    scanWindow = cachedWindow;
                }
                else
                {
                    scanWindowIsTemp = true;
                }

                int currentSec = -1;
                int totalSec = -1;

                if (scanWindow != null)
                {
                    IUIAutomationElement progressElement =
                        FindProgressElement(automation, scanWindow, out currentSec, out totalSec);
                    ReleaseComObject(progressElement);
                }

                if (scanWindowIsTemp)
                {
                    ReleaseComObject(scanWindow);
                }

                // 无论本轮是否找到进度文本都发布结果：找不到（如用户未 hover 进度条）就发布 -1/-1，
                // 让主线程能感知"当前没有可显示的进度"，而不是一直沿用上一轮的旧快照
                PublishProgress(generation, currentSec, totalSec);

                WorkSignal.WaitOne(PROGRESS_POLL_INTERVAL_MS);
            }
        }
        catch (Exception ex)
        {
            if (PRINT_EXCEPTION_LOG)
            {
                Console.Error.WriteLine($"【网易云】进度 UIA worker 异常：{ex}");
            }
        }
        finally
        {
            ReleaseComObject(cachedWindow);
            ReleaseComObject(automation);
        }
    }

    /// <summary>
    /// 将本轮抓取结果写入共享状态，供主线程读取。
    /// 写入前再校验一次 generation：如果在本轮抓取耗时期间曲目已经又发生了切换，
    /// 这份结果就已经过期，直接丢弃，避免污染新曲目的进度缓存。
    /// </summary>
    private static void PublishProgress(long generation, int currentSec, int totalSec)
    {
        lock (StateLock)
        {
            if (generation != _requestedGeneration || string.IsNullOrEmpty(_requestedTrack))
            {
                return;
            }

            _currentSec = currentSec;
            _totalSec = totalSec;
            _resultGeneration = generation;
        }
    }

    /// <summary>
    /// 判断已缓存的窗口元素是否仍然有效可用（标题含 " - " 且不含 "MediaPlayer"）。
    /// 避免每次轮询都重新从桌面根节点查找窗口。
    /// </summary>
    private static bool IsUsablePlayerWindow(IUIAutomationElement window)
    {
        if (window == null)
        {
            return false;
        }

        try
        {
            string name = window.CurrentName;
            return !string.IsNullOrEmpty(name) && name.Contains(" - ") && !name.Contains("MediaPlayer");
        }
        catch
        {
            return false;
        }
    }

    /// <summary>
    /// 在所有 cloudmusic 进程中查找标题包含 " - " 的主播放窗口，返回其 UIA 根节点。
    /// 仅在窗口缓存失效时调用，从桌面 UIA 根节点枚举子窗口进行匹配。
    /// </summary>
    private static IUIAutomationElement FindNeteasePlayerWindow(IUIAutomation automation)
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
                        catch
                        {
                        }
                        finally
                        {
                            ReleaseComObject(win);
                        }
                    }
                }
                catch
                {
                }
            }
        }
        catch (Exception ex)
        {
            if (PRINT_EXCEPTION_LOG)
            {
                Console.Error.WriteLine($"【网易云】查找播放窗口异常：{ex}");
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
    /// 在指定窗口的 UIA 子树内查找所有 Text 控件，逐个尝试解析成
    /// "MM:SS / MM:SS" 或 "MM:SS | MM:SS" 形式的进度文本，命中即返回。
    /// 查找范围限定在单个窗口子树内，开销较小，因此每轮都重新执行，
    /// 不做跨轮询缓存，以保证能及时反映"进度文本当前是否可见"这一状态。
    /// </summary>
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
                catch
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
                Console.Error.WriteLine($"【网易云】解析进度 UIA 异常：{ex}");
            }
        }
        finally
        {
            ReleaseComObject(textElements);
            ReleaseComObject(textCondition);
        }

        return result;
    }

    /// <summary>
    /// 解析形如 "MM:SS / MM:SS"、"MM:SS | MM:SS" 的进度字符串。
    /// 要求：分隔符两侧都能解析为合法的 MM:SS，且 current &lt;= total。
    /// </summary>
    private static bool TryParseProgressText(string text, out int currentSec, out int totalSec)
    {
        currentSec = -1;
        totalSec = -1;

        if (string.IsNullOrEmpty(text))
        {
            return false;
        }

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
            // total 必须为正；current 不应显著大于 total（留 2 秒容差应对边界取整）
            return false;
        }

        currentSec = c;
        totalSec = t;
        return true;
    }

    private static bool TryParseTimeString(string timeStr, out int seconds)
    {
        seconds = 0;
        string[] parts = timeStr.Split(':');
        if (parts.Length != 2)
        {
            return false;
        }

        if (!int.TryParse(parts[0], out int minutes) || !int.TryParse(parts[1], out int secs))
        {
            return false;
        }

        if (minutes < 0 || secs < 0 || secs >= 60)
        {
            return false;
        }

        seconds = minutes * 60 + secs;
        return true;
    }

    /// <summary>
    /// 依次尝试多种方式创建 UI Automation COM 对象：
    /// CUIAutomation8（Win8+）→ CUIAutomation（Win7）→ 按 CLSID 激活（兼容性回退）。
    /// </summary>
    private static IUIAutomation CreateAutomation()
    {
        try
        {
            return (IUIAutomation)new CUIAutomation8();
        }
        catch
        {
        }

        try
        {
            return (IUIAutomation)new CUIAutomation();
        }
        catch
        {
        }

        foreach (Guid clsid in new[]
        {
            new Guid("E22AD333-B25F-460C-83D0-0581107395C9"),
            new Guid("FF48DBA4-60EF-4201-AA87-54103EEF594E")
        })
        {
            try
            {
                Type type = Type.GetTypeFromCLSID(clsid);
                if (type != null && Activator.CreateInstance(type) is IUIAutomation automation)
                {
                    return automation;
                }
            }
            catch
            {
            }
        }

        throw new InvalidOperationException("无法创建 UI Automation COM 对象（CUIAutomation8/CUIAutomation）。");
    }

    private static void ReleaseComObject(object comObject)
    {
        if (comObject == null || !Marshal.IsComObject(comObject))
        {
            return;
        }

        try
        {
            Marshal.FinalReleaseComObject(comObject);
        }
        catch
        {
        }
    }
}
