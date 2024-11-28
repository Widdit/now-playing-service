using System;
using System.Text;
using System.Diagnostics;
using System.Runtime.InteropServices;

public class WindowDetector
{
    const int GW_HWNDNEXT = 2;

    [DllImport("user32.dll", SetLastError = true)]
    static extern bool EnumWindows(EnumWindowsProc lpEnumFunc, IntPtr lParam);

    [DllImport("user32.dll", SetLastError = true)]
    static extern int GetWindowText(IntPtr hWnd, StringBuilder lpString, int nMaxCount);

    [DllImport("user32.dll", SetLastError = true)]
    static extern int GetWindowTextLength(IntPtr hWnd);

    [DllImport("user32.dll", SetLastError = true)]
    static extern uint GetWindowThreadProcessId(IntPtr hWnd, out uint processId);

    [DllImport("user32.dll", SetLastError = true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    static extern bool IsWindowVisible(IntPtr hWnd);

    delegate bool EnumWindowsProc(IntPtr hWnd, IntPtr lParam);

    public string GetWindowTitle(string processName)
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
}