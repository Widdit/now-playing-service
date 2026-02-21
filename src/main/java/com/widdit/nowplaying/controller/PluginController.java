package com.widdit.nowplaying.controller;

import com.widdit.nowplaying.entity.plugins.VirtualCamera;
import com.widdit.nowplaying.entity.plugins.WindowWidget;
import com.widdit.nowplaying.service.PluginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PluginController {

    @Autowired
    private PluginService pluginService;

    /**
     * 获取虚拟摄像头设置
     * @return
     */
    @GetMapping("/api/settings/plugin/virtualCamera")
    public VirtualCamera getVirtualCamera() {
        return pluginService.getVirtualCamera();
    }

    /**
     * 更新虚拟摄像头设置
     * @param virtualCamera
     */
    @PutMapping("/api/settings/plugin/virtualCamera")
    public void setVirtualCamera(@RequestBody VirtualCamera virtualCamera) {
        pluginService.setVirtualCamera(virtualCamera);
    }

    /**
     * 获取磁盘上是否有虚拟摄像头相关文件
     * @return 如果有，返回 "yes"；否则返回 "no"
     */
    @GetMapping("/api/settings/plugin/hasVirtualCamera")
    public String hasVirtualCamera() {
        return pluginService.hasVirtualCamera();
    }

    /**
     * 获取窗口组件设置
     * @return
     */
    @GetMapping("/api/settings/plugin/windowWidget")
    public WindowWidget getWindowWidget() {
        return pluginService.getWindowWidget();
    }

    /**
     * 更新窗口组件设置
     * @param windowWidget
     */
    @PutMapping("/api/settings/plugin/windowWidget")
    public void setWindowWidget(@RequestBody WindowWidget windowWidget) {
        pluginService.setWindowWidget(windowWidget);
    }

}
