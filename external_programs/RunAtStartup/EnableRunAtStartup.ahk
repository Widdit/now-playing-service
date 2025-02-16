#SingleInstance, Force


SetWorkingDir %A_ScriptDir%

; 以管理员权限运行
RunAsAdmin()

; 获取主程序路径
SplitPath, A_ScriptDir, , parentDir
exePath := parentDir "\Now Playing.exe"

if FileExist(exePath)
{
    ; 写入注册表
    RegWrite, REG_SZ, HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\Run, Now Playing, "%exePath%"
}
else
{
    MsgBox, 0x10, , 错误：未找到主程序！`n`n预期路径：%exePath%
}

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
