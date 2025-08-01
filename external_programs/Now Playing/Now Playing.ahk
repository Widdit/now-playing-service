﻿#Persistent
#SingleInstance, Force
#Include JSON.ahk


/*
    =============================================
    ****************** 版本信息 ******************
    =============================================
*/
currentVersion := "1.0.9"


SetWorkingDir %A_ScriptDir%

; 如果程序在 C 盘，则以管理员身份重新运行
if (SubStr(A_ScriptFullPath, 1, 2) = "C:")
{
    RunAsAdmin()
}

; 常量
PLUGIN_SETTINGS_PATH := "Plugins/settings-plugin.json"

; 通用设置项
autoLaunchHomePage := true  ; 是否自动打开主页

; 读取通用设置文件
settingsFile := FileOpen("Settings/settings.json", "r", "UTF-8")
if (settingsFile)
{
    settingsStr := settingsFile.Read()
    settingsFile.Close()

    ; 将字符串反序列化为对象
    settingsObj := JSON.Load(settingsStr)
    autoLaunchHomePage := settingsObj.autoLaunchHomePage
}

; 读取插件设置文件
pluginSettingsFile := FileOpen(PLUGIN_SETTINGS_PATH, "r", "UTF-8")
if (pluginSettingsFile)
{
    pluginSettingsStr := pluginSettingsFile.Read()
    pluginSettingsFile.Close()

    ; 将字符串反序列化为对象
    pluginSettingsObj := JSON.Load(pluginSettingsStr)
}
else
{
    ; 创建默认插件设置文件
    pluginSettingsObj := {}
    pluginSettingsObj.desktopWidgetEnabled := false  ; 是否启用桌面组件

    ; 将对象序列化为字符串
    pluginSettingsStr := JSON.Dump(pluginSettingsObj, , "`t")

    FileCreateDir, Plugins
    FileAppend, %pluginSettingsStr%, %PLUGIN_SETTINGS_PATH%, UTF-8
}

; 软件退出前需执行操作
OnExit("ExitFunc")

; 如果不自动打开主页，则需要检查更新
if (!autoLaunchHomePage)
{
    try
    {
        versionStr := GetRequest("https://gitee.com/widdit/now-playing/raw/master/version.json")
        versionObj := JSON.Load(versionStr)
        latestVersion := versionObj.latestVersion

        if (VersionCompare(currentVersion, latestVersion) < 0)
        {
            updateDate := versionObj.updateDate
            updateLog := versionObj.updateLog

            MsgBox, 0x31, , 检测到新版本 %latestVersion%，是否前往下载？`n`n发布时间：`n%updateDate%`n`n更新日志：`n%updateLog%
            IfMsgBox OK
            {
                OpenPage("https://gitee.com/widdit/now-playing/releases", "下载页面")
                ExitApp
            }
        }
    }
    catch
    {
        ; Ignored
    }
}

; 检测是否有 jre，如果没有，则自动下载并解压
if (!FileExist("jre\bin\java.exe"))
{
    if (FileExist("jre"))
    {
        FileRemoveDir, jre, 1
    }

    RunWait, Assets\DownloadJava.exe
    if (ErrorLevel)
    {
        ExitApp
    }

    Sleep 200

    if (!FileExist("jre\bin\java.exe"))
    {
        MsgBox, 0x10, , 安装 OpenJDK11 失败！请按照图文教程手动完成安装
        OpenPage("https://www.kdocs.cn/l/cePnk1nX4Glw", "教程")
        ExitApp
    }
}

; 启动 NowPlayingService.exe
if (!FileExist("NowPlayingService.exe"))
{
    MsgBox, 0x10, , 未找到 NowPlayingService.exe！请检查软件安装目录
    ExitApp
}
Run, NowPlayingService.exe, , Hide, servicePID

Sleep 1000

