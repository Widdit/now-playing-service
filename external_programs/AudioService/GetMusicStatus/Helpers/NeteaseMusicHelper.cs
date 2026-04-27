using System;
using System.Diagnostics;
using System.Runtime.InteropServices;
using Interop.UIAutomationClient;

public static class NeteaseMusicHelper
{
    private const bool PRINT_EXCEPTION_LOG = false;

    private const int UIA_ProcessIdPropertyId = 30002;
    private const int UIA_ControlTypePropertyId = 30003;
    private const int UIA_TextControlTypeId = 50020;

    private static IUIAutomation _automation;

    /// <summary>
    /// 通过 UI Automation 从网易云主播放窗口读取进度文本
    /// 优先匹配形如 "MM:SS / MM:SS" 或 "MM:SS | MM:SS" 的单个文本元素
    /// </summary>
    public static void ReadProgressViaUIA(out int currentSec, out int totalSec)
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
    private static IUIAutomationElement FindNeteasePlayerWindow()
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
    private static void ParseProgressFromWindow(IUIAutomationElement root, out int currentSec, out int totalSec)
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
    private static bool TryParseProgressText(string text, out int currentSec, out int totalSec)
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

    private static bool TryParseTimeString(string timeStr, out int seconds)
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

    private static IUIAutomation GetAutomation()
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

    private static void ReleaseComObject(object comObject)
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