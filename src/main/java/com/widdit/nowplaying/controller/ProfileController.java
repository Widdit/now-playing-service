package com.widdit.nowplaying.controller;

import com.widdit.nowplaying.entity.SettingsWrapper;
import com.widdit.nowplaying.entity.profile.*;
import com.widdit.nowplaying.service.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    /**
     * 身份验证
     * @return
     */
    @GetMapping("/auth/check")
    public Message authCheck() {
        return new Message();
    }

    /**
     * 返回设置页面的所有配置信息
     * @return
     */
    @GetMapping("/users/profile")
    public ProfileWrapper usersProfile() {
        return profileService.usersProfile();
    }

    /**
     * 根据配置文件 ID 获取设置
     * @return
     */
    @GetMapping("/widgets/amuse/settings")
    public SettingsWrapper settings(String id) {
        return profileService.getSettings(id);
    }

    /**
     * 根据配置文件 ID 获取设置（路径参数）
     * @param id
     * @return
     */
    @GetMapping("/widgets/amuse/settings/{id}")
    public SettingsWrapper settingsPathVar(@PathVariable String id) {
        return profileService.getSettings(id);
    }

    @PostMapping("/widgets/amuse/settings/music-service")
    public String musicService() {
        return "Successfully set music service";
    }

    /**
     * 更新设置（布尔类型）
     * @param profileId 配置文件 ID
     * @param propName 属性名
     * @param value 属性值
     */
    @PostMapping("/widgets/amuse/settings/update/boolean")
    public String settingsUpdateBoolean(@RequestParam String profileId, @RequestParam String propName, @RequestParam Boolean value) {
        return profileService.settingsUpdateBoolean(profileId, propName, value);
    }

    /**
     * 更新设置（整数类型）
     * @param profileId 配置文件 ID
     * @param propName 属性名
     * @param value 属性值
     */
    @PostMapping("/widgets/amuse/settings/update/integer")
    public String settingsUpdateInteger(@RequestParam String profileId, @RequestParam String propName, @RequestParam Integer value) {
        return profileService.settingsUpdateInteger(profileId, propName, value);
    }

    /**
     * 更新设置（字符串类型）
     * @param profileId 配置文件 ID
     * @param propName 属性名
     * @param value 属性值
     */
    @PostMapping("/widgets/amuse/settings/update/string")
    public String settingsUpdateString(@RequestParam String profileId, @RequestParam String propName, @RequestParam String value) {
        return profileService.settingsUpdateString(profileId, propName, value);
    }

}
