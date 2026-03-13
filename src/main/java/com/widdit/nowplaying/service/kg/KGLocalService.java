package com.widdit.nowplaying.service.kg;

import com.widdit.nowplaying.entity.Lyric;
import com.widdit.nowplaying.service.LyricService;
import com.widdit.nowplaying.service.qq.Decrypter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.widdit.nowplaying.util.SongUtil;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.nio.file.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class KGLocalService {

    // 全民K歌本地缓存目录
    private static final String CACHE_DIR = System.getenv("APPDATA")
            + "\\Tencent\\WeSing\\WeSingCache\\WeSingDL\\Res\\";

    private static final int QRC_HEADER_LENGTH = 11;
    // QRC 逐字歌词的时间格式: [startMs,durationMs]
    private static final Pattern QRC_TIME_PATTERN = Pattern.compile("^\\[(\\d+),(\\d+)]");
    // 标准 LRC 时间格式: [MM:SS.ms]
    private static final Pattern LRC_TIME_PATTERN = Pattern.compile("\\[(\\d{2}):(\\d{2})[.](\\d{2,3})]");

    // 全民K歌运行日志目录（UTF-16LE 编码，每次选歌都会记录 StartKSong JSON）
    private static final String LOG_DIR = System.getenv("APPDATA")
            + "\\Tencent\\WeSing\\WeSingCache\\Log\\WeSing\\";
    // 日志中提取 songmid 和歌名的模式
    private static final Pattern LOG_MID_PATTERN = Pattern.compile("\"mid\":\"([^\"]+)\"");
    private static final Pattern LOG_SONGNAME_PATTERN = Pattern.compile("\"songname\":\"([^\"]+)\"");

    @Autowired
    @Lazy
    private LyricService lyricService;

    // 策略 C: WatchService 监听线程
    private Thread watchThread;
    private volatile boolean watching = false;
    // 上一次检测到的最新 .qrc 文件路径（用于去重，避免重复触发）
    private volatile String lastQrcPath = "";

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

    @PostConstruct
    public void init() {
        // 策略 B: 异步扫描所有 .qrc 文件，解密提取歌名，建立索引（避免阻塞 Spring 容器启动）
        Thread cacheThread = new Thread(() -> {
            File cacheDir = new File(CACHE_DIR);
            if (cacheDir.exists() && cacheDir.isDirectory()) {
                log.info("启动时扫描全民K歌缓存目录建立歌名索引...");
                buildTitleCache(cacheDir);
                log.info("歌名索引建立完成，共索引 {} 个歌名条目", titleToQrcCache.values().stream().mapToInt(List::size).sum());
            }
        }, "kg-cache-builder");
        cacheThread.setDaemon(true);
        cacheThread.start();
        startWatching();
    }

    @PreDestroy
    public void destroy() {
        stopWatching();
    }

    /**
     * 策略 B: 从本地缓存获取歌词（纯歌名索引匹配，不回退到最近修改文件）
     * @param windowTitle 当前窗口标题，用于匹配正确的歌词文件
     */
    public Lyric getLocalLyric(String windowTitle) {
        try {
            File cacheDir = new File(CACHE_DIR);
            if (!cacheDir.exists() || !cacheDir.isDirectory()) {
                log.info("全民K歌缓存目录不存在: {}", CACHE_DIR);
                return null;
            }

            String[] parsed = SongUtil.parseWindowTitle(windowTitle);
            String songTitle = parsed[0];

            File matchedQrc = null;

            // 优先：从 WeSing 日志提取 songmid，直接定位歌词文件（精确匹配，解决同名歌曲问题）
            String songMid = findSongMidFromLog(songTitle);
            if (songMid != null) {
                File directQrc = new File(CACHE_DIR + songMid + "\\" + songMid + ".qrc");
                if (directQrc.exists()) {
                    matchedQrc = directQrc;
                    log.info("通过日志 songmid 精确定位歌词: {}", directQrc.getAbsolutePath());
                }
            }

            // 回退：按歌名索引匹配
            if (matchedQrc == null) {
                matchedQrc = findQrcByTitle(cacheDir, songTitle);
            }

            if (matchedQrc == null) {
                log.info("未匹配到 .qrc 歌词文件，歌名: {}", songTitle);
                return null;
            }

            log.info("找到全民K歌本地歌词文件: {}", matchedQrc.getAbsolutePath());
            lastQrcPath = matchedQrc.getAbsolutePath();

            String qrcXml = decryptLocalQrc(matchedQrc);
            if (qrcXml == null || qrcXml.isEmpty()) {
                log.error("QRC 解密失败");
                return null;
            }

            return buildLyric(qrcXml);
        } catch (Exception e) {
            log.error("获取全民K歌本地歌词失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从全民K歌运行日志中提取最近一次选歌的 songmid
     * WeSing 每次选歌（即使歌曲已缓存）都会在日志中记录 StartKSong JSON，包含 mid 字段
     * 日志编码为 UTF-16LE
     * @param expectedSongName 预期的歌名（从窗口标题解析），用于验证日志条目是否匹配当前播放
     * @return songmid 或 null
     */
    private String findSongMidFromLog(String expectedSongName) {
        try {
            File logDir = new File(LOG_DIR);
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

            log.info("从 WeSing 日志提取到 songmid: {}", mid);
            return mid;
        } catch (Exception e) {
            log.debug("解析 WeSing 日志失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 根据窗口标题在缓存中查找最佳匹配的 .qrc 文件
     * 全民K歌窗口标题格式: "画你(女声版)(Live)"，QRC 中 [ti:] 可能是 "画你 （女声版）"
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
     * 同分时用 lastModified 时间戳选最近使用的（全民K歌播放时会写入/更新缓存文件）
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
                // 全民K歌每次播放都会重新写入 .qrc/.pcm 等文件，最近播放的目录时间最新
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
                    String qrcXml = decryptLocalQrc(qrcFile);
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
     * 全民K歌每次播放歌曲时会重新写入 .qrc/.pcm 等缓存文件
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

    // ==================== 策略 C: WatchService 文件监听 ====================

    /**
     * 启动 WatchService 监听 Res/ 目录及其子目录
     * 检测到新 .qrc 文件写入时，发布 LyricChangedEvent 通知歌词刷新
     */
    private void startWatching() {
        File cacheDir = new File(CACHE_DIR);
        if (!cacheDir.exists() || !cacheDir.isDirectory()) {
            log.info("全民K歌缓存目录不存在，跳过 WatchService: {}", CACHE_DIR);
            return;
        }

        watching = true;
        watchThread = new Thread(this::watchLoop, "kg-qrc-watcher");
        watchThread.setDaemon(true);
        watchThread.start();
        log.info("启动全民K歌缓存目录监听: {}", CACHE_DIR);
    }

    private void stopWatching() {
        watching = false;
        if (watchThread != null) {
            watchThread.interrupt();
        }
    }

    /**
     * WatchService 监听循环
     * 监听 Res/ 目录下新子目录的创建和已有子目录中 .qrc 文件的写入
     */
    private void watchLoop() {
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            Path resPath = Paths.get(CACHE_DIR);
            // 注册 Res/ 目录本身（监听新子目录创建）
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
                    File cacheDirForScan = new File(CACHE_DIR);
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

    // ==================== 工具方法 ====================

    /**
     * 解密本地 .qrc 文件
     */
    private String decryptLocalQrc(File qrcFile) {
        try {
            byte[] fileBytes = java.nio.file.Files.readAllBytes(qrcFile.toPath());

            int offset = 0;
            if (fileBytes.length > QRC_HEADER_LENGTH) {
                String header = new String(fileBytes, 0, QRC_HEADER_LENGTH);
                if (header.startsWith("[offset:")) {
                    for (int i = 0; i < Math.min(fileBytes.length, 50); i++) {
                        if (fileBytes[i] == '\n') {
                            offset = i + 1;
                            break;
                        }
                    }
                }
            }

            byte[] dataBytes = Arrays.copyOfRange(fileBytes, offset, fileBytes.length);
            StringBuilder hexString = new StringBuilder(dataBytes.length * 2);
            for (byte b : dataBytes) {
                hexString.append(String.format("%02x", b & 0xFF));
            }

            return Decrypter.decryptLyrics(hexString.toString());
        } catch (Exception e) {
            log.error("解密 QRC 文件失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将 QRC XML 转为 Lyric 对象
     * 注意: LyricContent 作为 XML 属性值时，换行符会被 DOM 解析器规范化为空格，
     * 因此直接从原始 XML 字符串中用正则提取 LyricContent，保留原始换行。
     */
    private Lyric buildLyric(String qrcXml) {
        Lyric lyric = new Lyric();
        // source 设为 "qq"，因为 QRC 格式与 QQ 音乐完全相同，
        // 前端逐字歌词解析器按 source 分发：netease→yrc, qq→qrc，"kg" 不被识别会导致歌词为空
        lyric.setSource("qq");

        try {
            // 从原始 XML 字符串中直接提取 LyricContent（避免 DOM 属性换行规范化问题）
            String content = extractLyricContent(qrcXml);

            if (content != null && !content.isEmpty()) {
                lyric.setKaraokeLyric(content);
                lyric.setHasKaraokeLyric(true);

                String lrc = convertQrcToLrc(content);
                if (lrc != null && !lrc.isEmpty()) {
                    lyric.setLrc(lrc);
                    lyric.setHasLyric(true);
                }
            }

            // 翻译歌词仍用 DOM 提取（无换行问题）
            Document doc = Decrypter.createXmlDocument(qrcXml);
            Map<String, String> mappingDict = new HashMap<>();
            mappingDict.put("BDLyric", "translatedLyric");
            Map<String, Node> resDict = new HashMap<>();
            Decrypter.recursionFindElement(doc.getDocumentElement(), mappingDict, resDict);

            Node translatedNode = resDict.get("translatedLyric");
            if (translatedNode != null) {
                String translatedContent = Decrypter.getNodeText(translatedNode);
                if (translatedContent != null && !translatedContent.isEmpty()) {
                    lyric.setTranslatedLyric(translatedContent);
                    lyric.setHasTranslatedLyric(true);
                }
            }

        } catch (Exception e) {
            log.error("解析 QRC XML 失败: {}", e.getMessage());
            if (qrcXml.contains("[") && qrcXml.contains("]")) {
                lyric.setLrc(qrcXml);
                lyric.setHasLyric(true);
            }
        }

        return lyric;
    }

    /**
     * 从原始 QRC XML 字符串中提取 LyricContent 属性值
     * 直接用字符串查找，避免 DOM 解析器将属性中的换行符规范化为空格
     * 使用转义感知的引号查找，处理歌词中可能包含 &quot; 转义引号的情况
     */
    private String extractLyricContent(String qrcXml) {
        String marker = "LyricContent=\"";
        int start = qrcXml.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();

        // 转义感知的结束引号查找：跳过被转义的引号（如 &quot;）
        int pos = start;
        while (pos < qrcXml.length()) {
            int quotePos = qrcXml.indexOf("\"", pos);
            if (quotePos < 0) {
                return null;
            }
            // 检查这个引号前面是否是 &quot 的一部分（即 &quot;）
            // &quot; 中 " 前面紧跟 &quot 共5个字符
            if (quotePos >= 5 && qrcXml.substring(quotePos - 5, quotePos + 1).equals("&quot;")) {
                pos = quotePos + 1;
                continue;
            }
            return qrcXml.substring(start, quotePos);
        }

        return null;
    }

    /**
     * 将 QRC 逐字歌词转换为标准 LRC 歌词
     * QRC 格式: [startMs,durationMs]字(charStartMs,charDuration)字(charStartMs,charDuration)...
     * LRC 格式: [MM:SS.xx]歌词文本
     */
    private String convertQrcToLrc(String qrcContent) {
        if (qrcContent == null || qrcContent.isEmpty()) {
            return null;
        }

        StringBuilder lrc = new StringBuilder();
        String[] lines = qrcContent.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 跳过元信息行 [ti:] [ar:] [al:] [by:] [offset:]
            if (line.startsWith("[ti:") || line.startsWith("[ar:") ||
                line.startsWith("[al:") || line.startsWith("[by:") ||
                line.startsWith("[offset:")) {
                continue;
            }

            // 先尝试匹配 QRC 毫秒格式 [startMs,durationMs]
            Matcher qrcMatcher = QRC_TIME_PATTERN.matcher(line);
            if (qrcMatcher.find()) {
                int startMs = Integer.parseInt(qrcMatcher.group(1));
                String timeTag = msToLrcTime(startMs);

                // 提取歌词文本：去掉行首的 [ms,ms]，再去掉逐字时间标签 (ms,ms)
                String text = line.substring(qrcMatcher.end());
                text = text.replaceAll("\\(\\d+,\\d+\\)", "");
                text = text.trim();

                if (!text.isEmpty()) {
                    lrc.append(timeTag).append(text).append("\n");
                }
                continue;
            }

            // 再尝试匹配标准 LRC 格式 [MM:SS.ms]
            Matcher lrcMatcher = LRC_TIME_PATTERN.matcher(line);
            if (lrcMatcher.find()) {
                String timeTag = lrcMatcher.group(0);
                String text = line.substring(lrcMatcher.end());
                text = text.replaceAll("\\(\\d+,\\d+\\)", "");
                text = text.replaceAll("\\[\\d+,\\d+]", "");
                text = text.trim();

                if (!text.isEmpty()) {
                    lrc.append(timeTag).append(text).append("\n");
                }
            }
        }

        return lrc.toString();
    }

    /**
     * 将毫秒转换为 LRC 时间标签 [MM:SS.xx]
     */
    private String msToLrcTime(int ms) {
        int totalSec = ms / 1000;
        int minutes = totalSec / 60;
        int seconds = totalSec % 60;
        int hundredths = (ms % 1000) / 10;
        return String.format("[%02d:%02d.%02d]", minutes, seconds, hundredths);
    }
}