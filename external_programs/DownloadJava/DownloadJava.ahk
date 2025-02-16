#SingleInstance, Force
#Include ./Neutron.ahk


; 初始化 GUI 界面
loadingGUI := new NeutronWindow()

loadingGUI.Load("loading.html")
loadingGUI.Gui("+LabelLoadingGUI")
loadingGUI.Gui("-Resize")
loadingGUI.Show("w800 h550")
loadingGUI.doc.getElementById("loading-text").innerText := "正在下载 OpenJDK11 依赖..."

; TUNA 镜像源
url := "https://mirrors.tuna.tsinghua.edu.cn/Adoptium/11/jre/x64/windows"
if (!A_Is64bitOS) {
    url := "https://mirrors.tuna.tsinghua.edu.cn/Adoptium/11/jre/x32/windows"
}

html := HttpGet(url)

; 使用正则表达式提取 OpenJDK11U-jre...zip 字符串
RegExMatch(html, "OpenJDK11U-jre[^\s]+\.zip", fileName)

if (!fileName)
{
    MsgBox, 16, Error, 未能从清华大学镜像站中找到 OpenJDK11 的下载链接！`n`n请按照图文教程手动完成下载。
    OpenPage("https://www.kdocs.cn/l/cePnk1nX4Glw", "教程")
    ExitApp, 1001
}

downloadLink := url "/" fileName

; 设置下载文件的路径
filePath := A_WorkingDir "\" fileName

; 下载文件
URLDownloadToFile, % downloadLink, % filePath
if (ErrorLevel) {
    MsgBox, 16, Error, 下载 OpenJDK11 失败！请按照图文教程手动完成下载
    OpenPage("https://www.kdocs.cn/l/cePnk1nX4Glw", "教程")
    ExitApp, 1002
}

loadingGUI.doc.getElementById("loading-text").innerText := "正在解压文件..."

; 解压 ZIP 文件
RunWait, %ComSpec% /c "powershell -Command Expand-Archive -Path '%filePath%' -DestinationPath '%A_WorkingDir%' -Force", , Hide

folderName := ""
; 遍历工作目录，找到名称以 "-jre" 结尾的文件夹
Loop Files, % A_WorkingDir "\*-jre", D
{
    folderName := A_LoopFileName
    break
}
if (!folderName)
{
    MsgBox, 16, Error, 解压 OpenJDK11 失败！请按照图文教程手动完成解压
    OpenPage("https://www.kdocs.cn/l/cePnk1nX4Glw", "教程")
    ExitApp, 1003
}

; 将解压后的文件夹重命名为 "jre"
FileMoveDir, % A_WorkingDir "\" folderName, % A_WorkingDir "\jre", R

loadingGUI.doc.getElementById("loading-text").innerText := "安装成功"

; 删除 ZIP 文件
FileDelete, % filePath

Sleep 50

ExitApp


; ---------------------- 主程序执行完毕 ----------------------


; 将依赖集成到编译好的 EXE 文件中
; 前端页面
FileInstall, loading.html, loading.html
; 资源
FileInstall, loading.gif, loading.gif


HttpGet(url)
{
    xmlhttp := ComObjCreate("MSXML2.XMLHTTP")
    xmlhttp.Open("GET", url, false)
    xmlhttp.Send()

    responseHTML := xmlhttp.responseText
    xmlhttp := ""
    
    return responseHTML
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