; 检查 NowPlayingService.exe 是否成功启动
Process, Exist, %servicePID%
if (ErrorLevel = 0)
{
    MsgBox, 0x10, , 该版本程序不兼容！请按照教程安装其它版本
    OpenPage("https://www.kdocs.cn/l/cujPFHSMXiAJ")
    ExitApp
}

; 新版本第一次运行强制打开设置页面
if (FileExist("Assets\first_run.txt"))
{
    OpenPage("http://localhost:9863", "主页")
    Sleep 2000
    OpenPage("http://localhost:9863/lyric", "新版本页面")
    FileDelete, Assets\first_run.txt
}
else if (autoLaunchHomePage)  ; 自动打开主页
{
    OpenPage("http://localhost:9863", "主页")
}

; 每 2 秒检查 NowPlayingService.exe 是否存在，不存在则退出程序
SetTimer, CheckProcess, 2000

; 创建组件预览子菜单
Menu, PreviewMenu, Add, Main, MenuWidgetPreviewHandler
Menu, PreviewMenu, Add, 配置文件A, MenuWidgetPreviewHandler
Menu, PreviewMenu, Add, 配置文件B, MenuWidgetPreviewHandler
Menu, PreviewMenu, Add, 配置文件C, MenuWidgetPreviewHandler
Menu, PreviewMenu, Add, 配置文件D, MenuWidgetPreviewHandler

; 创建托盘右键菜单
Menu, Tray, NoStandard  ; 移除自带菜单项
Menu, Tray, Add, 主页, MenuHomeHandler
Menu, Tray, Add, 设置, MenuSettingsHandler
Menu, Tray, Add, 组件预览, :PreviewMenu
Menu, Tray, Add  ; 分割线
Menu, Tray, Add, 启用桌面组件, DesktopWidgetEnableHandler
Menu, Tray, Add, 桌面组件设置, DesktopWidgetShowWindowHandler
Menu, Tray, Add  ; 分割线
Menu, Tray, Add, 打开歌词页面, LyricTestHandler
Menu, Tray, Add  ; 分割线
Menu, Tray, Add, 重新启动, MenuRestartHandler
Menu, Tray, Add, 退出, MenuExitHandler

if (pluginSettingsObj.desktopWidgetEnabled)
{
    Menu, Tray, Check, 启用桌面组件
    Sleep 1000
    Run, Plugins\desktop-widget\now-playing-desktop-widget.exe
}

return

; ======================================== 主程序执行完毕 ========================================

; 检查 NowPlayingService.exe 是否存在，不存在则退出软件
CheckProcess:
Process, Exist, %servicePID%
if (ErrorLevel = 0)
{
    MsgBox, 服务被终止
    ExitApp
}
return


; 软件退出前需执行操作
ExitFunc()
{
    global servicePID
    global pluginSettingsObj

    Process, Close, %servicePID%
    Process, Close, GetMusicStatus.exe

    if (pluginSettingsObj.desktopWidgetEnabled)
    {
        ; 关闭桌面组件
        try
        {
            GetRequest("http://localhost:9864/exit")
            Sleep 500
        }
        catch
        {
            ; Ignored
        }
        Process, Close, now-playing-desktop-widget.exe
    }
}


; 发送 GET 请求，返回响应字符串
GetRequest(url)
{
    whr := ComObjCreate("WinHttp.WinHttpRequest.5.1")
    whr.Open("GET", url, true)
    whr.Send()
    whr.WaitForResponse()

    arr := whr.responseBody
    pData := NumGet(ComObjValue(arr) + 8 + A_PtrSize)
    length := arr.MaxIndex() + 1
    responseStr := StrGet(pData, length, "utf-8")

    return responseStr
}


; 打开页面（带异常捕获）
OpenPage(url, name := "页面")
{
    try
    {
        Run, %url%
    }
    catch
    {
        Clipboard := url
        MsgBox, 0x10, , 打开%name%失败，请手动打开页面！（链接已复制到剪贴板）`n`n您可以通过重新设置【默认浏览器】来解决此问题
    }
}


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


