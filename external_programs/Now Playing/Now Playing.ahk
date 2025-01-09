#Persistent
#SingleInstance, Force
#Include JSON.ahk


/*
    =============================================
    ****************** 版本信息 ******************
    =============================================
*/
currentVersion := "1.0.6"


; 通用设置项
autoLaunchHomePage := true  ; 是否自动打开主页

; 读取通用设置文件
settingsFile := FileOpen("Settings/settings.json", "r", "UTF-8")
if (settingsFile)
{
    settingsStr := settingsFile.Read()
    settingsFile.Close()

    if (InStr(settingsStr, "autoLaunchHomePage"))
    {
        settingsObj := JSON.Load(settingsStr)
        autoLaunchHomePage := settingsObj.autoLaunchHomePage
    }
}

; 软件退出前需执行操作
OnExit("exitFunc")

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
                Run, https://gitee.com/widdit/now-playing/releases
                ExitApp
            }
        }
    }
    catch
    {
        ; Ignored
    }
}

; 检测 Java 运行环境
if (!FileExist("jre") && !FileExist("jre1.8") && !FileExist("jre-1.8") && !FileExist("jdk1.8") && !FileExist("jdk-1.8"))
{
    EnvGet, javaHome, JAVA_HOME
    if (javaHome = "")
    {
        MsgBox, 0x10, , JAVA_HOME 环境变量未配置！`n如果您已配置，重启电脑后生效
        ExitApp
    }
    javaExePath := javaHome . "\bin\java.exe"
    if (!FileExist(javaExePath))
    {
        MsgBox, 0x10, , JAVA_HOME 环境变量配置有误，请检查！`nJAVA_HOME: %javaHome%
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
    MsgBox, 0x10, , 该版本程序不兼容！请按照教程安装 32 位补丁
    Run, https://www.kdocs.cn/l/cujPFHSMXiAJ
    ExitApp
}

; 自动打开主页
if (autoLaunchHomePage)
{
    Run, http://localhost:9863
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
Menu, Tray, Add, 重新启动, MenuRestartHandler
Menu, Tray, Add, 退出, MenuExitHandler

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
exitFunc()
{
    global servicePID

    Process, Close, %servicePID%
    Process, Close, GetMusicStatus.exe
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


; 托盘菜单 - 主页
MenuHomeHandler:
Run, http://localhost:9863
return


; 托盘菜单 - 设置
MenuSettingsHandler:
Run, http://localhost:9863/settings
return


; 托盘菜单 - 组件预览
MenuWidgetPreviewHandler:
if (A_ThisMenuItem = "Main")
{
    Run, http://localhost:9863/widget
}
else if (A_ThisMenuItem = "配置文件A")
{
    Run, http://localhost:9863/widget/profileA
}
else if (A_ThisMenuItem = "配置文件B")
{
    Run, http://localhost:9863/widget/profileB
}
else if (A_ThisMenuItem = "配置文件C")
{
    Run, http://localhost:9863/widget/profileC
}
else if (A_ThisMenuItem = "配置文件D")
{
    Run, http://localhost:9863/widget/profileD
}
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
    Run, http://localhost:9863
}

; 每 2 秒检查 NowPlayingService.exe 是否存在，不存在则退出程序
SetTimer, CheckProcess, 2000

return


; 托盘菜单 - 退出
MenuExitHandler:
ExitApp
