#SingleInstance, Force


; 以管理员权限运行
RunAsAdmin()

; 从注册表中删除
RegDelete, HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\Run, Now Playing

return


; 检查当前程序是否以管理员身份运行，如果不是，则以管理员身份重新运行
RunAsAdmin()
{
    full_command_line := DllCall("GetCommandLine", "str")

    if not (A_IsAdmin or RegExMatch(full_command_line, " /restart(?!\S)"))
    {
        try
        {
            if A_IsCompiled
                Run *RunAs "%A_ScriptFullPath%" /restart
            else
                Run *RunAs "%A_AhkPath%" /restart "%A_ScriptFullPath%"
        }
        ExitApp
    }
}
