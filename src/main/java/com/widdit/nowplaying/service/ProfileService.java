package com.widdit.nowplaying.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.widdit.nowplaying.entity.Settings;
import com.widdit.nowplaying.entity.SettingsWrapper;
import com.widdit.nowplaying.entity.profile.Amuse;
import com.widdit.nowplaying.entity.profile.Profile;
import com.widdit.nowplaying.entity.profile.ProfileWrapper;
import com.widdit.nowplaying.entity.profile.Widget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@Slf4j
public class ProfileService {

    private static Settings settingsMain;

    private static Map<String, Settings> settingsMap = new HashMap<>();

    private static List<String> profileIds = Arrays.asList("profileA", "profileB", "profileC", "profileD");

    /*
     * 初始化：读取本地配置文件
     */
    static {
        settingsMain = loadSettings("main");

        for (String profileId : profileIds) {
            Settings settings = loadSettings(profileId);
            settingsMap.put(profileId, settings);
        }
    }

    /**
     * 根据 ID 获取配置文件
     * @param id 配置文件 ID
     * @return
     */
    public SettingsWrapper getSettings(String id) {
        if (id == null || "".equals(id) || "Main".equalsIgnoreCase(id)) {
            return new SettingsWrapper(settingsMain);
        }

        if (!settingsMap.containsKey(id)) {
            return new SettingsWrapper(new Settings());
        }

        return new SettingsWrapper(settingsMap.get(id));
    }

    /**
     * 返回设置页面的所有配置信息
     * @return
     */
    public ProfileWrapper usersProfile() {
        List<Profile> profiles = new ArrayList<>();

        for (String profileId : profileIds) {
            Profile profile = Profile.builder()
                    .profile_name(profileId.replace("profile", "配置文件"))
                    .profile_settings(settingsMap.get(profileId))
                    ._id(profileId)
                    .build();

            profiles.add(profile);
        }

        Amuse amuse = new Amuse();
        amuse.setProfiles(profiles);

        Widget widget = new Widget();
        widget.setAmuse(amuse);

        ProfileWrapper profileWrapper = new ProfileWrapper();
        profileWrapper.setWidgets(widget);

        return profileWrapper;
    }

    /**
     * 更新设置（布尔类型属性），并写到本地文件
     * @param profileId 配置文件 ID
     * @param propName 属性名
     * @param value 属性值
     */
    public String settingsUpdateBoolean(String profileId, String propName, Boolean value) {
        // 确定要操作的配置文件对象
        Settings settings;
        String profileIdHuman = profileId;
        if (profileId == null || "".equals(profileId) || "Main".equalsIgnoreCase(profileId)) {
            settings = settingsMain;
            profileIdHuman = "Main";
        } else if (!settingsMap.containsKey(profileId)) {
            return "Profile does not exist";
        } else {
            settings = settingsMap.get(profileId);
        }

        // 更新属性值
        if ("cover_blur".equals(propName)) {
            settings.setCover_blur(value);
        } else if ("cover_glow".equals(propName)) {
            settings.setCover_glow(value);
        } else if ("hide_on_pause".equals(propName)) {
            settings.setHide_on_pause(value);
        } else if ("song_change_only".equals(propName)) {
            settings.setSong_change_only(value);
        } else if ("player_colors".equals(propName)) {
            settings.setPlayer_colors(value);
        } else if ("hide_equalizer".equals(propName)) {
            settings.setHide_equalizer(value);
        }

        // 写入到本地文件
        writeSettings(profileId, settings);
        log.info("成功将配置文件 " + profileIdHuman + " 的属性 " + propName + " 修改为 " + value);

        return "ok";
    }

    /**
     * 更新设置（整数类型属性），并写到本地文件
     * @param profileId 配置文件 ID
     * @param propName 属性名
     * @param value 属性值
     */
    public String settingsUpdateInteger(String profileId, String propName, Integer value) {
        // 确定要操作的配置文件对象
        Settings settings;
        String profileIdHuman = profileId;
        if (profileId == null || "".equals(profileId) || "Main".equalsIgnoreCase(profileId)) {
            settings = settingsMain;
            profileIdHuman = "Main";
        } else if (!settingsMap.containsKey(profileId)) {
            return "Profile does not exist";
        } else {
            settings = settingsMap.get(profileId);
        }

        // 更新属性值
        if ("player".equals(propName)) {
            settings.setPlayer(value);
        } else if ("cover".equals(propName)) {
            settings.setCover(value);
        } else if ("theme".equals(propName)) {
            settings.setTheme(value);
        } else if ("hide_delay".equals(propName)) {
            settings.setHide_delay(value);
        } else if ("visible_duration".equals(propName)) {
            settings.setVisible_duration(value);
        }

        // 写入到本地文件
        writeSettings(profileId, settings);
        log.info("成功将配置文件 " + profileIdHuman + " 的属性 " + propName + " 修改为 " + value);

        return "ok";
    }

    /**
     * 更新设置（字符串类型属性），并写到本地文件
     * @param profileId 配置文件 ID
     * @param propName 属性名
     * @param value 属性值
     */
    public String settingsUpdateString(String profileId, String propName, String value) {
        // 确定要操作的配置文件对象
        Settings settings;
        String profileIdHuman = profileId;
        if (profileId == null || "".equals(profileId) || "Main".equalsIgnoreCase(profileId)) {
            settings = settingsMain;
            profileIdHuman = "Main";
        } else if (!settingsMap.containsKey(profileId)) {
            return "Profile does not exist";
        } else {
            settings = settingsMap.get(profileId);
        }

        // 更新属性值
        if ("tint_color".equals(propName)) {
            settings.setTint_color(value);
        }

        // 写入到本地文件
        writeSettings(profileId, settings);
        log.info("成功将配置文件 " + profileIdHuman + " 的属性 " + propName + " 修改为 " + value);

        return "ok";
    }

    /**
     * 根据 ID 加载配置文件
     * @param id 配置文件 ID
     * @return
     */
    private static Settings loadSettings(String id) {
        Settings settings = new Settings();

        String filePath = "Settings\\settings-" + id + ".json";

        Path path = Paths.get(filePath);

        // 如果配置文件不存在，则创建一个默认模板的配置文件
        if (!Files.exists(path)) {
            Settings defaultSettings = new Settings();
            writeSettings(id, defaultSettings);
            return defaultSettings;
        }

        try {
            // 读取 JSON 文件内容
            String content = new String(Files.readAllBytes(path));

            // 解析 JSON
            JSONObject jsonObject = JSON.parseObject(content);
            JSONObject settingsObject = jsonObject.getJSONObject("settings");

            // 将 JSON 数据映射到 Settings 对象
            settings = settingsObject.toJavaObject(Settings.class);

        } catch (Exception e) {
            log.error("加载 " + filePath + " 配置文件异常：" + e.getMessage());
        }

        return settings;
    }

    /**
     * 把配置文件对象写入本地文件
     * @param id 配置文件 ID
     * @param settings 目标配置文件对象
     */
    private static void writeSettings(String id, Settings settings) {
        // 指定文件路径
        if (id == null || "".equals(id) || "Main".equalsIgnoreCase(id)) {
            id = "main";
        }
        String filePath = "Settings\\settings-" + id + ".json";

        // 将 SettingsWrapper 对象转换为 JSON 字符串
        SettingsWrapper settingsWrapper = new SettingsWrapper(settings);
        String json = JSON.toJSONString(settingsWrapper, true);

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
            log.error("写入 " + filePath + " 配置文件异常：" + e.getMessage());
        }
    }

}
