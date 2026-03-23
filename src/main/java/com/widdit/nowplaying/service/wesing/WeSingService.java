package com.widdit.nowplaying.service.wesing;

import com.widdit.nowplaying.entity.Lyric;
import com.widdit.nowplaying.event.CurrentPlatformChangedEvent;
import com.widdit.nowplaying.event.SettingsGeneralChangedEvent;
import com.widdit.nowplaying.service.AudioService;
import com.widdit.nowplaying.service.LyricService;
import com.widdit.nowplaying.service.SettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.widdit.nowplaying.util.SongUtil;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class WeSingService {
    
    @Autowired
    private AudioService audioService;
    @Autowired
    private SettingsService settingsService;
    @Autowired
    @Lazy
    private LyricService lyricService;

    // 日志中提取 songmid 和歌名的模式
    private static final Pattern LOG_MID_PATTERN = Pattern.compile("\"mid\":\"([^\"]+)\"");
    private static final Pattern LOG_SONGNAME_PATTERN = Pattern.compile("\"songname\":\"([^\"]+)\"");

    // 策略 C: WatchService 监听线程
    private Thread watchThread;
    private volatile boolean watching = false;

    // 缓存条目：歌名 + 歌手 + 文件
    private static class QrcCacheEntry {
        final String title;   // QRC 中的 [ti:] 完整标题
        final String artist;  // QRC 中的 [ar:] 歌手名
        final File file;

        QrcCacheEntry(String title, String artist, File file) {
            this.title = title;
            this.artist = artist;
            this.file = file;
        }
    }

    // 歌名(小写) → 缓存条目列表（支持同名歌曲）
    private final Map<String, List<QrcCacheEntry>> titleToQrcCache = new ConcurrentHashMap<>();
    // 已扫描过的文件路径集合（避免重复解密）
    private final Set<String> scannedFiles = ConcurrentHashMap.newKeySet();

    // 上一次的 weSingCachePath，用于判断设置是否发生变化
    private volatile String lastWeSingCachePath;

    @PostConstruct
    public void init() {
        lastWeSingCachePath = normalizeCachePath(settingsService.getSettingsGeneral().getWeSingCachePath());

        if (isWeSing(audioService.getCurrentPlatform())) {
            initQrcIndexAsync();
        }
    }

    @PreDestroy
    public void destroy() {
        stopWatching();
    }

    /**
     * 获取全民 K 歌缓存目录
     */
    public String getCacheDir() {
        String basePath = settingsService.getSettingsGeneral().getWeSingCachePath();
        return basePath + "\\WeSingDL\\Res\\";
    }

    /**
     * 获取全民 K 歌日志目录（UTF-16LE 编码，每次选歌都会记录 StartKSong JSON）
     */
    public String getLogDir() {
        String basePath = settingsService.getSettingsGeneral().getWeSingCachePath();
        return basePath + "\\Log\\WeSing\\";
    }

    /**
     * 策略 B: 从本地缓存获取歌词（纯歌名索引匹配，不回退到最近修改文件）
     * @param windowTitle 当前窗口标题，用于匹配正确的歌词文件
     */
    public Lyric getLocalLyric(String windowTitle) {
        String cacheDir = getCacheDir();

        try {
            File dir = new File(cacheDir);
            if (!dir.exists() || !dir.isDirectory()) {
                log.info("全民 K 歌缓存目录不存在: {}", cacheDir);
                return null;
            }

            String[] parsed = SongUtil.parseWindowTitle(windowTitle);
            String songTitle = parsed[0];

            File matchedQrc = null;

            // 优先：从全民 K 歌日志提取 songmid，直接定位歌词文件（精确匹配，解决同名歌曲问题）
            String songMid = findSongMidFromLog(songTitle);
            if (songMid != null) {
                File directQrc = new File(cacheDir + songMid + "\\" + songMid + ".qrc");
                if (directQrc.exists()) {
                    matchedQrc = directQrc;
                    log.info("通过日志 songmid 精确定位歌词: {}", directQrc.getAbsolutePath());
                }
            }

            // 回退：按歌名索引匹配
            if (matchedQrc == null) {
                matchedQrc = findQrcByTitle(dir, songTitle);
            }

            if (matchedQrc == null) {
                log.info("未匹配到 .qrc 歌词文件，歌名: {}", songTitle);
                return null;
            }

            log.info("找到全民 K 歌本地歌词文件: {}", matchedQrc.getAbsolutePath());

            String qrcXml = WeSingHelper.decryptLocalQrc(matchedQrc);
            if (qrcXml == null || qrcXml.isEmpty()) {
                log.error("QRC 解密失败");
                return null;
            }

            // 构造歌词对象
            return WeSingHelper.buildLyric(qrcXml, songTitle);

        } catch (Exception e) {
            log.error("读取全民 K 歌本地歌词失败: {}", e.getMessage());
            return null;
        }
    }

    @EventListener
    public void handleCurrentPlatformChanged(CurrentPlatformChangedEvent event) {
        boolean oldIsWeSing = isWeSing(event.getOldPlatform());
        boolean newIsWeSing = isWeSing(event.getNewPlatform());

        // 其他值 -> wesing
        if (!oldIsWeSing && newIsWeSing) {
            log.info("当前音乐平台切换为 wesing，开始初始化 QRC 索引");
            initQrcIndexAsync();
            return;
        }

        // wesing -> 其他值
        if (oldIsWeSing && !newIsWeSing) {
            log.info("当前音乐平台从 wesing 切换为 {}，停止目录监听", event.getNewPlatform());
            stopWatching();
        }
    }

    @EventListener
    public void handleSettingsGeneralChanged(SettingsGeneralChangedEvent event) {
        String newWeSingCachePath = normalizeCachePath(settingsService.getSettingsGeneral().getWeSingCachePath());

        if (Objects.equals(lastWeSingCachePath, newWeSingCachePath)) {
            return;
        }

        lastWeSingCachePath = newWeSingCachePath;

        log.info("检测到 weSingCachePath 发生变化: {}", newWeSingCachePath);

        // 先停止旧目录监听
        stopWatching();

        // 清空旧目录建立的索引缓存，避免残留旧路径数据
        clearQrcCache();

        // 重新初始化新目录索引并启动监听
        initQrcIndexAsync();
    }

    /**
     * 异步扫描所有 .qrc 文件，解密提取歌名，建立索引
     */
    private void initQrcIndexAsync() {
        Thread cacheThread = new Thread(() -> {
            File cacheDir = new File(getCacheDir());
            if (cacheDir.exists() && cacheDir.isDirectory()) {
                log.info("扫描全民 K 歌缓存目录建立歌名索引...");
                buildTitleCache(cacheDir);
                log.info("歌名索引建立完成，共索引 {} 个歌名条目", titleToQrcCache.values().stream().mapToInt(List::size).sum());
            }
        }, "wesing-cache-builder");

        cacheThread.setDaemon(true);
        cacheThread.start();

        startWatching();
    }

    /**
     * 从全民 K 歌运行日志中提取最近一次选歌的 songmid
     * 全民 K 歌每次选歌（即使歌曲已缓存）都会在日志中记录 StartKSong JSON，包含 mid 字段
     * 日志编码为 UTF-16LE
     * @param expectedSongName 预期的歌名（从窗口标题解析），用于验证日志条目是否匹配当前播放
     * @return songmid 或 null
     */
    private String findSongMidFromLog(String expectedSongName) {
        try {
            File logDir = new File(getLogDir());
            if (!logDir.exists() || !logDir.isDirectory()) {
                return null;
            }

            // 找到最新的日志文件
            File[] logFiles = logDir.listFiles((d, name) -> name.endsWith(".log"));
            if (logFiles == null || logFiles.length == 0) {
                return null;
            }

            File latestLog = null;
            long latestMod = 0;
            for (File f : logFiles) {
                if (f.lastModified() > latestMod) {
                    latestMod = f.lastModified();
                    latestLog = f;
                }
            }
            if (latestLog == null) return null;

            // 读取日志文件末尾 ~100KB（UTF-16LE 每字符2字节，约50K字符足够找到最近的 StartKSong）
            long fileLen = latestLog.length();
            long readStart = Math.max(0, fileLen - 100 * 1024);
            // UTF-16LE: 确保从偶数位置开始读
            if (readStart % 2 != 0) readStart++;

            byte[] tailBytes;
            try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(latestLog, "r")) {
                raf.seek(readStart);
                tailBytes = new byte[(int) (fileLen - readStart)];
                raf.readFully(tailBytes);
            }

            String content = new String(tailBytes, "UTF-16LE");

            // 找最后一个 StartKSong 条目
            int lastIdx = content.lastIndexOf("\"StartKSong\"");
            if (lastIdx < 0) return null;

            // 截取从 StartKSong 到行尾
            String fromStartKSong = content.substring(lastIdx);
            int lineEnd = fromStartKSong.indexOf('\n');
            if (lineEnd > 0) {
                fromStartKSong = fromStartKSong.substring(0, lineEnd);
            }

            // 提取 mid
            Matcher midMatcher = LOG_MID_PATTERN.matcher(fromStartKSong);
            if (!midMatcher.find()) return null;
            String mid = midMatcher.group(1);

            // 验证歌名匹配当前播放（防止使用过期的日志条目）
            if (expectedSongName != null && !expectedSongName.isEmpty()) {
                Matcher nameMatcher = LOG_SONGNAME_PATTERN.matcher(fromStartKSong);
                if (nameMatcher.find()) {
                    String logSongName = nameMatcher.group(1);
                    String normalizedExpected = normalizeTitleForMatch(expectedSongName);
                    String normalizedLog = normalizeTitleForMatch(logSongName);
                    if (!normalizedExpected.equals(normalizedLog)
                            && !normalizedExpected.contains(normalizedLog)
                            && !normalizedLog.contains(normalizedExpected)) {
                        log.debug("日志歌名 [{}] 与当前窗口歌名 [{}] 不匹配，跳过日志匹配",
                                logSongName, expectedSongName);
                        return null;
                    }
                }
            }

            log.info("从全民 K 歌日志提取到 songmid: {}", mid);
            return mid;
        } catch (Exception e) {
            log.debug("解析全民 K 歌日志失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 根据窗口标题在缓存中查找最佳匹配的 .qrc 文件
     * 全民 K 歌窗口标题格式: "画你(女声版)(Live)"，QRC 中 [ti:] 可能是 "画你 （女声版）"
     * 同名歌曲通过窗口标题中的附加信息（如 Live、Cover、女声版）区分
     */
    private File findQrcByTitle(File cacheDir, String songTitle) {
        if (songTitle == null || songTitle.isEmpty()) {
            return null;
        }

        // 规范化窗口标题用于匹配
        String normalizedQuery = normalizeTitleForMatch(songTitle);

        // 缓存未命中时先补充索引
        if (titleToQrcCache.isEmpty()) {
            buildTitleCache(cacheDir);
        }

        // 1. 精确匹配（规范化后的完整标题）
        File result = findBestMatchInEntries(normalizedQuery);
        if (result != null) return result;

        // 2. 补充扫描后再试
        buildTitleCache(cacheDir);
        result = findBestMatchInEntries(normalizedQuery);
        if (result != null) return result;

        return null;
    }

    /**
     * 在所有缓存条目中找最佳匹配
     * 匹配策略：精确匹配 > 规范化匹配 > 基础名包含匹配（带消歧义评分）
     * 同分时用 lastModified 时间戳选最近使用的（全民 K 歌播放时会写入/更新缓存文件）
     */
    private File findBestMatchInEntries(String normalizedQuery) {
        // 提取基础歌名（去掉括号修饰）
        String baseQuery = extractBaseName(normalizedQuery);
        List<String> queryModifiers = extractModifiers(normalizedQuery);

        File bestMatch = null;
        int bestScore = -1;
        long bestModified = -1;

        for (Map.Entry<String, List<QrcCacheEntry>> entry : titleToQrcCache.entrySet()) {
            for (QrcCacheEntry cacheEntry : entry.getValue()) {
                if (!cacheEntry.file.exists()) continue;

                String normalizedCacheTitle = normalizeTitleForMatch(cacheEntry.title);
                String baseCacheTitle = extractBaseName(normalizedCacheTitle);

                int score = 0;

                // 精确匹配（规范化后）
                if (normalizedCacheTitle.equals(normalizedQuery)) {
                    score = 100;
                } else {
                    // 基础名必须匹配（"画你" == "画你"），否则跳过
                    if (!baseCacheTitle.equals(baseQuery) &&
                        !baseCacheTitle.contains(baseQuery) &&
                        !baseQuery.contains(baseCacheTitle)) {
                        continue;
                    }

                    // 基础名匹配，计算附加修饰的匹配度
                    score += 10; // 基础分

                    // 检查修饰词匹配（Live、女声版、Cover 等）
                    List<String> cacheModifiers = extractModifiers(normalizedCacheTitle);

                    // 修饰词完全匹配
                    if (queryModifiers.equals(cacheModifiers)) {
                        score += 50;
                    } else {
                        // 部分匹配：有多少修饰词相同
                        for (String qm : queryModifiers) {
                            for (String cm : cacheModifiers) {
                                if (qm.equals(cm) || qm.contains(cm) || cm.contains(qm)) {
                                    score += 20;
                                }
                            }
                        }
                        // 无修饰词的查询匹配无修饰词的缓存（原版歌曲）
                        if (queryModifiers.isEmpty() && cacheModifiers.isEmpty()) {
                            score += 30;
                        }
                        // 惩罚：查询无修饰但缓存有修饰（不应匹配翻唱版）
                        if (queryModifiers.isEmpty() && !cacheModifiers.isEmpty()) {
                            score -= 5;
                        }
                    }
                }

                // 同分时用缓存目录中最新文件的 lastModified 消歧
                // 全民 K 歌每次播放都会重新写入 .qrc/.pcm 等文件，最近播放的目录时间最新
                long dirModified = getLatestModifiedInDir(cacheEntry.file.getParentFile());
                if (score > bestScore || (score == bestScore && dirModified > bestModified)) {
                    bestScore = score;
                    bestMatch = cacheEntry.file;
                    bestModified = dirModified;
                }
            }
        }

        return bestMatch;
    }

    /**
     * 规范化标题用于匹配：统一括号、去空格、转小写
     * "画你 （女声版）" → "画你(女声版)"
     */
    private String normalizeTitleForMatch(String title) {
        return title.toLowerCase()
                .replace('（', '(').replace('）', ')')
                .replace('　', ' ')
                .replaceAll("\\s+", "")
                .trim();
    }

    /**
     * 提取基础歌名（去掉所有括号及其内容）
     * "画你(女声版)(live)" → "画你"
     */
    private String extractBaseName(String normalizedTitle) {
        return normalizedTitle.replaceAll("\\([^)]*\\)", "").trim();
    }

    /**
     * 提取标题中的修饰词（括号内的内容）
     * "画你(女声版)(live)" → ["女声版", "live"]
     */
    private List<String> extractModifiers(String normalizedTitle) {
        List<String> modifiers = new ArrayList<>();
        Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(normalizedTitle);
        while (m.find()) {
            modifiers.add(m.group(1));
        }
        return modifiers;
    }

    /**
     * 扫描所有 .qrc 文件，解密提取歌名和歌手，建立标题→条目列表映射缓存
     * 只扫描尚未扫描过的文件
     */
    private void buildTitleCache(File cacheDir) {
        File[] subDirs = cacheDir.listFiles(File::isDirectory);
        if (subDirs == null) return;

        for (File subDir : subDirs) {
            File[] qrcFiles = subDir.listFiles((d, name) ->
                    name.endsWith(".qrc") && !name.contains("_Roma"));
            if (qrcFiles == null) continue;

            for (File qrcFile : qrcFiles) {
                String path = qrcFile.getAbsolutePath();
                if (scannedFiles.contains(path)) continue;

                try {
                    String qrcXml = WeSingHelper.decryptLocalQrc(qrcFile);
                    if (qrcXml == null) continue; // 解密失败（可能文件未写完），不标记，下次重试

                    String[] titleAndArtist = extractTitleAndArtistFromQrc(qrcXml);
                    String title = titleAndArtist[0];
                    String artist = titleAndArtist[1];
                    if (title != null && !title.isEmpty()) {
                        String key = normalizeTitleForMatch(title);
                        QrcCacheEntry entry = new QrcCacheEntry(title, artist, qrcFile);
                        titleToQrcCache.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
                        // 同时用基础名作为 key 存一份，便于模糊查找
                        String baseKey = extractBaseName(key);
                        if (!baseKey.equals(key)) {
                            titleToQrcCache.computeIfAbsent(baseKey, k -> new ArrayList<>()).add(entry);
                        }
                    }
                    scannedFiles.add(path);
                } catch (Exception e) {
                    // 解密异常（可能文件未写完），不标记为已扫描，下次重试
                    log.debug("扫描 .qrc 文件异常，稍后重试: {}", path);
                }
            }
        }
    }

    /**
     * 从解密后的 QRC 内容中提取歌曲标题和歌手名
     * @return [title, artist]
     */
    private String[] extractTitleAndArtistFromQrc(String qrcXml) {
        String title = null;
        String artist = null;

        // 提取 [ti:xxx]
        int start = qrcXml.indexOf("[ti:");
        if (start >= 0) {
            int end = qrcXml.indexOf("]", start + 4);
            if (end > start) {
                title = qrcXml.substring(start + 4, end).trim();
            }
        }

        // 提取 [ar:xxx]
        start = qrcXml.indexOf("[ar:");
        if (start >= 0) {
            int end = qrcXml.indexOf("]", start + 4);
            if (end > start) {
                artist = qrcXml.substring(start + 4, end).trim();
            }
        }

        return new String[] {title, artist != null ? artist : ""};
    }

    /**
     * 获取目录中所有文件的最新 lastModified 时间
     * 全民 K 歌每次播放歌曲时会重新写入 .qrc/.pcm 等缓存文件
     */
    private long getLatestModifiedInDir(File dir) {
        if (dir == null || !dir.isDirectory()) return 0;
        File[] files = dir.listFiles();
        if (files == null) return 0;
        long latest = 0;
        for (File f : files) {
            long mod = f.lastModified();
            if (mod > latest) latest = mod;
        }
        return latest;
    }

    /**
     * 启动 WatchService 监听 Res 目录及其子目录
     */
    private synchronized void startWatching() {
        if (watching) {
            return;
        }

        String cacheDir = getCacheDir();

        File dir = new File(cacheDir);
        if (!dir.exists() || !dir.isDirectory()) {
            log.info("全民 K 歌缓存目录不存在，跳过 WatchService: {}", cacheDir);
            return;
        }

        watching = true;
        watchThread = new Thread(this::watchLoop, "wesing-qrc-watcher");
        watchThread.setDaemon(true);
        watchThread.start();
        log.info("启动全民 K 歌缓存目录监听: {}", cacheDir);
    }

    /**
     * 停止 WatchService 监听 Res 目录及其子目录
     */
    private synchronized void stopWatching() {
        watching = false;
        if (watchThread != null) {
            watchThread.interrupt();
            watchThread = null;
        }
    }

    /**
     * WatchService 监听循环
     * 监听 Res 目录下新子目录的创建和已有子目录中 .qrc 文件的写入
     */
    private void watchLoop() {
        String cacheDir = getCacheDir();

        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            Path resPath = Paths.get(cacheDir);
            // 注册 Res 目录本身（监听新子目录创建）
            resPath.register(watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            // 注册所有已有子目录（监听 .qrc 文件写入）
            File[] subDirs = resPath.toFile().listFiles(File::isDirectory);
            if (subDirs != null) {
                for (File subDir : subDirs) {
                    subDir.toPath().register(watcher,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_MODIFY);
                }
            }

            while (watching) {
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException e) {
                    break;
                }

                boolean qrcChanged = false;
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changed = (Path) event.context();
                    if (changed == null) continue;
                    String name = changed.toString();

                    // 新子目录创建 → 注册监听
                    Path parent = (Path) key.watchable();
                    Path fullPath = parent.resolve(changed);
                    if (fullPath.toFile().isDirectory()) {
                        try {
                            fullPath.register(watcher,
                                    StandardWatchEventKinds.ENTRY_CREATE,
                                    StandardWatchEventKinds.ENTRY_MODIFY);
                        } catch (Exception ignored) {}
                        // 新目录可能已包含 .qrc 文件（目录和文件几乎同时创建，
                        // 注册监听时 .qrc 创建事件已被错过），标记需要扫描
                        qrcChanged = true;
                    }

                    // .qrc 文件变化（排除 _Roma.qrc）
                    if (name.endsWith(".qrc") && !name.contains("_Roma")) {
                        qrcChanged = true;
                    }
                }

                key.reset();

                if (qrcChanged) {
                    // 等待歌词文件完全写入（新歌首次下载需要较长时间）
                    try { Thread.sleep(2000); } catch (InterruptedException e) { break; }

                    // 扫描并索引新增的 .qrc 文件
                    File cacheDirForScan = new File(cacheDir);
                    int sizeBefore = scannedFiles.size();
                    buildTitleCache(cacheDirForScan);
                    int sizeAfter = scannedFiles.size();
                    if (sizeAfter > sizeBefore) {
                        log.info("WatchService 检测到新歌词缓存，新增索引 {} 个文件", sizeAfter - sizeBefore);
                        lyricService.forceRefreshLyric();
                    }
                }
            }
        } catch (Exception e) {
            if (watching) {
                log.error("WatchService 监听异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 判断指定平台是否为全民 K 歌
     * @param platform 平台
     * @return
     */
    private boolean isWeSing(String platform) {
        return "wesing".equalsIgnoreCase(platform);
    }

    /**
     * 清空 QRC 索引缓存
     */
    private synchronized void clearQrcCache() {
        titleToQrcCache.clear();
        scannedFiles.clear();
    }

    /**
     * 路径规范化
     * @param path 路径
     * @return
     */
    private String normalizeCachePath(String path) {
        if (path == null) {
            return "";
        }

        String normalized = path.trim().replace('/', '\\');

        while (normalized.endsWith("\\")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

}