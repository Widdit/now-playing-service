using System;
using System.Collections.Generic;
using CSCore.CoreAudioAPI;

public class NeteaseMusicService : MusicService
{
    private const int NONE_HOLDOVER_MS = 1500;
    private string _lastGoodOutput = null;
    private DateTime _lastGoodOutputAt = DateTime.MinValue;

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
            NeteaseMusicHelper.ReadProgressViaUIA(out currentSec, out totalSec);
        }
        catch (Exception)
        {
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
    /// 
    /// 原因：
    /// 拖动进度条时，网易云可能短暂断开音频会话 / UIA 读取瞬时失败，这会让本服务返回 "None"
    /// 解决思路：若某次轮询的结果为 "None"，但最近 1.5s 内曾经输出过有效结果，
    /// 就继续沿用那份旧输出，等真正闭屏再返回 None，避免把瞬时失联放大成可见事件。
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
}