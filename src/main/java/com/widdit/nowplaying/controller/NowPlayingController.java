package com.widdit.nowplaying.controller;

import com.widdit.nowplaying.entity.*;
import com.widdit.nowplaying.service.CoverService;
import com.widdit.nowplaying.service.NowPlayingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
     * @throws IOException
     */
    @GetMapping("/query")
    public Query query() throws IOException {
        return nowPlayingService.query();
    }

    /**
     * 获取歌曲封面的 BASE64 编码
     * @param coverConvertDTO
     * @return
     */
    @PostMapping("/cover/convert")
    public Base64Img convert(@RequestBody CoverConvertDTO coverConvertDTO) {
        return coverService.convertToBase64(coverConvertDTO.getCover_url());
    }

    /**
     * 获取歌曲封面的动态封面 URL
     * @param coverVideoDTO
     * @return
     */
    @PostMapping("/cover/videoUrl")
    public String videoUrl(@RequestBody CoverVideoDTO coverVideoDTO) {
        return coverService.getVideoUrl(coverVideoDTO.getSongTitle(), coverVideoDTO.getSongAuthor());
    }

}
