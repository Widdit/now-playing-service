package com.widdit.nowplaying.service;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class ServerInfoService {

    @Autowired
    private RestTemplate restTemplate;

    // 缓存
    private String socialInfo = null;

    /**
     * 获取最新版本信息
     * @return
     */
    public Map getVersion() {
        String rawJsonString = restTemplate.getForObject(
                "https://gitee.com/widdit/now-playing/raw/master/version.json",
                String.class
        );
        return JSON.parseObject(rawJsonString, Map.class);
    }

    /**
     * 获取赞助列表
     * @return
     */
    public Map getSponsorList() {
        String rawJsonString = restTemplate.getForObject(
                "https://gitee.com/widdit/now-playing/raw/master/sponsor_list.json",
                String.class
        );
        return JSON.parseObject(rawJsonString, Map.class);
    }

    /**
     * 获取社交平台信息
     * @return
     */
    public Map getSocialInfo() {
        if (socialInfo == null || "".equals(socialInfo)) {
            socialInfo = restTemplate.getForObject(
                    "https://gitee.com/widdit/now-playing/raw/master/social_info.json",
                    String.class
            );
        }
        return JSON.parseObject(socialInfo, Map.class);
    }

    /**
     * 获取公告信息
     * @return
     */
    public Map getAnnouncement() {
        String rawJsonString = restTemplate.getForObject(
                "https://gitee.com/widdit/now-playing/raw/master/announcement.json",
                String.class
        );
        return JSON.parseObject(rawJsonString, Map.class);
    }

}
