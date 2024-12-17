package com.widdit.nowplaying.service;

import com.widdit.nowplaying.entity.Base64Img;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Service
@Slf4j
public class CoverService {

    /**
     * 根据封面 URL 获取 BASE64 字符串
     * @param cover_url 封面 URL
     * @return
     */
    public Base64Img convertToBase64(String cover_url) {
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        Base64Img base64Img = null;

        try {
            // 获取图片输入流
            URL url = new URL(cover_url);
            inputStream = url.openStream();
            outputStream = new ByteArrayOutputStream();

            // 获取图片的 MIME 类型
            String mimeType = url.openConnection().getContentType();

            // 读取输入流并写入到字节数组输出流
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // 将输出流转换为字节数组
            byte[] imageBytes = outputStream.toByteArray();

            // 转换为 Base64 字符串
            String base64Str = Base64.getEncoder().encodeToString(imageBytes);
            String result = "data:" + mimeType + ";base64," + base64Str;
            base64Img = new Base64Img(result);

        } catch (Exception e) {
            log.error("歌曲封面转码BASE64失败，使用默认封面代替。异常信息：" + e.getMessage());
            try {
                base64Img = new Base64Img(new String(Files.readAllBytes(
                        Paths.get(ResourceUtils.getFile("classpath:no_cover_base64.txt").toURI()))));
            } catch (Exception ex) {
                log.error("默认封面BASE64加载失败：" + e.getMessage());
            }

        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception ignored) {}
        }

        return base64Img;
    }

    /**
     * 根据歌曲信息获得动态封面的 URL
     * @param songTitle 歌曲标题
     * @param songAuthor 歌手名
     * @return
     */
    public String getVideoUrl(String songTitle, String songAuthor) {
        String searchUrl = "https://music.apple.com/cn/search?term=" + songTitle + " - " + songAuthor;

        try {
            // 发送 HTTP 请求获取搜索页面内容
            Document searchPage = Jsoup.connect(searchUrl).get();

            // 找到 aria-label="歌曲" 的 div 元素
            Element songsDiv = searchPage.select("div[aria-label=歌曲]").first();

            // 找不到歌曲
            if (songsDiv == null) {
                return null;
            }

            // 遍历 data-index="0" 到 data-index="4" 的 li 元素
            for (int i = 0; i < 5; i++) {
                Element liElement = songsDiv.select("li[data-index=" + i + "]").first();
                if (liElement == null) {
                    return null;
                }

                // 找到 class 包含 track-lockup__title 的元素
                Element titleElement = liElement.select("li[class*='track-lockup__title']").first();
                if (titleElement == null) {
                    continue;
                }

                // 获取 a 标签的 href 属性值
                Element aElement = titleElement.select("a").first();
                if (aElement == null) {
                    continue;
                }

                String albumUrl = aElement.attr("href");  // 保存 href 属性
                String title = aElement.text();  // 保存 a 标签内部文本
                if ("".equals(albumUrl) || !titleMatch(songTitle, title)) {  // 歌曲标题必须匹配才算
                    continue;
                }

                // 查询是否有动态封面
                Document albumPage = Jsoup.connect(albumUrl).get();

                // 查找 amp-ambient-video 元素
                Element videoElement = albumPage.select("amp-ambient-video").first();

                // 如果存在，获取 src 属性（动态封面 URL）
                if (videoElement != null) {
                    log.info("获取动态封面成功");
                    return videoElement.attr("src");
                }
            }
        } catch (Exception e) {
            log.error("尝试获取动态封面失败：" + e.getMessage());
        }

        return null;
    }

    /**
     * 比较实际的歌曲标题和搜到的歌曲标题是否匹配
     * @param realTitle 实际的歌曲标题
     * @param searchedTitle 搜到的歌曲标题
     * @return
     */
    private boolean titleMatch(String realTitle, String searchedTitle) {
        if (realTitle == null || "".equals(realTitle) || searchedTitle == null || "".equals(searchedTitle)) {
            return false;
        }

        realTitle = realTitle.toLowerCase();
        searchedTitle = searchedTitle.toLowerCase();

        if (realTitle.equals(searchedTitle)) {
            return true;
        }

        String[] ignoredParts = {"(feat", "(ft"};

        for (String ignoredPart : ignoredParts) {
            if (realTitle.contains(ignoredPart)) {
                int index = realTitle.indexOf(ignoredPart);
                realTitle = realTitle.substring(0, index).trim();
            }

            if (searchedTitle.contains(ignoredPart)) {
                int index = searchedTitle.indexOf(ignoredPart);
                searchedTitle = searchedTitle.substring(0, index).trim();
            }
        }

        return realTitle.equals(searchedTitle);
    }

}
