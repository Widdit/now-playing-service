package com.widdit.nowplaying.entity;

import com.widdit.nowplaying.entity.plugins.VirtualCamera;
import com.widdit.nowplaying.entity.plugins.WindowWidget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsPlugin {

    // 虚拟摄像头
    private VirtualCamera virtualCamera = new VirtualCamera();

    // 窗口组件
    private WindowWidget windowWidget = new WindowWidget();

}
