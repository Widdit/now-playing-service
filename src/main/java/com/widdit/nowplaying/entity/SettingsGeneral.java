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

    // 开机自启
    private Boolean runAtStartup = false;

    // 检查更新频率（天）
    private Integer updateCheckFreq = 0;

    // 优先使用 SMTC
    private Boolean smtc = true;

    // 启用备选音乐平台
    private Boolean fallbackPlatformEnabled = false;

    // 备选音乐平台
    private String fallbackPlatform = "netease";

    // 轮询间隔（毫秒）
    private Integer pollInterval = 100;

    // 全民 K 歌缓存目录
    private String weSingCachePath = "";

    // SPlayer-Next 本机 API 端口
    private Integer splayerNextPort = 14558;

    public static final String YESPLAY_LEGACY = "legacy";
    public static final String YESPLAY_MCP = "mcp";
    public static final int YESPLAY_DEFAULT_PORT = 18888;

    private String yesPlayMode;
    private Integer yesPlayMcpPort;

    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @lombok.ToString.Exclude
    private String yesPlayMcpToken;

    public String getYesPlayMode() {
        return yesPlayMode == null ? YESPLAY_LEGACY : yesPlayMode;
    }

    public Integer getYesPlayMcpPort() {
        return yesPlayMcpPort == null ? YESPLAY_DEFAULT_PORT : yesPlayMcpPort;
    }

    @com.alibaba.fastjson.annotation.JSONField(serialize = false, deserialize = false)
    public boolean isYesPlayMcpTokenConfigured() {
        return yesPlayMcpToken != null && !yesPlayMcpToken.isEmpty();
    }

    public void retainOmittedYesPlaySettings(SettingsGeneral previous) {
        if (yesPlayMode == null) yesPlayMode = previous.getYesPlayMode();
        if (yesPlayMcpPort == null) yesPlayMcpPort = previous.getYesPlayMcpPort();
        if (yesPlayMcpToken == null) yesPlayMcpToken = previous.getYesPlayMcpToken();
    }
}
