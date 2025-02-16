package com.widdit.nowplaying.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsGeneral {

    // 音频设备 ID
    private String deviceId = "default";

    // 音频设备名称
    private String deviceName = "主声音驱动程序";

    // 音乐平台
    private String platform = "netease";

    // 启动时自动打开主页
    private Boolean autoLaunchHomePage = true;

    // 优先使用 SMTC
    private Boolean smtc = true;

    // 开机自启
    private Boolean runAtStartup = false;

}
