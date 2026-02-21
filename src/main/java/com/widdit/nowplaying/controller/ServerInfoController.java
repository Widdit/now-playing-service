package com.widdit.nowplaying.controller;

import com.widdit.nowplaying.annotation.ApiDebugLog;
import com.widdit.nowplaying.service.ServerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class ServerInfoController {

    @Autowired
    private ServerInfoService serverInfoService;

    /**
     * 获取最新版本信息
     * @return
     */
    @ApiDebugLog
    @GetMapping(value = "/api/version", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map getVersion() {
        return serverInfoService.getVersion();
    }

    /**
     * 获取赞助列表
     * @return
     */
    @GetMapping(value = "/api/sponsorList", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map getSponsorList() {
        return serverInfoService.getSponsorList();
    }

    /**
     * 获取社交平台信息
     * @return
     */
    @GetMapping(value = "/api/socialInfo", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map getSocialInfo() {
        return serverInfoService.getSocialInfo();
    }

    /**
     * 获取公告信息
     * @return
     */
    @GetMapping(value = "/api/announcement", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map getAnnouncement() {
        return serverInfoService.getAnnouncement();
    }

}
