package com.widdit.nowplaying.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.widdit.nowplaying.entity.SettingsGeneral;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class SettingsService {

    private static SettingsGeneral settingsGeneral;

    /*
     * 初始化：读取本地设置文件
     */
    static {
        settingsGeneral = loadSettings();
    }

    /**
     * 获取通用设置对象
     * @return
     */
    public SettingsGeneral getSettingsGeneral() {
        return settingsGeneral;
    }

    /**
     * 更新通用设置对象
     * @return
     */
    public void updateSettingsGeneral(SettingsGeneral settings) {
        settingsGeneral = settings;

        writeSettings(settingsGeneral);

        log.info("修改通用设置成功");
    }

    /**
     * 加载通用设置文件
     * @return
     */
    private static SettingsGeneral loadSettings() {
        SettingsGeneral settings = new SettingsGeneral();

        String filePath = "Settings\\settings.json";

        Path path = Paths.get(filePath);

        // 如果设置文件不存在，则创建一个默认模板的设置文件
        if (!Files.exists(path)) {
            SettingsGeneral defaultSettings = new SettingsGeneral();
            writeSettings(defaultSettings);
            return defaultSettings;
        }

        try {
            // 读取 JSON 文件内容
            String content = new String(Files.readAllBytes(path));

            // 解析 JSON
            JSONObject jsonObject = JSON.parseObject(content);

            // 将 JSON 数据映射到 Settings 对象
            settings = jsonObject.toJavaObject(SettingsGeneral.class);
        } catch (Exception e) {
            log.error("加载 " + filePath + " 设置文件异常：" + e.getMessage());
        }

        return settings;
    }

    /**
     * 把通用设置对象写入本地文件
     */
    private static void writeSettings(SettingsGeneral settings) {
        String filePath = "Settings\\settings.json";

        // 将 SettingsGeneral 对象转换为 JSON 字符串
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

}
