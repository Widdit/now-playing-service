package com.widdit.nowplaying.controller;

import com.widdit.nowplaying.annotation.ApiDebugLog;
import com.widdit.nowplaying.entity.Lyric;
import com.widdit.nowplaying.entity.SettingsLyric;
import com.widdit.nowplaying.entity.SettingsLyricCommon;
import com.widdit.nowplaying.service.LyricService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class LyricController {

    @Autowired
    private LyricService lyricService;

    /**
     * 获取歌词
     * @return
     */
    @ApiDebugLog
    @GetMapping("/api/lyric")
    public Lyric lyric() {
        return lyricService.getLyric();
    }

    /**
     * 获取指定 ID 的歌词设置
     * @param id 配置文件 ID
     * @return
     */
    @GetMapping("/api/lyric/settings")
    public SettingsLyric lyricSettings(String id) {
        return lyricService.getSettings(id);
    }

    /**
     * 获取通用歌词设置
     * @return
     */
    @GetMapping("/api/lyric/settings/common")
    public SettingsLyricCommon commonLyricSettings() {
        return lyricService.getCommonSettings();
    }

    /**
     * 更新歌词设置
     * @param id 配置文件 ID
     * @param settingsLyric 歌词配置对象
     */
    @PutMapping("/api/lyric/settings")
    public void lyricSettings(@RequestParam String id, @RequestBody SettingsLyric settingsLyric) {
        lyricService.updateSettings(id, settingsLyric);
    }

    /**
     * 更新通用歌词设置
     * @param settingsLyricCommon 通用歌词设置对象
     */
    @PutMapping("/api/lyric/settings/common")
    public void commonLyricSettings(@RequestBody SettingsLyricCommon settingsLyricCommon) {
        lyricService.updateCommonSettings(settingsLyricCommon);
    }

}
