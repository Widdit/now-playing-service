#Persistent
#SingleInstance, Force


serviceRunning := false  ; NowPlayingService.exe 是否运行中

; 软件退出前需执行操作
OnExit("exitFunc")

; 检测系统 Java 运行环境
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

; 启动 NowPlayingService.exe
if (!FileExist("NowPlayingService.exe"))
{
    MsgBox, 0x10, , 未找到 NowPlayingService.exe！请检查软件安装目录
    ExitApp
}
Run, NowPlayingService.exe, , Hide, servicePID
serviceRunning := true

; 自动打开主页
Sleep 1000
try
{
    Run, http://localhost:9863
}
catch
{
    Clipboard := "http://localhost:9863"
    MsgBox, 主页打开失败，请手动打开浏览器进入主页！`n（链接已复制到剪贴板）
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
    global serviceRunning
    global servicePID

    if (serviceRunning)
    {
        Process, Close, %servicePID%
    }
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
Sleep 1000
Run, NowPlayingService.exe, , Hide, servicePID

; 自动打开主页
Sleep 1000
try
{
    Run, http://localhost:9863
}
catch
{
    Clipboard := "http://localhost:9863"
    MsgBox, 主页打开失败，请手动打开浏览器进入主页！`n（链接已复制到剪贴板）
}

; 每 2 秒检查 NowPlayingService.exe 是否存在，不存在则退出程序
SetTimer, CheckProcess, 2000

return


; 托盘菜单 - 退出
MenuExitHandler:
ExitApp
