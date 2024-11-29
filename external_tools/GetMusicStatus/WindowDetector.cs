using System;
using System.Text;
using System.Diagnostics;
using System.Collections.Generic;
using System.Runtime.InteropServices;

public class WindowDetector
{
    [DllImport("user32.dll", SetLastError = true)]
    private static extern bool EnumWindows(EnumWindowsProc lpEnumFunc, IntPtr lParam);

    [DllImport("user32.dll", SetLastError = true)]
    private static extern int GetWindowText(IntPtr hWnd, StringBuilder lpString, int nMaxCount);

    [DllImport("user32.dll", SetLastError = true)]
    private static extern int GetWindowTextLength(IntPtr hWnd);

    [DllImport("user32.dll", SetLastError = true)]
    private static extern uint GetWindowThreadProcessId(IntPtr hWnd, out uint processId);

    [DllImport("user32.dll", SetLastError = true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    private static extern bool IsWindowVisible(IntPtr hWnd);

    private delegate bool EnumWindowsProc(IntPtr hWnd, IntPtr lParam);

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
                        windowTitle = Process.GetProcessById((int)processId).MainWindowTitle;
                        return false;  // Stop enumerating
                    }
                }
                catch (Exception) { }  // Ignore errors
            }

            return true;  // Continue enumerating
        }, IntPtr.Zero);

        return windowTitle;
    }

    public static List<string> GetWindowTitles(string processName)
    {
        List<string> windowTitles = new List<string>();
        uint targetProcessId = (uint)Process.GetProcessesByName(processName)[0].Id;

        EnumWindows(new EnumWindowsProc((hWnd, lParam) =>
        {
            uint pid;
            GetWindowThreadProcessId(hWnd, out pid);

            if (pid == targetProcessId)
            {
                StringBuilder sb = new StringBuilder(256);
                GetWindowText(hWnd, sb, sb.Capacity);
                string title = sb.ToString();

                if (!string.IsNullOrWhiteSpace(title))
                {
                    windowTitles.Add(title);
                }
            }

            return true;  // Continue enumerating
        }), IntPtr.Zero);

        return windowTitles;
    }
}