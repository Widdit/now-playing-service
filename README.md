# NowPlaying Service

**▶︎ ၊၊||၊|။|||| |   一款直播歌曲歌名显示组件**

QQ 交流群：150453391

![](/images/now playing release banner.png)



## 功能特性

- 支持国内四大主流音乐软件（网易云音乐、QQ 音乐、酷狗音乐、酷我音乐）
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
- 适用于 OBS、B 站直播姬、抖音直播、虎牙直播、Streamlabs 等各类直播软件



## 如何安装

1. 安装 JDK 或 JRE（版本 ≥ 1.8 即可）
   - 如果你从未安装过 Java 环境，可前往 [Java 官网](https://www.java.com/zh-CN/) 进行下载
   - 安装好后需配置 JAVA_HOME 和 Path 环境变量：[教程](https://www.bilibili.com/video/BV1uJ411k7wy?p=9)
2. 前往 [Release](https://github.com/Widdit/now-playing-service/releases) 页面下载最新版本的 ZIP 压缩包
3. 将压缩包解压至任意目录，例如 D 盘根目录
4. 此时双击 `NowPlayingService.exe` 即可启动程序
   - 请不要关闭控制台（黑窗口），关闭它就意味着结束程序
   - 信息日志在 info.log 里查看，错误日志在 error.log 里查看
5. 浏览器访问 `http://localhost:9863`，当看到页面上显示 "NowPlaying Service is Running" 就说明程序运行成功

**注意事项：**如果你用网易云听歌，电脑上已经安装过 Java 环境，并且 JDK 版本 > 14，那么你需要使用旧版本 Java 来运行该程序（[点击查看操作步骤](https://www.kdocs.cn/l/cmfQeMoUD87z)）



## 使用方法

### 方法一

- 结合 [该网站](https://6klabs.com/amuse) 使用
- 首先使用 Google 账号登录（只有登录才用到梯子），然后点击 "Widgets" 页面进入组件的设置页面
- 第一行 "Music Service" 一定要选择 "YouTube Music"。后面的设置就是歌曲显示组件的样式，可自由选择
- 在 "OBS Setup" 里点击 "Click here to copy your URL"
- 在直播软件中添加浏览器源，URL 填入刚才复制的内容。此时打开音乐软件播放歌曲，便能看到正在播放的歌曲被显示出来。效果如下所示：
![](/images/sample_1.png)
![](/images/sample_2.png)

### 方法二

- 自己编写前端页面来渲染歌曲组件，调用接口 `http://localhost:9863/query`（请求方式为 GET，建议每秒调用一次）
- 下图为接口响应示例。其中 player 为播放器信息，track 为歌曲信息

![](/images/query_response.png)



## 程序原理

首先通过 `Assets\GetMusicStatus\GetMusicStatus.exe` 程序获取到音乐软件的播放状态（Playing, Paused, None）、音乐平台（Netease, QQ, KuGou, KuWo）、窗口标题。该程序可独立运行，运行示例如下图所示：
![](/images/getMusicStatus_output.png)
程序执行流程图如下图所示：
![](/images/flow_chart.png)
由于播放进度是通过算法逻辑计算出来的（这个不可能直接获取到），所以拖动进度条是无法检测的，但是暂停动作可以检测到。该部分使用到了 [这段代码](https://stackoverflow.com/questions/23182880/check-if-an-application-emits-sound) 来获取指定进程的实时音量，从而判断音乐是否暂停。