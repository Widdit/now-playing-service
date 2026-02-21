package com.widdit.nowplaying.controller;

import com.widdit.nowplaying.service.SystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@Slf4j
public class PublicResourceController {

    @Autowired
    private SystemService systemService;

    private final Path publicFolder;

    public PublicResourceController() {
        this.publicFolder = Paths.get(System.getProperty("user.dir"), "Public")
                .toAbsolutePath()
                .normalize();

        // 如果目录不存在，自动创建
        if (!Files.exists(publicFolder)) {
            try {
                Files.createDirectories(publicFolder);
            } catch (Exception e) {
                log.error("无法创建 Public 目录: {}", publicFolder, e);
            }
        }
    }

    @GetMapping("/public/**")
    public ResponseEntity<Resource> servePublicResource(HttpServletRequest request) {
        // 获取请求路径，去掉 /public/ 前缀
        String path = request.getRequestURI().substring("/public/".length());

        // 安全检查：防止路径遍历攻击
        if (path.contains("..")) {
            return ResponseEntity.badRequest().build();
        }

        Path filePath = publicFolder.resolve(path).normalize();

        // 确保文件在 Public 目录内
        if (!filePath.startsWith(publicFolder)) {
            return ResponseEntity.badRequest().build();
        }

        // 检查文件是否存在
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            // 判断路径是否为示例页面
            if ("/public/example/index.html".equals(request.getRequestURI())) {
                // 执行还原方法
                systemService.restorePublicExample();
                // 等待 1000 ms
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                // 还原完成后重新检查文件是否存在并返回
                if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                    Resource resource = new FileSystemResource(filePath);
                    MediaType mediaType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
                    return ResponseEntity.ok()
                            .contentType(mediaType)
                            .body(resource);
                }
            }
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(filePath);

        // 自动检测 Content-Type
        MediaType mediaType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }

}
