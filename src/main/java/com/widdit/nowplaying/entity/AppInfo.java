package com.widdit.nowplaying.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppInfo {

    // ============= 基本信息 =============

    // 操作系统名称
    private String operatingSystem;

    // 系统位数
    private Integer osBit;

    // 软件安装目录
    private String installPath;

    // 程序运行时长（HH:mm:ss）
    private String programRunningTime;

    // ============= 通用设置 =============

    // 音频设备 ID
    private String deviceId;

    // 音频设备名称
    private String deviceName;

    // 音乐平台
    private String platform;

    // 启动时自动打开主页
    private Boolean autoLaunchHomePage;

    // 开机自启
    private Boolean runAtStartup;

    // 检查更新频率（天）
    private Integer updateCheckFreq;

    // 优先使用 SMTC
    private Boolean smtc;

    // 启用备选音乐平台
    private Boolean fallbackPlatformEnabled;

    // 备选音乐平台
    private String fallbackPlatform;

    // 轮询间隔（毫秒）
    private Integer pollInterval;

    // ============= 歌词设置 =============

    private String lyricSource;

    private Boolean autoSelectBestLyric;

}
