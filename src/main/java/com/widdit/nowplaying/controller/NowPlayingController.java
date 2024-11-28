package com.widdit.nowplaying.controller;

import com.widdit.nowplaying.entity.*;
import com.widdit.nowplaying.service.ImageService;
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
    private ImageService imageService;

    /**
     * 获取播放器和歌曲信息
     * @return
     */
    @GetMapping("/query")
    public Query query() {
        return nowPlayingService.query();
    }

    @GetMapping("/")
    public String root() {
        return "NowPlaying Service is Running...";
    }

    /**
     * 获取歌曲封面的 BASE64 编码
     * @param coverConvertDTO
     * @return
     */
    @PostMapping("/cover/convert")
    public Base64Img convert(@RequestBody CoverConvertDTO coverConvertDTO) {
        return imageService.convertToBase64(coverConvertDTO.getCover_url());
    }

}
