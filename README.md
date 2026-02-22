<div align="center">

![Banner](/images/now_playing_2_banner_3.png)

<h2>一款用于直播/桌面的「正在播放」歌曲信息展示工具</h2>

此仓库为 Now Playing 后端代码仓库，前端代码请访问：[now-playing-frontend](https://github.com/Widdit/now-playing-frontend)

QQ 交流群：150453391

</div>

---

## 功能特性

### 核心功能

- 歌曲组件：实时检测并展示正在播放的歌曲信息，包括歌名、作者、封面、时长及播放进度
- 歌词组件：提供近 40 个配置项，支持全方位自定义；智能匹配最佳歌词，同时获取多种歌词源并采用最佳结果
- 播放器：仿 Apple Music 风格的播放器页面，展示歌曲信息与滚动歌词；支持电脑、移动设备等多端同步使用
- 信息输出：系统会生成包含当前歌曲信息的文件，供直播软件读取，支持模板自定义展示歌曲信息
- API 接口：开发者可利用 Now Playing API 自行设计前端页面，并通过软件内置服务器进行本地部署

### 支持平台

支持 20+ 音乐平台，持续扩充中：

| 类型 | 平台 |
|:---:|:---|
| 国内平台 | 网易云音乐、QQ 音乐、酷狗音乐、酷我音乐、汽水音乐 |
| 国外平台 | Spotify、Apple Music、YouTube Music |
| 点歌机 | 咩播、卡西米尔唱片机、花花直播助手、BQ 点歌姬 |
| 本地播放器 | PotPlayer、Foobar2000、AIMP |
| 其它 | 洛雪音乐、MusicFree、Cider、YesPlayMusic |

### 检测能力

- **歌曲信息**：歌名、歌手名、专辑名、歌曲封面、时长
- **进度条信息**：进度条位置、播放状态（播放/暂停）
- **识别方式**：支持窗口标题 / SMTC 两种方式

### 兼容性

- 直播软件：适用于 OBS、B 站直播姬、抖音直播伴侣、斗鱼直播伴侣、虎牙直播、视频号直播、小红书直播、Streamlabs 等各类直播软件
- 操作系统：Windows 10 / 11（64 位）

---

## 界面预览

![](/images/app_preview.png)

---

## 使用方法

### 方法一：下载整合包（推荐）

- 前往 [Release](https://github.com/Widdit/now-playing-service/releases) 页面下载整合包
- 开箱即用，无需进行任何配置

### 方法二：结合第三方网站

1. 前往 [网站](https://6klabs.com/amuse) 并使用 Google 账号登录（仅登录时需要梯子）
2. 点击 "Widgets" 页面进入组件设置页面
3. 在 "Music Service" 中选择 "YouTube Music"，下方样式设置可自由调整
4. 在 "OBS Setup" 中点击 "Click here to copy your URL" 复制链接
5. 在直播软件中添加浏览器源，将复制的 URL 填入

### 方法三：使用 API 自行开发

- 开发者可利用 Now Playing API 自行设计前端页面，并通过软件内置服务器进行本地部署

- API 接口界面：

![](/images/api_preview.png)

---

## 开发引导

### 核心代码（Java）

使用 IDEA 打开项目（需注意：请使用 JDK 11 运行）：

| 操作 | 步骤                                                                          |
|:---:|:----------------------------------------------------------------------------|
| 运行 | 运行 `NowPlayingApplication` 类的 `main` 方法                                     |
| 打包 | 依次双击 IDEA 右侧 Maven → Lifecycle 的 `clean` 和 `package`，在 `target` 目录下生成 JAR 包 |
| JAR To EXE | 使用 [exe4j](https://www.ej-technologies.com/exe4j/download) 将 JAR 包转为 EXE    |

### 外部程序（C#）

`external_programs/AudioService/GetMusicStatus` 使用 VS Code 打开：

| 操作 | 命令 |
|:---:|:---|
| 运行 | `dotnet run` |
| 打包 | `dotnet publish -c Release -r win-x64 --self-contained -o ./publish` |

---

## 程序原理

通过 `Assets/AudioService/GetMusicStatus.exe` 程序检测音乐软件的播放状态（Playing、Paused、None）和歌曲信息，通常情况下每隔 1 秒输出一次，当检测到歌曲状态变化时会立即输出。

<details>
<summary>点击展开详细原理</summary>

### 命令行参数

接收命令行参数（具体取值请参考 [源码](https://github.com/Widdit/now-playing-service/blob/master/external_programs/AudioService/GetMusicStatus/Program.cs)）：

| 参数 | 含义 | 描述 |
|:---|:---|:---|
| `--device-id` | 音频设备 ID | 仅检测该音频设备，默认值为 `default`，检测默认音频设备 |
| `--platform` | 音乐平台 | 期望检测的音乐软件平台，默认值为 `netease`，检测网易云音乐 |
| `--smtc` | 是否优先使用 SMTC | 优先通过 SMTC 识别歌曲信息，默认值为 `true` |
| `--poll-interval` | 轮询间隔（ms） | 建议取值范围为 100~1000 ms，默认值为 `100` ms（最快） |

### 独立运行示例

`GetMusicStatus.exe` 程序可独立运行，运行示例如下图所示：

![](/images/getMusicStatus_output.png)

### 执行流程

![](/images/flow_chart.png)

> **说明**：由于播放进度是通过算法逻辑计算出来的（无法直接获取），所以拖动进度条无法检测，但暂停动作可以检测到。该部分使用了 [这段代码](https://stackoverflow.com/questions/23182880/check-if-an-application-emits-sound) 来获取指定进程的实时音量，从而判断音乐是否暂停。

</details>

---

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=Widdit/now-playing-service&type=date&legend=top-left)](https://www.star-history.com/#Widdit/now-playing-service&type=date&legend=top-left)
