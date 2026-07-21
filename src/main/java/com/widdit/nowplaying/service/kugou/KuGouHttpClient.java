package com.widdit.nowplaying.service.kugou;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
public class KuGouHttpClient {
    public String get(String url) throws IOException {
        String host = new URL(url).getHost();
        Connection.Response response = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .header("Accept", "*/*")
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .header("Host", host)
                .header("Accept-Language", "zh-CN,en-US;q=0.7,en;q=0.3")
                .header("Pragma", "no-cache")
                .ignoreContentType(true)
                .timeout(10000)
                .method(Connection.Method.GET)
                .execute();
        return response.body();
    }
}
