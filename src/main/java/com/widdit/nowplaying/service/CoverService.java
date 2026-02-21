package com.widdit.nowplaying.service;

import com.widdit.nowplaying.entity.Base64Img;
import com.widdit.nowplaying.entity.SettingsGeneral;
import com.widdit.nowplaying.event.SettingsGeneralChangedEvent;
import com.widdit.nowplaying.util.SongMatchingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CoverService {

    @Autowired
    private SettingsService settingsService;

    /** 是否应该读取本地图片 */
    private boolean shouldReadLocalImage;

    // ==================== convertToBase64 缓存相关 ====================
    /** 上一次的封面 URL */
    private String prevCoverUrl = null;
    /** 缓存的 Base64 图片结果 */
    private Base64Img cachedBase64Img = null;
    /** 是否已缓存（用于区分"未缓存"和"缓存了 null"） */
    private boolean base64ImgCached = false;
    /** convertToBase64 方法的锁对象 */
    private final Object convertToBase64Lock = new Object();

    // ==================== getVideoUrl 缓存相关 ====================
    /** 上一次的歌曲标题 */
    private String prevSongTitle = null;
    /** 上一次的歌手名 */
    private String prevSongAuthor = null;
    /** 缓存的视频 URL 结果 */
    private String cachedVideoUrl = null;
    /** 是否已缓存（用于区分"未缓存"和"缓存了 null"） */
    private boolean videoUrlCached = false;
    /** getVideoUrl 方法的锁对象 */
    private final Object getVideoUrlLock = new Object();

    /**
     * 根据封面 URL 获取 Base64 字符串
     * @param coverUrl 封面 URL
     * @return Base64Img 对象
     */
    public Base64Img convertToBase64(String coverUrl) {
        synchronized (convertToBase64Lock) {
            // 检查缓存：如果 coverUrl 与上一次相同，直接返回缓存结果
            if (base64ImgCached && Objects.equals(coverUrl, prevCoverUrl)) {
                return cachedBase64Img;
            }

            // 缓存未命中，执行实际的转换逻辑
            Base64Img result = doConvertToBase64(coverUrl);

            // 更新缓存
            prevCoverUrl = coverUrl;
            cachedBase64Img = result;
            base64ImgCached = true;

            return result;
        }
    }

    /**
     * 根据歌曲信息获得动态封面的 URL
     * @param songTitle 歌曲标题
     * @param songAuthor 歌手名
     * @return 动态封面的 URL，如果没有找到则返回 null
     */
    public String getVideoUrl(String songTitle, String songAuthor) {
        synchronized (getVideoUrlLock) {
            // 检查缓存：如果 songTitle 和 songAuthor 都与上一次相同，直接返回缓存结果
            if (videoUrlCached
                    && Objects.equals(songTitle, prevSongTitle)
                    && Objects.equals(songAuthor, prevSongAuthor)) {
                return cachedVideoUrl;
            }

            // 缓存未命中，执行实际的获取逻辑
            String result = doGetVideoUrl(songTitle, songAuthor);

            // 更新缓存
            prevSongTitle = songTitle;
            prevSongAuthor = songAuthor;
            cachedVideoUrl = result;
            videoUrlCached = true;

            return result;
        }
    }

    /**
     * 初始化操作。该方法会在该类实例被 Spring 创建时自动执行
     */
    @PostConstruct
    public void init() {
        SettingsGeneral settings = settingsService.getSettingsGeneral();
        shouldReadLocalImage = checkShouldReadLocalImage(settings);
    }

    /**
     * 监听通用设置被修改的事件
     * @param event
     */
    @EventListener
    public void handleSettingsGeneralChange(SettingsGeneralChangedEvent event) {
        SettingsGeneral settings = settingsService.getSettingsGeneral();
        shouldReadLocalImage = checkShouldReadLocalImage(settings);
    }

    /**
     * 实际执行封面转 Base64 的逻辑
     * @param coverUrl
     * @return
     */
    private Base64Img doConvertToBase64(String coverUrl) {
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        Base64Img base64Img = null;

        try {
            if (shouldReadLocalImage) {
                // 读取 C# 程序生成的本地图片文件（已经过 Base64 编码）
                String filePath = "Assets\\cover_base64.txt";
                String lockPath = "Assets\\cover_base64.lock";
                Path coverPath = Paths.get(filePath);
                Path lockFilePath = Paths.get(lockPath);

                int maxRetries = 40;
                int retryCount = 0;

                // 如果锁文件存在，则等待 100 毫秒后重试（防止读到旧封面）
                while (Files.exists(lockFilePath) && retryCount < maxRetries) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();  // 恢复中断状态
                        throw new RuntimeException("等待封面解锁时被中断", e);
                    }
                    retryCount++;
                }

                if (retryCount >= maxRetries) {
                    log.warn("封面文件锁未能及时解除，强制进行读取");
                }

                if (!Files.exists(coverPath)) {
                    throw new RuntimeException("本地文件 " + filePath + " 不存在");
                }

                String base64Str = new String(Files.readAllBytes(coverPath));
                base64Img = new Base64Img(base64Str);

            } else {
                // 获取图片输入流
                URL url = new URL(coverUrl);
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
                base64Str = "data:" + mimeType + ";base64," + base64Str;
                base64Img = new Base64Img(base64Str);
            }

        } catch (Exception e) {
            log.error("歌曲封面转码 Base64 失败，使用默认封面代替。异常信息：" + e.getMessage());
            try {
                base64Img = new Base64Img(new String(Files.readAllBytes(Paths.get("Assets\\no_cover_base64.txt"))));
            } catch (Exception ex) {
                log.error("默认封面 Base64 加载失败：" + e.getMessage());
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
     * 实际执行获取动态封面 URL 的逻辑
     * @param songTitle
     * @param songAuthor
     * @return
     */
    private String doGetVideoUrl(String songTitle, String songAuthor) {
        try {
            String keyword = songTitle + " - " + songAuthor;

            String searchUrl = UriComponentsBuilder
                    .fromHttpUrl("https://music.apple.com/cn/search")
                    .queryParam("term", keyword)
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();

            // 发送 HTTP 请求获取搜索页面内容
            Document searchPage = Jsoup.connect(searchUrl).get();

            // 找到 aria-label="歌曲" 的 div 元素
            Element songsDiv = searchPage.select("div[aria-label=歌曲]").first();

            // 找不到歌曲
            if (songsDiv == null) {
                return null;
            }

            String title;
            String author;

            // 遍历 data-index="0" 到 data-index="4" 的 li 元素（即前 5 个结果）
            for (int i = 0; i < 5; i++) {
                Element liElement = songsDiv.select("li[data-index=" + i + "]").first();
                if (liElement == null) {
                    return null;
                }

                // 歌名：li.track-lockup__title 下的 a
                Element titleLi = liElement.selectFirst("li.track-lockup__title");
                if (titleLi == null) {
                    continue;
                }

                Element titleA = titleLi.selectFirst("a[href]");
                if (titleA == null) {
                    continue;
                }

                String albumUrl = titleA.attr("href");  // 专辑链接
                title = titleA.text();  // 歌曲标题

                // 歌手名：li.track-lockup__subtitle 下的多个 a（每个 a 的文本就是歌手名）
                Element subtitleLi = liElement.selectFirst("li.track-lockup__subtitle");
                author = "";
                if (subtitleLi != null) {
                    // 取所有歌手链接
                    List<String> authors = subtitleLi.select("a").eachText();

                    // 清理空值和重复值（通常不会发生，以防万一）
                    authors = authors.stream()
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .distinct()
                            .collect(Collectors.toList());

                    author = String.join(" / ", authors);
                }

                if ("".equals(albumUrl)) {
                    continue;
                }

                // 歌曲信息必须匹配才算
                if (SongMatchingUtil.calculateSimilarity(songTitle, songAuthor, title, author) < 80) {
                    continue;
                }

                // 请求专辑详情页面
                Document albumPage = Jsoup.connect(albumUrl).get();

                // 查询是否有动态封面
                Element videoElement = albumPage.select("amp-ambient-video").first();

                // 如果存在，获取 src 属性（即动态封面 URL）
                if (videoElement != null) {
                    log.info("获取动态封面成功：{} - {}", title, author);
                    return videoElement.attr("src");
                }
            }
        } catch (Exception e) {
            log.error("尝试获取动态封面失败：" + e.getMessage());
        }

        return null;
    }

    /**
     * 判断是否应该读取本地图片
     * @param settings 通用设置对象
     * @return
     */
    private boolean checkShouldReadLocalImage(SettingsGeneral settings) {
        String platform = settings.getPlatform();
        Boolean smtc = settings.getSmtc();

        if (smtc == null) {
            return false;
        }

        switch (platform) {
            case "netease":
            case "kuwo":
                return false;

            case "ayna":
            case "soda":
            case "huahua":
            case "bq":
            case "youtube":
            case "miebo":
            case "yesplay":
                return true;

            case "qq":
            case "kugou":
            case "spotify":
            case "apple":
            case "potplayer":
            case "foobar":
            case "lx":
            case "musicfree":
            case "aimp":
            case "cider":
                return smtc;

            default:
                return false;
        }
    }

}
