package com.widdit.nowplaying.controller;

import com.widdit.nowplaying.annotation.ApiDebugLog;
import com.widdit.nowplaying.entity.*;
import com.widdit.nowplaying.service.SystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class SystemController {

    @Autowired
    private SystemService systemService;

    /**
     * 启用开机自启
     */
    @GetMapping("/api/system/enableRunAtStartup")
    public void enableRunAtStartup() {
        systemService.enableRunAtStartup();
    }

    /**
     * 禁用开机自启
     */
    @GetMapping("/api/system/disableRunAtStartup")
    public void disableRunAtStartup() {
        systemService.disableRunAtStartup();
    }

    /**
     * 获取系统位数
     * @return
     */
    @GetMapping("/api/system/osBit")
    public OsBit getOsBit() {
        return systemService.getOsBit();
    }

    /**
     * 获取软件信息
     * @return
     */
    @ApiDebugLog
    @GetMapping("/api/system/appInfo")
    public AppInfo getAppInfo() {
        return systemService.getAppInfo();
    }

    /**
     * 获取软件安装目录
     * @return
     */
    @GetMapping("/api/system/installPath")
    public RespData<String> getInstallPath() {
        return systemService.getInstallPath();
    }

    /**
     * 打开软件安装目录
     */
    @GetMapping("/api/system/openInstallPath")
    public void openInstallPath() {
        systemService.openInstallPath();
    }

    /**
     * 打开软件公共目录
     */
    @GetMapping("/api/system/openPublicDir")
    public void openPublicDir() {
        systemService.openPublicDir();
    }

    /**
     * 获取日志主要内容
     * @return
     */
    @ApiDebugLog
    @GetMapping("/api/system/log/mainContent")
    public RespData<String> getLogMainContent() {
        return systemService.getLogMainContent();
    }

    /**
     * 运行音频设备调试程序
     */
    @GetMapping("/api/system/runDeviceVolumeTest")
    public void runDeviceVolumeTest() {
        systemService.runDeviceVolumeTest();
    }

    /**
     * 运行 SMTC 调试程序
     */
    @GetMapping("/api/system/runSmtcTest")
    public void runSmtcTest() {
        systemService.runSmtcTest();
    }

    /**
     * 获取日志文件状态
     * @return
     */
    @GetMapping("/api/system/logStatus")
    public LogStatus getLogStatus() {
        return systemService.getLogStatus();
    }

    /**
     * 打开日志文件所在目录并定位到该文件
     * @param logType 日志文件类型（"main", "desktop", "camera"）
     */
    @GetMapping("/api/system/openLogFile")
    public void openLogFile(String logType) {
        systemService.openLogFile(logType);
    }

    /**
     * 系统预热
     */
    @ApiDebugLog
    @GetMapping("/api/system/warmUp")
    public void warmUp() {}

    /**
     * 获取局域网网络接口列表
     * @return
     */
    @ApiDebugLog
    @GetMapping("/api/system/networkInterfaces")
    public List<NetworkInterfaceInfo> getNetworkInterfaces() {
        return systemService.getNetworkInterfaces();
    }

    /**
     * 扫描给定局域网 IP 所在网段内的可达 IP 地址
     * @param localIp 本机某网卡的 IPv4 地址，例如 "192.168.1.10"
     * @return
     */
    @ApiDebugLog
    @GetMapping("/api/system/lanDevices")
    public List<String> scanLanDevices(String localIp) {
        return systemService.scanLanDevices(localIp);
    }

    /**
     * 还原 Public 目录下的 example 示例文件夹
     */
    @GetMapping("/api/system/restorePublicExample")
    public void restorePublicExample() {
        systemService.restorePublicExample();
    }

}
