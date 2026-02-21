package com.widdit.nowplaying.controller;

import com.widdit.nowplaying.annotation.ApiDebugLog;
import com.widdit.nowplaying.entity.*;
import com.widdit.nowplaying.entity.dto.CoverConvertDTO;
import com.widdit.nowplaying.entity.dto.CoverVideoDTO;
import com.widdit.nowplaying.service.CoverService;
import com.widdit.nowplaying.service.NowPlayingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class NowPlayingController {

    @Autowired
    private NowPlayingService nowPlayingService;
    @Autowired
    private CoverService coverService;

    /**
     * 获取播放器和歌曲信息
     * @return
     */
    @ApiDebugLog
    @GetMapping({"/query", "/api/query"})
    public Query query() {
        return nowPlayingService.query();
    }

    /**
     * 获取播放器进度条毫秒值
     * @return
     */
    @ApiDebugLog
    @GetMapping({"/query/progress", "/api/query/progress"})
    public QueryProgress queryProgress() {
        return nowPlayingService.queryProgress();
    }

    /**
     * 获取播放器信息
     * @return
     */
    @ApiDebugLog
    @GetMapping({"/query/player", "/api/query/player"})
    public Player queryPlayer() {
        return nowPlayingService.queryPlayer();
    }

    /**
     * 获取歌曲信息
     * @return
     */
    @ApiDebugLog
    @GetMapping({"/query/track", "/api/query/track"})
    public Track queryTrack() {
        return nowPlayingService.queryTrack();
    }

    /**
     * 获取歌曲封面的 BASE64 编码
     * @param coverConvertDTO
     * @return
     */
    @ApiDebugLog
    @PostMapping({"/cover/convert", "/api/cover/convert"})
    public Base64Img convert(@RequestBody CoverConvertDTO coverConvertDTO) {
        return coverService.convertToBase64(coverConvertDTO.getCover_url());
    }

    /**
     * 获取歌曲封面的动态封面 URL
     * @param coverVideoDTO
     * @return
     */
    @ApiDebugLog
    @PostMapping({"/cover/videoUrl", "/api/cover/videoUrl"})
    public String videoUrl(@RequestBody CoverVideoDTO coverVideoDTO) {
        return coverService.getVideoUrl(coverVideoDTO.getSongTitle(), coverVideoDTO.getSongAuthor());
    }

    /**
     * 获取当前是否有歌曲
     * @return
     */
    @ApiDebugLog
    @GetMapping("/api/query/hasSong")
    public RespData<Boolean> hasSong() {
        return nowPlayingService.hasSong();
    }

    /**
     * 获取是否成功连接平台
     * @return
     */
    @ApiDebugLog
    @GetMapping("/api/query/isConnected")
    public RespData<Boolean> isConnected() {
        return nowPlayingService.isConnected();
    }

}