; 比较两个版本号（格式形如 1.0.5）。如果 version1 < version2，返回 -1；大于返回 1；相等返回 0
VersionCompare(version1, version2)
{
    version1Array := StrSplit(version1, ".")
    version2Array := StrSplit(version2, ".")

    Loop, % Max(version1Array.Length(), version2Array.Length())
    {
        part1 := (A_Index <= version1Array.Length()) ? version1Array[A_Index] : 0
        part2 := (A_Index <= version2Array.Length()) ? version2Array[A_Index] : 0

        if (part1 < part2) {
            return -1
        } else if (part1 > part2) {
            return 1
        }
    }

    return 0
}


; 保存插件设置
SavePluginSettings()
{
    global PLUGIN_SETTINGS_PATH
    global pluginSettingsObj

    ; 将对象序列化为字符串
    pluginSettingsStr := JSON.Dump(pluginSettingsObj, , "`t")

    FileDelete, %PLUGIN_SETTINGS_PATH%
    FileAppend, %pluginSettingsStr%, %PLUGIN_SETTINGS_PATH%, UTF-8
}


; 托盘菜单 - 主页
MenuHomeHandler:
OpenPage("http://localhost:9863")
return


; 托盘菜单 - 设置
MenuSettingsHandler:
OpenPage("http://localhost:9863/settings")
return


; 托盘菜单 - 组件预览
MenuWidgetPreviewHandler:
if (A_ThisMenuItem = "Main")
{
    OpenPage("http://localhost:9863/widget")
}
else if (A_ThisMenuItem = "配置文件A")
{
    OpenPage("http://localhost:9863/widget/profileA")
}
else if (A_ThisMenuItem = "配置文件B")
{
    OpenPage("http://localhost:9863/widget/profileB")
}
else if (A_ThisMenuItem = "配置文件C")
{
    OpenPage("http://localhost:9863/widget/profileC")
}
else if (A_ThisMenuItem = "配置文件D")
{
    OpenPage("http://localhost:9863/widget/profileD")
}
return


; 托盘菜单 - 启用桌面组件
DesktopWidgetEnableHandler:
Menu, Tray, ToggleCheck, 启用桌面组件
pluginSettingsObj.desktopWidgetEnabled := !pluginSettingsObj.desktopWidgetEnabled

if (pluginSettingsObj.desktopWidgetEnabled)
{
    ; 运行桌面组件
    Run, Plugins\desktop-widget\now-playing-desktop-widget.exe
}
else
{
    ; 关闭桌面组件
    try
    {
        GetRequest("http://localhost:9864/exit")
        Sleep 500
    }
    catch
    {
        ; Ignored
    }
    Process, Close, now-playing-desktop-widget.exe
}

SavePluginSettings()
return


; 托盘菜单 - 桌面组件设置
DesktopWidgetShowWindowHandler:
if (!pluginSettingsObj.desktopWidgetEnabled)
{
    MsgBox, 0x30, , 您需要先启用桌面组件！
    return
}

try
{
    if (GetRequest("http://localhost:9864/show") != "ok")
    {
        throw Exception("error")
    }
}
catch
{
    MsgBox, 0x10, , 显示桌面组件设置失败！
}
return


; 托盘菜单 - 打开歌词页面
LyricTestHandler:
OpenPage("http://localhost:9863/lyric")
return


; 托盘菜单 - 重新启动
MenuRestartHandler:
SetTimer, CheckProcess, Off
Sleep 500

Process, Close, %servicePID%
Process, Close, GetMusicStatus.exe
Sleep 1000
Run, NowPlayingService.exe, , Hide, servicePID

Sleep 1000

; 自动打开主页
if (autoLaunchHomePage)
{
    OpenPage("http://localhost:9863", "主页")
}

; 每 2 秒检查 NowPlayingService.exe 是否存在，不存在则退出程序
SetTimer, CheckProcess, 2000

return


; 托盘菜单 - 退出
MenuExitHandler:
ExitApp
