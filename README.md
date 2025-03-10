# NowPlaying Service

**▶︎ ၊၊||၊|။|||| |   一款直播歌曲歌名显示组件**

QQ 交流群：150453391

![](/images/now_playing_release_banner.png)



## 功能特性

- 支持国内主流音乐软件（网易云音乐、QQ 音乐、酷狗音乐、酷我音乐、汽水音乐）
- 支持国外平台（Spotify、Apple Music）
- 支持点歌（卡西米尔唱片机、花花直播助手）
- 支持本地播放器（PotPlayer、Foobar2000、洛雪音乐、MusicFree）
- 能够实时检测正在播放的 **歌曲信息** 以及 **进度条信息**
  - 歌曲信息
    - 歌名
    - 专辑名
    - 作者名
    - 歌曲封面 URL
    - 时长
  - 进度条信息
    - 进度条位置
    - 是否暂停
- 提供查询 API 接口
- 支持两种方式识别歌曲：窗口标题或 SMTC
- 适用于 OBS、B 站直播姬、虎牙直播、Streamlabs 等各类直播软件



## 使用方法

### 方法一

- 前往 [Release](https://github.com/Widdit/now-playing-service/releases) 页面下载整合包，开箱即用，无需进行任何配置

### 方法二

- 结合 [该网站](https://6klabs.com/amuse) 使用
- 首先使用 Google 账号登录（只有登录时才用到梯子），然后点击 "Widgets" 页面进入组件的设置页面
- 第一行 "Music Service" 一定要选择 "YouTube Music"。后面的设置就是歌曲显示组件的样式，可自由选择
- 在 "OBS Setup" 里点击 "Click here to copy your URL"
- 在直播软件中添加浏览器源，URL 填入刚才复制的内容。此时打开音乐软件播放歌曲，便能看到正在播放的歌曲被显示出来。效果如下所示：
![](/images/sample_1.png)
![](/images/sample_2.png)

### 方法三

- 自己编写前端页面来渲染歌曲组件，调用接口 `http://localhost:9863/query`（请求方式为 GET，建议每秒调用一次）
- 下图为接口响应示例。其中 player 为播放器信息，track 为歌曲信息

![](/images/query_response.png)



## 如何编译

- 由于版权原因，该项目仅包含后端代码
- 核心代码使用 Java 编写，使用 IDEA 打开即可
  - 运行：运行 NowPlayingApplication 类的 main 方法
  - 打包：双击 IDEA 右侧 Maven - Lifecycle 的 clean，然后再双击 package，即可在 target 目录下生成 JAR 包
  - JAR To EXE：你可以使用 [exe4j](https://www.ej-technologies.com/exe4j/download) 将 JAR 包转为 EXE
- 外部程序 `external_programs/AudioService/GetMusicStatus` 的代码使用 C# 编写，使用 VS Code 打开
  - 运行：`dotnet run`
  - 打包：`dotnet publish -c Release -r win-x64 --self-contained -o ./publish`



## 程序原理

首先通过 `Assets/AudioService/GetMusicStatus.exe` 程序检测音乐软件的播放状态（Playing, Paused, None）和歌曲信息，每隔 1 秒输出一次。

接收命令行参数（具体取值请参考 [源码](https://github.com/Widdit/now-playing-service/blob/master/external_programs/AudioService/GetMusicStatus/Program.cs)）：

| 参数        | 含义 | 描述                                                         |
| ----------- | ---- | ------------------------------------------------------------ |
| --device-id | 音频设备 ID | 仅检测该音频设备，默认值为 "default"，检测默认音频设备       |
| --platform  | 音乐平台 | 期望检测的音乐软件平台，默认值为 "netease"，检测网易云音乐 |
| --smtc      | 是否优先使用 SMTC | 默认值为 true，优先通过 SMTC 识别歌曲信息 |

该程序可独立运行，运行示例如下图所示：
![](/images/getMusicStatus_output.png)
程序执行流程图如下图所示：
![](/images/flow_chart.png)
由于播放进度是通过算法逻辑计算出来的（这个不可能直接获取到），所以拖动进度条是无法检测的，但是暂停动作可以检测到。该部分使用到了 [这段代码](https://stackoverflow.com/questions/23182880/check-if-an-application-emits-sound) 来获取指定进程的实时音量，从而判断音乐是否暂停。