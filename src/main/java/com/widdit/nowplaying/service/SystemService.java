package com.widdit.nowplaying.service;

import com.widdit.nowplaying.entity.*;
import com.widdit.nowplaying.util.ConsoleUtil;
import com.widdit.nowplaying.util.FileUtil;
import com.widdit.nowplaying.util.NetworkInterfaceUtil;
import com.widdit.nowplaying.util.NetworkScannerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class SystemService {

    @Autowired
    private SettingsService settingsService;
    @Autowired
    private LyricService lyricService;

    /**
     * 启用开机自启
     */
    public void enableRunAtStartup() {
        log.info("尝试启用开机自启");

        String exePath = "Assets\\EnableRunAtStartup.exe";
        try {
            ConsoleUtil.runWait(exePath);
            log.info("启用开机自启成功");
        } catch (Exception e) {
            log.error("运行 " + exePath + " 失败：" + e.getMessage());
        }
    }

    /**
     * 禁用开机自启
     */
    public void disableRunAtStartup() {
        log.info("尝试禁用开机自启");

        String exePath = "Assets\\DisableRunAtStartup.exe";
        try {
            ConsoleUtil.runWait(exePath);
            log.info("禁用开机自启成功");
        } catch (Exception e) {
            log.error("运行 " + exePath + " 失败：" + e.getMessage());
        }
    }

    /**
     * 获取系统位数
     * @return
     */
    public OsBit getOsBit() {
        // 获取系统环境变量
        String processorArch = System.getenv("PROCESSOR_ARCHITECTURE");
        String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

        // 检查 64 位系统的特征
        if (processorArch != null && processorArch.contains("64")) {
            return new OsBit(64);
        }
        // 检查 32 位 JVM 运行在 64 位系统上的情况
        if (wow64Arch != null && wow64Arch.contains("64")) {
            return new OsBit(64);
        }

        // 以上都不满足则为 32 位系统
        return new OsBit(32);
    }

    /**
     * 获取软件信息
     * @return
     */
    public AppInfo getAppInfo() {
        // 操作系统名称
        String osName = System.getProperty("os.name");

        // 系统位数
        OsBit osBit = getOsBit();

        // 软件安装目录
        String installPath = System.getProperty("user.dir");

        // 程序运行时长（HH:mm:ss）
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long uptimeMillis = runtimeMXBean.getUptime();
        Duration duration = Duration.ofMillis(uptimeMillis);

        long hours = duration.toHours();
        long minutes = (duration.toMinutes() % 60);
        long seconds = (duration.getSeconds() % 60);

        String runningTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        // 通用设置
        SettingsGeneral settingsGeneral = settingsService.getSettingsGeneral();

        // 歌词设置
        SettingsLyricCommon settingsLyricCommon = lyricService.getCommonSettings();

        return AppInfo.builder()
                .operatingSystem(osName)
                .osBit(osBit.getOsBit())
                .installPath(installPath)
                .programRunningTime(runningTime)
                .deviceId(settingsGeneral.getDeviceId())
                .deviceName(settingsGeneral.getDeviceName())
                .platform(settingsGeneral.getPlatform())
                .smtc(settingsGeneral.getSmtc())
                .runAtStartup(settingsGeneral.getRunAtStartup())
                .autoLaunchHomePage(settingsGeneral.getAutoLaunchHomePage())
                .updateCheckFreq(settingsGeneral.getUpdateCheckFreq())
                .fallbackPlatformEnabled(settingsGeneral.getFallbackPlatformEnabled())
                .fallbackPlatform(settingsGeneral.getFallbackPlatform())
                .pollInterval(settingsGeneral.getPollInterval())
                .lyricSource(settingsLyricCommon.getLyricSource())
                .autoSelectBestLyric(settingsLyricCommon.getAutoSelectBestLyric())
                .build();
    }

    /**
     * 获取软件安装目录
     * @return
     */
    public RespData<String> getInstallPath() {
        return new RespData<>(System.getProperty("user.dir"));
    }

    /**
     * 打开软件安装目录
     */
    public void openInstallPath() {
        String currentDir = System.getProperty("user.dir");

        try {
            Runtime.getRuntime().exec(new String[]{"explorer", currentDir});
        } catch (Exception e) {
            log.error("打开软件安装目录失败：{}", e.getMessage());
        }
    }

    /**
     * 打开软件公共目录
     */
    public void openPublicDir() {
        Path publicFolder = Paths.get(System.getProperty("user.dir"), "Public")
                .toAbsolutePath()
                .normalize();

        // 如果目录不存在，自动创建
        if (!Files.exists(publicFolder)) {
            try {
                Files.createDirectories(publicFolder);
            } catch (Exception e) {
                log.error("无法创建 Public 目录: {}", publicFolder, e);
            }
        }

        try {
            Runtime.getRuntime().exec(new String[]{"explorer", publicFolder.toString()});
        } catch (Exception e) {
            log.error("打开软件公共目录失败：{}", e.getMessage());
        }
    }

    /**
     * 获取日志主要内容
     * @return
     */
    public RespData<String> getLogMainContent() {
        String logLines = "";
        try {
            List<String> lines = Files.readAllLines(Paths.get("now-playing.log"), StandardCharsets.UTF_8);

            StringBuilder sb = new StringBuilder();
            String lineSeparator = System.lineSeparator();

            for (int i = 0; i < lines.size(); i++) {
                sb.append(lines.get(i));
                if (i != lines.size() - 1) {
                    sb.append(lineSeparator);
                }
            }

            logLines = sb.toString();

        } catch (IOException e) {
            log.error("获取日志信息失败：" + e.getMessage());
        }

        // 只保留日志文件的首尾指定行数
        logLines = truncateLines(logLines, 30, 40, 1);

        return new RespData<>(logLines);
    }

    /**
     * 运行音频设备调试程序
     */
    public void runDeviceVolumeTest() {
        try {
            ConsoleUtil.kill("DeviceVolumeTest.exe");
        } catch (Exception e) {
            log.error("结束音频设备调试程序失败：" + e.getMessage());
        }

        try {
            ConsoleUtil.runWithConsole("Assets\\AudioService\\DeviceVolumeTest.exe");
        } catch (Exception e) {
            log.error("运行音频设备调试程序失败：" + e.getMessage());
        }
    }

    /**
     * 运行 SMTC 调试程序
     */
    public void runSmtcTest() {
        try {
            ConsoleUtil.kill("SMTCTest.exe");
        } catch (Exception e) {
            log.error("结束 SMTC 调试程序失败：" + e.getMessage());
        }

        try {
            ConsoleUtil.runWithConsole("Assets\\AudioService\\SMTCTest.exe");
        } catch (Exception e) {
            log.error("运行 SMTC 调试程序失败：" + e.getMessage());
        }
    }

    /**
     * 获取日志文件状态
     * @return
     */
    public LogStatus getLogStatus() {
        boolean mainLogExist = new File("now-playing.log").exists();
        boolean desktopLogExist = new File("now-playing-desktop.log").exists();
        boolean virtualCameraLogExist = new File("Plugins\\now-playing-cam\\cef_debug.log").exists();

        return LogStatus.builder()
                .mainLogExist(mainLogExist)
                .desktopLogExist(desktopLogExist)
                .virtualCameraLogExist(virtualCameraLogExist)
                .build();
    }

    /**
     * 打开日志文件所在目录并定位到该文件
     * @param logType 日志文件类型（"main", "desktop", "camera"）
     */
    public void openLogFile(String logType) {
        String path;
        switch (logType) {
            case "main":
                path = "now-playing.log";
                break;
            case "desktop":
                path = "now-playing-desktop.log";
                break;
            case "camera":
                path = "Plugins\\now-playing-cam\\cef_debug.log";
                break;
            default:
                path = "now-playing.log";
        }

        try {
            File file = new File(path);
            if (file.exists()) {
                // 获取文件的绝对路径
                String absolutePath = file.getAbsolutePath();
                // 使用 explorer /select 命令在资源管理器中定位到文件
                Runtime.getRuntime().exec(new String[]{
                        "explorer",
                        "/select,",
                        absolutePath
                });
            } else {
                throw new RuntimeException("文件不存在: " + path);
            }
        } catch (Exception e) {
            log.error("打开日志文件失败：" + e.getMessage());
        }
    }

    /**
     * 获取局域网网络接口列表（已按照 "真实局域网 IP" 的可能性从高到低排序）
     * @return
     */
    public List<NetworkInterfaceInfo> getNetworkInterfaces() {
        return NetworkInterfaceUtil.getNetworkInterfaceList();
    }

    /**
     * 扫描给定局域网 IP 所在网段内的可达 IP 地址
     * @param localIp 本机某网卡的 IPv4 地址，例如 "192.168.1.10"
     * @return 可达 IP 列表，本机 IP 排在第一个
     */
    public List<String> scanLanDevices(String localIp) {
        return NetworkScannerUtil.scanLanDevices(localIp);
    }

    /**
     * 还原 Public 目录下的 example 示例文件夹
     * 将 Assets\PublicExample 文件夹内的所有内容复制到 Public\example 文件夹中
     */
    public void restorePublicExample() {
        // ========== 1. 获取并验证基础路径 ==========
        String userDir = System.getProperty("user.dir");
        if (userDir == null || userDir.trim().isEmpty()) {
            log.error("无法获取当前工作目录 (user.dir)");
            return;
        }

        Path assetsDir = Paths.get(userDir, "Assets");
        Path publicDir = Paths.get(userDir, "Public");
        Path sourceDir = assetsDir.resolve("PublicExample");
        Path targetDir = publicDir.resolve("example");

        log.info("开始还原 example 示例文件夹...");

        // ========== 2. 检查源目录 PublicExample ==========
        if (!Files.exists(sourceDir)) {
            log.error("Assets 目录下的 PublicExample 文件夹不存在: {}", sourceDir);
            return;
        }

        if (!Files.isDirectory(sourceDir)) {
            log.error("PublicExample 存在但不是文件夹，而是文件: {}", sourceDir);
            return;
        }

        if (!FileUtil.isDirectoryReadable(sourceDir)) {
            log.error("PublicExample 文件夹不可读，请检查权限: {}", sourceDir);
            return;
        }

        try {
            // ========== 3. 确保Public目录存在 ==========
            if (Files.exists(publicDir) && !Files.isDirectory(publicDir)) {
                log.error("Public 路径存在但不是目录: {}", publicDir);
                return;
            }
            FileUtil.ensureDirectoryExists(publicDir);

            // ========== 4. 处理目标目录example ==========
            prepareTargetDirectory(targetDir);

            // ========== 5. 复制PublicExample内容到example ==========
            log.info("开始复制文件...");
            int[] result = FileUtil.copyDirectory(sourceDir, targetDir);

            log.info("还原 example 示例文件夹完成。共复制 {} 个目录，{} 个文件", result[1], result[0]);

        } catch (AccessDeniedException e) {
            log.error("权限不足，无法访问或操作文件: {}", e.getFile(), e);
        } catch (FileSystemException e) {
            log.error("文件系统错误（可能文件被占用或磁盘空间不足）: {}", e.getMessage(), e);
        } catch (IOException e) {
            log.error("还原 example 示例文件夹时发生 IO 错误", e);
        } catch (SecurityException e) {
            log.error("安全管理器阻止了文件操作", e);
        } catch (Exception e) {
            log.error("还原 example 示例文件夹时发生未知错误", e);
        }
    }

    /**
     * 准备目标目录：确保目标目录存在且为空
     *
     * @param targetDir 目标目录
     * @throws IOException 如果操作失败
     */
    private void prepareTargetDirectory(Path targetDir) throws IOException {
        if (Files.exists(targetDir)) {
            if (!Files.isDirectory(targetDir)) {
                // example 存在但是是个文件，需要删除后重建为目录
                log.warn("example 存在但是是文件而非目录，将删除并重建为目录");
                Files.delete(targetDir);
                Files.createDirectories(targetDir);
            } else {
                // example 是目录，清空其中的内容
                if (!FileUtil.isDirectoryEmpty(targetDir)) {
                    log.info("example 文件夹已存在且非空，正在清空内容...");
                    FileUtil.clearDirectory(targetDir);
                }
            }
        } else {
            // example 不存在，创建目录
            Files.createDirectories(targetDir);
            log.info("example 文件夹不存在，已创建: {}", targetDir);
        }
    }

    /**
     * 截取多行文本（仅保留首尾指定行数）
     * @param str 多行文本
     * @param head 首行数
     * @param tail 尾行数
     * @param startLineIndex 开始行索引
     * @return
     */
    private String truncateLines(String str, int head, int tail, int startLineIndex) {
        if (str == null || str.isEmpty()) {
            return "";
        }

        // 按行拆分
        String[] lines = str.split("\\r?\\n", -1);

        // 如果 startLineIndex 越界，返回空
        if (startLineIndex >= lines.length) {
            return "";
        }

        // 从 startLineIndex 开始截取
        String[] subLines = new String[lines.length - startLineIndex];
        System.arraycopy(lines, startLineIndex, subLines, 0, subLines.length);

        int total = subLines.length;

        // 如果行数 <= head + tail，直接返回
        if (total <= head + tail) {
            return String.join("\n", subLines);
        }

        // 需要省略的行数
        int omitted = total - head - tail;

        StringBuilder sb = new StringBuilder();

        // 前 head 行
        for (int i = 0; i < head; i++) {
            sb.append(subLines[i]).append("\n");
        }

        // 省略提示行
        sb.append("[ ... 此处省略 ").append(omitted).append(" 行 ... ]\n");

        // 后 tail 行
        for (int i = total - tail; i < total; i++) {
            sb.append(subLines[i]).append("\n");
        }

        // 去掉最后一个换行符
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

}
