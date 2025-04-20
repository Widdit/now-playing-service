package com.widdit.nowplaying.controller;

import com.widdit.nowplaying.entity.Lyric;
import com.widdit.nowplaying.service.LyricService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class LyricController {

    @Autowired
    private LyricService lyricService;

    /**
     * 获取歌词
     * @return
     */
    @GetMapping("/api/lyric")
    public Lyric lyric() {
        return lyricService.getLyric();
    }

}
