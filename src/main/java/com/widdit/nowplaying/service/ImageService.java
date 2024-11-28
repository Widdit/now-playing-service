package com.widdit.nowplaying.service;

import com.widdit.nowplaying.entity.Base64Img;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Service
@Slf4j
public class ImageService {

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
            log.error("歌曲封面转码BASE64失败，使用默认封面代替。异常信息：：" + e.getMessage());
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

}
