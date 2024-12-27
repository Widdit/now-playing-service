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

    private String deviceId = "default";  // 音频设备ID

    private String deviceName = "主声音驱动程序";  // 音频设备名称

    private String platform = "netease";  // 音乐平台

    private Boolean autoLaunchHomePage = true;  // 启动时自动打开主页

}
