package com.widdit.nowplaying.service;

import com.widdit.nowplaying.entity.Lyric;
import com.widdit.nowplaying.service.netease.NeteaseMusicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LyricService {

    @Autowired
    private AudioService audioService;
    @Autowired
    private NeteaseMusicService neteaseMusicService;

    /**
     * 获取歌词
     * @return
     */
    public Lyric getLyric() {
        String windowTitle = audioService.getWindowTitle();

        Lyric lyric = new Lyric();
        try {
            lyric = neteaseMusicService.getLyric(windowTitle);
            // TODO 特判周杰伦情况
        } catch (Exception e) {
            log.error("获取歌词失败：" + e.getMessage());
        }

        return lyric;
    }

}
