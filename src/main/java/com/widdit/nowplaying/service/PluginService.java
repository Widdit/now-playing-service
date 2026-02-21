package com.widdit.nowplaying.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.widdit.nowplaying.entity.SettingsPlugin;
import com.widdit.nowplaying.entity.cmd.Args;
import com.widdit.nowplaying.entity.cmd.Option;
import com.widdit.nowplaying.entity.plugins.VirtualCamera;
import com.widdit.nowplaying.entity.plugins.WindowWidget;
import com.widdit.nowplaying.util.ConsoleUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PluginService {

    private static SettingsPlugin settingsPlugin;

    /*
     * 初始化：读取本地设置文件
     */
    static {
        settingsPlugin = loadSettings();

        runVirtualCamera();
        runWindowWidget();
    }

    /**
     * 获取虚拟摄像头设置
     * @return
     */
    public VirtualCamera getVirtualCamera() {
        return settingsPlugin.getVirtualCamera();
    }

    /**
     * 更新虚拟摄像头设置
     * @param virtualCamera
     */
    public void setVirtualCamera(VirtualCamera virtualCamera) {
        // 保存当前的安装状态（不应被外部传入的对象覆盖）
        boolean currentInstalled = settingsPlugin.getVirtualCamera().getInstalled();

        settingsPlugin.setVirtualCamera(virtualCamera);
        settingsPlugin.getVirtualCamera().setInstalled(currentInstalled);

        writeSettings(settingsPlugin);

        log.info("修改虚拟摄像头设置成功");

        runVirtualCamera();
    }

    /**
     * 获取窗口组件设置
     * @return
     */
    public WindowWidget getWindowWidget() {
        return settingsPlugin.getWindowWidget();
    }

    /**
     * 更新窗口组件设置
     * @param windowWidget
     */
    public void setWindowWidget(WindowWidget windowWidget) {
        settingsPlugin.setWindowWidget(windowWidget);

        writeSettings(settingsPlugin);

        log.info("修改窗口模式设置成功");

        runWindowWidget();
    }

    /**
     * 获取磁盘上是否有虚拟摄像头相关文件
     * @return 如果有，返回 "yes"；否则返回 "no"
     */
    public String hasVirtualCamera() {
        String path = "Plugins\\now-playing-cam\\nowplaying-cam-sender.exe";
        if (Files.exists(Paths.get(path))) {
            return "yes";
        }
        return "no";
    }

    /**
     * 加载插件设置文件
     * @return
     */
    private static SettingsPlugin loadSettings() {
        SettingsPlugin settings = new SettingsPlugin();

        String filePath = "Settings\\settings-plugin.json";

        Path path = Paths.get(filePath);

        // 如果设置文件不存在，则创建一个默认模板的设置文件
        if (!Files.exists(path)) {
            SettingsPlugin defaultSettings = new SettingsPlugin();
            writeSettings(defaultSettings);
            return defaultSettings;
        }

        try {
            // 读取 JSON 文件内容
            String content = new String(Files.readAllBytes(path));

            // 解析 JSON
            JSONObject jsonObject = JSON.parseObject(content);

            // 将 JSON 数据映射到 Settings 对象
            settings = jsonObject.toJavaObject(SettingsPlugin.class);

        } catch (Exception e) {
            log.error("加载 " + filePath + " 设置文件异常：" + e.getMessage());
        }

        return settings;
    }

    /**
     * 把插件设置对象写入本地文件
     */
    private static void writeSettings(SettingsPlugin settings) {
        String filePath = "Settings\\settings-plugin.json";

        // 将 SettingsPlugin 对象转换为 JSON 字符串
        String json = JSON.toJSONString(settings, true);

        // 如果 Settings 目录不存在，则创建
        Path settingsDir = Paths.get("Settings");
        if (!Files.exists(settingsDir)) {
            try {
                Files.createDirectory(settingsDir);
            } catch (Exception e) {
                log.error("创建 Settings 目录失败: " + e.getMessage());
            }
        }

        // 将 JSON 写入文件
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(json);
        } catch (Exception e) {
            log.error("写入 " + filePath + " 设置文件异常：" + e.getMessage());
        }
    }

    /**
     * 安装虚拟摄像头
     */
    private static void installVirtualCamera() {
        String exePath = "Plugins\\now-playing-cam\\install.exe";
        try {
            ConsoleUtil.runWaitAsAdmin(exePath);

            log.info("虚拟摄像头安装成功");

            settingsPlugin.getVirtualCamera().setInstalled(true);
            writeSettings(settingsPlugin);

        } catch (Exception e) {
            log.error("运行 " + exePath + " 失败：" + e.getMessage());
        }
    }

    /**
     * 启动虚拟摄像头
     */
    private static void runVirtualCamera() {
        // 结束之前的进程
        try {
            ConsoleUtil.kill("nowplaying-cam-sender.exe");
        } catch (Exception e) {
            log.error("结束虚拟摄像头进程失败：" + e.getMessage());
        }

        // 如果虚拟摄像头未启用，则退出
        VirtualCamera virtualCamera = settingsPlugin.getVirtualCamera();
        if (!virtualCamera.getEnabled()) {
            return;
        }

        // 执行安装虚拟摄像头脚本
        if (!virtualCamera.getInstalled()) {
            installVirtualCamera();
        }

        String currentDirectory = System.getProperty("user.dir");
        String exePath = currentDirectory + "\\Plugins\\now-playing-cam\\nowplaying-cam-sender.exe";

        // 封装命令行参数
        List<Option> options = new ArrayList<>();
        options.add(new Option("--app-width", virtualCamera.getWidth().toString()));
        options.add(new Option("--app-height", virtualCamera.getHeight().toString()));
        options.add(new Option("--app-fps", virtualCamera.getFps().toString()));
        String content = virtualCamera.getContent();
        String url = null;
        if ("song".equals(content)) {
            url = "http://localhost:9863/widget";
        } else if ("lyric".equals(content)) {
            url = "http://localhost:9863/lyric";
        }
        if (url != null) {
            options.add(new Option("--app-url", url));
        }
        Args args = new Args(options);

        // 启动虚拟摄像头进程
        try {
            ConsoleUtil.run(exePath, args);
            log.info("虚拟摄像头已启动");
        } catch (Exception e) {
            log.error("运行 " + exePath + " 失败：" + e.getMessage());
        }
    }

    /**
     * 启动窗口组件
     */
    private static void runWindowWidget() {
        // 如果未启用，则结束之前的进程
        // 对于已启用的情况，由于窗口组件程序为单例模式，如果二次运行，会自动重新运行（因此无需结束之前进程）
        WindowWidget windowWidget = settingsPlugin.getWindowWidget();
        boolean anyEnabled =
                windowWidget.getSongWindow().getEnabled() ||
                windowWidget.getLyricWindow().getEnabled() ||
                windowWidget.getCustomWindow().getEnabled();
        if (!anyEnabled) {
            try {
                ConsoleUtil.kill("np-window-widget.exe");
            } catch (Exception e) {
                log.error("结束窗口模式进程失败：" + e.getMessage());
            }
            return;
        }

        String exePath = "Plugins\\window-widget\\np-window-widget.exe";

        try {
            ConsoleUtil.run(exePath);
            log.info("窗口模式已启动");
        } catch (Exception e) {
            log.error("运行 " + exePath + " 失败：" + e.getMessage());
        }
    }

}
