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

    // worker 轮询间隔（毫秒）：热路径下直接复用缓存元素，开销极低，可以轮询较快
    private const int PROGRESS_POLL_INTERVAL_MS = 300;
    // 缓存失效后重新全量扫描 UIA 树的最小间隔（毫秒），避免频繁遍历拖慢性能
    private const int FULL_SCAN_RETRY_MS = 500;
    // 切歌后等待 UIA 元素稳定的宽限期（毫秒）：Chromium 切歌会重建内部元素，需等待稳定
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
    /// 通知 worker 当前曲目已切换，仅更新内存状态，不执行 UIA 操作。
    /// worker 会等待 <see cref="TRACK_CHANGE_GRACE_MS"/> 毫秒宽限期，
    /// 确保切歌时 UIA 元素重建完成后再开始抓取进度。
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
    /// 非阻塞读取 worker 最近一次成功抓到的当前歌曲进度。
    /// 通过 generation 版本号判断缓存结果是否属于当前曲目请求。
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
    /// 后台 worker 主循环：维护 UIA 窗口缓存与进度元素缓存，定期轮询进度。
    /// 采用两级缓存策略：
    ///   热路径 —— 直接读已命中的 Text 元素的 CurrentName，避免重复遍历 UIA 树；
    ///   冷路径 —— 缓存失效时才执行全量扫描，并限制最小扫描间隔，避免阻塞歌曲检测。
    /// </summary>
    private static void ProgressWorker()
    {
        IUIAutomation automation = null;
        IUIAutomationElement cachedWindow = null;
        IUIAutomationElement cachedProgressElement = null;
        long observedGeneration = -1;
        DateTime scanAllowedAt = DateTime.MinValue;
        DateTime lastFullScanAt = DateTime.MinValue;
        int lastObservedCurrentSec = -1;
        bool hasObservedProgress = false;

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
                    // Chromium 切歌会重建内部 UIA 元素，进度元素缓存必须作废；
                    // 顶层窗口通常在切歌时保持稳定，继续沿用窗口缓存。
                    ReleaseComObject(cachedProgressElement);
                    cachedProgressElement = null;
                    observedGeneration = generation;
                    scanAllowedAt = DateTime.UtcNow.AddMilliseconds(TRACK_CHANGE_GRACE_MS);
                    lastFullScanAt = DateTime.MinValue;
                    lastObservedCurrentSec = -1;
                    hasObservedProgress = false;
                }

                if (string.IsNullOrEmpty(track))
                {
                    // 无曲目时释放所有缓存，进入休眠等待下一次 SetTrack 唤醒
                    ReleaseComObject(cachedProgressElement);
                    cachedProgressElement = null;
                    ReleaseComObject(cachedWindow);
                    cachedWindow = null;
                    lastObservedCurrentSec = -1;
                    hasObservedProgress = false;
                    WorkSignal.WaitOne();
                    continue;
                }

                // 切歌宽限期内不读取，等待 UIA 元素重建完成
                int graceRemaining = (int)Math.Ceiling((scanAllowedAt - DateTime.UtcNow).TotalMilliseconds);
                if (graceRemaining > 0)
                {
                    WorkSignal.WaitOne(Math.Min(graceRemaining, PROGRESS_POLL_INTERVAL_MS));
                    continue;
                }

                int currentSec = -1;
                int totalSec = -1;

                // 热路径：直接读已命中的 Text 元素的 CurrentName，避免重复遍历 UIA 树
                if (cachedProgressElement != null)
                {
                    try
                    {
                        if (!TryParseProgressText(cachedProgressElement.CurrentName, out currentSec, out totalSec))
                        {
                            // 元素已失效（如歌曲切换导致元素重建），清除缓存走冷路径重新扫描
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

                // 冷路径：缓存失效时才执行全量扫描，同时限制最小扫描间隔避免频繁遍历拖慢性能；
                // 长尾扫描只会阻塞本 worker，不会阻塞主线程的歌曲检测逻辑。
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
                    bool progressChanged = !hasObservedProgress || currentSec != lastObservedCurrentSec;

                    lastObservedCurrentSec = currentSec;
                    hasObservedProgress = true;

                    if (progressChanged)
                    {
                        PublishProgress(generation, currentSec, totalSec);
                    }
                    else
                    {
                        // UIA 缓存元素在鼠标移出进度条后，可能仍会返回上一次的历史时间。
                        // 如果已播放时间没有变化，则认为当前没有新的进度信息，不再继续输出历史值。
                        ClearPublishedProgress(generation);
                    }
                }
                else
                {
                    // 当前无法解析出有效进度时，不继续保留上一份可输出的进度缓存。
                    ClearPublishedProgress(generation);
                }

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
            ReleaseComObject(cachedProgressElement);
            ReleaseComObject(cachedWindow);
            ReleaseComObject(automation);
        }
    }

    /// <summary>
    /// 将本次读取结果写入共享状态。
    /// 写入前再次校验 generation，防止 worker 在读取期间发生曲目切换时污染新曲目的进度缓存。
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
    /// 清除当前可输出的进度缓存，但保留 worker 内部记录的上一次进度，
    /// 避免 UIA 在鼠标移出进度条后持续返回历史值时重复输出相同进度。
    /// </summary>
    private static void ClearPublishedProgress(long generation)
    {
        lock (StateLock)
        {
            if (generation != _requestedGeneration || string.IsNullOrEmpty(_requestedTrack))
            {
                return;
            }

            _currentSec = -1;
            _totalSec = -1;
            _resultGeneration = -1;
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
    /// 通过 UI Automation 在窗口 UIA 子树内搜索所有 Text 控件，
    /// 优先匹配形如 "MM:SS / MM:SS" 或 "MM:SS | MM:SS" 的单个文本元素，解析进度。
    /// 命中后返回该元素供 worker 跨 tick 缓存（热路径复用，避免重复遍历 UIA 树）。
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
