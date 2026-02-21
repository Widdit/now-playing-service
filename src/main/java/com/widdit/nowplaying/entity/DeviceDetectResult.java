package com.widdit.nowplaying.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDetectResult {

    // 是否识别成功
    private Boolean success;

    // 音频设备 ID
    private String deviceId;

}
