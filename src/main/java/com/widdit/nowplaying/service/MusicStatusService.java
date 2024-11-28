package com.widdit.nowplaying.service;

import com.widdit.nowplaying.util.ConsoleUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MusicStatusService {

    /**
     * 获取音乐软件的播放状态（Playing, Paused, None）、音乐平台（Netease, QQ, KuGou, KuWo）、窗口标题
     * 输出格式：
     * "
     *     播放状态 音乐平台
     *     窗口标题
     * "
     * 优先级顺序：网易云 > QQ > 酷狗 > 酷我
     */
    public String getMusicStatus() {
        String stdOut = "";
        try {
            stdOut = ConsoleUtil.runGetStdOut("Assets\\GetMusicStatus\\GetMusicStatus.exe");
        } catch (Exception e) {
            log.error("获取音乐软件的播放状态失败：" + e.getMessage());
        }
        return stdOut;
    }

}
