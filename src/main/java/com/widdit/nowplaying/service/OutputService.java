package com.widdit.nowplaying.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.widdit.nowplaying.entity.Base64Img;
import com.widdit.nowplaying.entity.SettingsOutput;
import com.widdit.nowplaying.entity.Track;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class OutputService {

    private static SettingsOutput settingsOutput;

    @Autowired
    private CoverService coverService;

    /*
     * 初始化：读取输出设置文件
     */
    static {
        settingsOutput = loadSettingsOutput();
    }

    /**
     * 获取输出设置对象
     * @return
     */
    public SettingsOutput getSettingsOutput() {
        return settingsOutput;
    }

    /**
     * 在 Outputs 文件夹中输出所有歌曲信息相关的文件，具体包含以下内容：
     * - title.txt 歌名
     * - author.txt 歌手名
     * - cover.jpg 封面
     * - custom.txt 自定义输出
     * @param track 歌曲信息对象
     */
    public void output(Track track) {
        // 如果 Outputs 目录不存在，则创建
        Path outputDir = Paths.get("Outputs");
        if (!Files.exists(outputDir)) {
            try {
                Files.createDirectory(outputDir);
            } catch (Exception e) {
                log.error("创建 Outputs 目录失败: " + e.getMessage());
            }
        }

        // 将歌名写入到文件 title.txt 中
        try {
            writeText("Outputs\\title.txt", track.getTitle());
        } catch (IOException e) {
            log.error("将歌名输出到文件 title.txt 失败: " + e.getMessage());
        }

        // 将歌手名写入到文件 author.txt 中
        try {
            writeText("Outputs\\author.txt", track.getAuthor());
        } catch (IOException e) {
            log.error("将歌手名输出到文件 author.txt 失败: " + e.getMessage());
        }

        // 将封面写入到文件 cover.jpg 中
        Base64Img base64Img = coverService.convertToBase64(track.getCover());
        outputCover(base64Img);

        // 输出自定义文本到文件 custom.txt 中
        outputCustom(track);
    }

    /**
     * 更新输出设置对象
     * @return
     */
    public void updateSettingsOutput(SettingsOutput settings) {
        settingsOutput = settings;

        writeSettingsOutput(settingsOutput);

        log.info("修改输出模板成功");
    }

    /**
     * 恢复默认输出设置对象
     */
    public void resetSettingsOutput() {
        settingsOutput = new SettingsOutput();

        writeSettingsOutput(settingsOutput);

        log.info("恢复默认输出模板成功");
    }

    /**
     * 将封面写入到文件 cover.jpg 中
     * @param base64Img
     */
    private void outputCover(Base64Img base64Img) {
        String base64Str = base64Img.getBase64Img();

        // 去除 MIME 前缀
        if (base64Str.contains(",")) {
            base64Str = base64Str.substring(base64Str.indexOf(",") + 1);
        }

        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Str);

            try (FileOutputStream fos = new FileOutputStream("Outputs\\cover.jpg")) {
                fos.write(imageBytes);
            }

        } catch (IllegalArgumentException e) {
            log.error("输出封面图片文件失败（Base64 格式错误）：" + e.getMessage());
        } catch (IOException e) {
            log.error("输出封面图片文件失败：" + e.getMessage());
        }
    }

    /**
     * 输出自定义文本到文件 custom.txt 中
     * @param track
     */
    private void outputCustom(Track track) {
        String template = settingsOutput.getTemplate();

        Map<String, String> variableMap = new HashMap<>();
        variableMap.put("author", track.getAuthor());
        variableMap.put("title", track.getTitle());
        variableMap.put("album", track.getAlbum());
        variableMap.put("duration", track.getDuration().toString());
        variableMap.put("durationHuman", track.getDurationHuman());

        String author = track.getAuthor();
        variableMap.put("firstAuthor", author.contains("/") ? author.substring(0, author.indexOf("/")).trim() : author);

        Pattern pattern = Pattern.compile("\\{(\\w+)}");
        Matcher matcher = pattern.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = variableMap.containsKey(key) ? variableMap.get(key) : matcher.group(0);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement != null ? replacement : ""));
        }

        matcher.appendTail(result);
        String customText = result.toString();

        try {
            writeText("Outputs\\custom.txt", customText);
        } catch (IOException e) {
            log.error("将自定义内容输出到文件 custom.txt 失败: " + e.getMessage());
        }
    }

    /**
     * 将 text 写入到名为 filename 的文件中（覆盖写）
     * @param filename 文件名
     * @param text 文本
     * @throws IOException
     */
    private void writeText(String filename, String text) throws IOException {
        try (FileWriter writer = new FileWriter(filename, false)) {
            writer.write(text);
        }
    }

    /**
     * 加载输出设置文件
     * @return
     */
    private static SettingsOutput loadSettingsOutput() {
        SettingsOutput settingsOutput = new SettingsOutput();

        String filePath = "Settings\\settings-output.json";

        Path path = Paths.get(filePath);

        // 如果输出设置文件不存在，则创建一个默认模板的设置文件
        if (!Files.exists(path)) {
            SettingsOutput defaultSettingsOutput = new SettingsOutput();
            writeSettingsOutput(defaultSettingsOutput);
            return defaultSettingsOutput;
        }

        try {
            // 读取 JSON 文件内容
            String content = new String(Files.readAllBytes(path));

            // 解析 JSON
            JSONObject jsonObject = JSON.parseObject(content);

            // 将 JSON 数据映射到 SettingsOutput 对象
            settingsOutput = jsonObject.toJavaObject(SettingsOutput.class);

        } catch (Exception e) {
            log.error("加载 " + filePath + " 输出设置文件异常：" + e.getMessage());
        }

        return settingsOutput;
    }

    /**
     * 把输出设置对象写入本地文件
     * @param settingsOutput
     */
    private static void writeSettingsOutput(SettingsOutput settingsOutput) {
        String filePath = "Settings\\settings-output.json";

        // 将 SettingsOutput 对象转换为 JSON 字符串
        String json = JSON.toJSONString(settingsOutput, true);

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
            log.error("写入 " + filePath + " 输出设置文件异常：" + e.getMessage());
        }
    }

}
