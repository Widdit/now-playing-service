package com.widdit.nowplaying.controller;

import com.widdit.nowplaying.entity.Link;
import com.widdit.nowplaying.entity.profile.VersionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@RestController
@Slf4j
public class VersionController {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 获取版本信息
     * @return
     */
    @GetMapping("/version")
    public VersionInfo getVersionInfo() {
        String url = "https://gitee.com/widdit/now-playing/raw/master/version.json";

        ResponseEntity<VersionInfo> response = restTemplate.getForEntity(url, VersionInfo.class);

        return response.getBody();
    }

    /**
     * 获取 Apple Music 帮助链接
     * @return
     */
    @GetMapping("/appleMusicHelp")
    public Link getAppleMusicHelp() {
        String url = "https://gitee.com/widdit/now-playing/raw/master/apple_music_help.json";

        ResponseEntity<Link> response = restTemplate.getForEntity(url, Link.class);

        return response.getBody();
    }

}
