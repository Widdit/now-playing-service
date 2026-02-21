#Requires AutoHotkey v2.0
#SingleInstance Force
#Include Lib\WebViewToo.ahk
#Include JSON.ahk
#NoTrayIcon
Gui.ProtoType.AddWebViewCtrl := WebViewCtrl
;///////////////////////////////////////////////////////////////////////////////////////////

; 主程序
;///////////////////////////////////////////////////////////////////////////////////////////
if (A_IsCompiled) {
	WebViewCtrl.CreateFileFromResource(DllPath := ((A_PtrSize * 8) "bit\WebView2Loader.dll"), WebViewCtrl.TempDir)
}

WebViewSettings := {
    DataDir: WebViewCtrl.TempDir,
    DllPath: A_IsCompiled ? WebViewCtrl.TempDir "\" (A_PtrSize * 8) "bit\WebView2Loader.dll" : "WebView2Loader.dll",
    Transparent: true
}

; 获取窗口设置
windowSettingsStr := GetRequest("http://localhost:9863/api/settings/plugin/windowWidget")
windowSettingsObj := JSON.Load(windowSettingsStr)

; 创建歌曲组件窗口
if (windowSettingsObj["songWindow"]["enabled"]) {
    width := windowSettingsObj["songWindow"]["width"]
    height := windowSettingsObj["songWindow"]["height"]

    if (width = "")
        width := 800
    width := width > 2560 ? 2560 : width

    if (height = "")
        height := 600
    height := height > 2560 ? 2560 : height

    SongGui := Gui("-MinimizeBox -MaximizeBox -Caption +Border", "歌曲组件窗口 - Now Playing")
    SongGui.OnEvent("Close", GuiClose)
    SongGui.BackColor := "f0f0f0"
    SongGui.AddWebViewCtrl("xm w" width " h" height " vWebView", WebViewSettings)
    SongGui.Show()

    WinSetTransColor("f0f0f0", SongGui.Hwnd)

    SongGui["WebView"].Navigate("http://localhost:9863/widget")
}

; 创建歌词组件窗口
if (windowSettingsObj["lyricWindow"]["enabled"]) {
    width := windowSettingsObj["lyricWindow"]["width"]
    height := windowSettingsObj["lyricWindow"]["height"]

    if (width = "")
        width := 800
    width := width > 2560 ? 2560 : width

    if (height = "")
        height := 600
    height := height > 2560 ? 2560 : height

    LyricGui := Gui("-MinimizeBox -MaximizeBox -Caption +Border", "歌词组件窗口 - Now Playing")
    LyricGui.OnEvent("Close", GuiClose)
    LyricGui.BackColor := "f0f0f0"
    LyricGui.AddWebViewCtrl("xm w" width " h" height " vWebView", WebViewSettings)
    LyricGui.Show()

    WinSetTransColor("f0f0f0", LyricGui.Hwnd)

    LyricGui["WebView"].Navigate("http://localhost:9863/lyric")
}


;///////////////////////////////////////////////////////////////////////////////////////////

GuiClose(*) {
    result := MsgBox("您确定要关闭窗口吗？", "Now Playing", "OKCancel Icon!")
    
    if (result = "OK")
        ExitApp()
    else
        return true
}

; 发送 GET 请求，返回响应字符串
GetRequest(url) {
    whr := ComObject("WinHttp.WinHttpRequest.5.1")
    whr.Open("GET", url, true)
    whr.Send()
    whr.WaitForResponse()

    arr := whr.responseBody
    pData := NumGet(ComObjValue(arr) + 8 + A_PtrSize, "UPtr")
    length := arr.MaxIndex() + 1
    responseStr := StrGet(pData, length, "utf-8")

    return responseStr
}

; 引入资源到编译后的脚本中
;///////////////////////////////////////////////////////////////////////////////////////////
;@Ahk2Exe-AddResource Lib\32bit\WebView2Loader.dll, 32bit\WebView2Loader.dll
;@Ahk2Exe-AddResource Lib\64bit\WebView2Loader.dll, 64bit\WebView2Loader.dll
;///////////////////////////////////////////////////////////////////////////////////////////
