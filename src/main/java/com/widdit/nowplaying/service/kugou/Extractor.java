package com.widdit.nowplaying.service.kugou;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 酷狗 KRC 逐字歌词提取工具类。
 * <p>
 * 推荐用法（同时需要 LRC 歌词和翻译歌词时，只解析一次 KRC）：
 * <pre>
 *     Extractor.ExtractedKrc extracted = Extractor.extract(krc);
 *     String lrc = extracted.toLrc();
 *     String translationLrc = extracted.toTranslationLrc(); // 无翻译时返回 ""
 * </pre>
 * <p>
 * 如果只需要其中一种歌词，也可以使用便捷静态方法：
 * <pre>
 *     String lrc = Extractor.extractLrc(krc);
 *     String translationLrc = Extractor.extractTranslationLrc(krc);
 * </pre>
 */
public class Extractor {

    private Extractor() {
    }

    /**
     * 匹配 KRC 歌词行，如 [790,3661]<0,1072,0>Lately ...
     * group1 = 起始时间(ms)，group2 = 持续时间(ms)，group3 = 逐字内容
     */
    private static final Pattern LINE_TIME_PATTERN = Pattern.compile("^\\[(\\d+),(\\d+)\\](.*)$");

    /**
     * 匹配头部属性行，如 [ar:xxx]、[ti:xxx] 等
     */
    private static final Pattern ATTR_PATTERN = Pattern.compile("^\\[([a-zA-Z0-9_]+):(.*)]$");

    /**
     * 匹配逐字时间标记 <偏移,时长,标志>
     */
    private static final Pattern SYLLABLE_TAG_PATTERN = Pattern.compile("<\\d+,\\d+,\\d+>");

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // =========================== 对外方法 ===========================

    /**
     * 提取 KRC 歌词，返回一个包含正文歌词与翻译歌词中间数据的结果对象。
     * 后续调用 {@link ExtractedKrc#toLrc()} 与 {@link ExtractedKrc#toTranslationLrc()}
     * 均基于此次解析结果，不会重复解析原始 KRC 字符串。
     *
     * @param krc 酷狗 KRC 格式歌词（已解密的明文字符串）
     * @return 提取结果对象，永不为 null
     */
    public static ExtractedKrc extract(String krc) {
        List<String> headerLines = new ArrayList<>();
        List<LyricLine> lyricLines = new ArrayList<>();

        if (krc != null && !krc.isEmpty()) {
            String normalized = krc.replace("\r\n", "\n").replace("\r", "\n");
            String[] rawLines = normalized.split("\n");

            for (String raw : rawLines) {
                String line = raw.trim();
                if (line.isEmpty() || !line.startsWith("[")) {
                    continue;
                }

                Matcher timeMatcher = LINE_TIME_PATTERN.matcher(line);
                if (timeMatcher.matches()) {
                    long startMs;
                    try {
                        startMs = Long.parseLong(timeMatcher.group(1));
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    String rawContent = timeMatcher.group(3);
                    String text = SYLLABLE_TAG_PATTERN.matcher(rawContent).replaceAll("");
                    lyricLines.add(new LyricLine(startMs, text));
                    continue;
                }

                Matcher attrMatcher = ATTR_PATTERN.matcher(line);
                if (attrMatcher.matches()) {
                    String key = attrMatcher.group(1);
                    if (!"language".equalsIgnoreCase(key)) {
                        headerLines.add(line);
                    }
                }
            }
        }

        List<String> translations = (krc == null || krc.isEmpty())
                ? null
                : extractTranslationTexts(krc);

        return new ExtractedKrc(headerLines, lyricLines, translations);
    }

    /**
     * 便捷方法：从 KRC 歌词字符串中提取出 LRC 格式的正文歌词。
     * 若同时还需要翻译歌词，建议改用 {@link #extract(String)}，避免重复解析。
     */
    public static String extractLrc(String krc) {
        return extract(krc).toLrc();
    }

    /**
     * 便捷方法：从 KRC 歌词字符串中提取出翻译歌词（LRC 格式），无翻译时返回空字符串。
     * 若同时还需要正文歌词，建议改用 {@link #extract(String)}，避免重复解析。
     */
    public static String extractTranslationLrc(String krc) {
        return extract(krc).toTranslationLrc();
    }

    // =========================== 内部数据结构 ===========================

    /**
     * 表示一行歌词的中间数据（起始时间 + 去除逐字标记后的文本）
     */
    private static final class LyricLine {
        final long startMs;
        final String text;

        LyricLine(long startMs, String text) {
            this.startMs = startMs;
            this.text = text;
        }
    }

    /**
     * KRC 提取结果，缓存了生成正文 LRC 与翻译 LRC 所需的全部中间数据。
     * 可重复调用 {@link #toLrc()} / {@link #toTranslationLrc()}，不会重复解析原始 KRC。
     */
    public static final class ExtractedKrc {
        private final List<String> headerLines;
        private final List<LyricLine> lyricLines;
        private final List<String> translationTexts; // 可能为 null，代表无翻译

        private ExtractedKrc(List<String> headerLines, List<LyricLine> lyricLines, List<String> translationTexts) {
            this.headerLines = headerLines;
            this.lyricLines = lyricLines;
            this.translationTexts = translationTexts;
        }

        /**
         * 是否存在有效翻译
         */
        public boolean hasTranslation() {
            if (translationTexts == null || translationTexts.isEmpty()) {
                return false;
            }
            for (String t : translationTexts) {
                if (t != null && !t.trim().isEmpty() && !"//".equals(t.trim())) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 生成 LRC 格式的正文歌词
         */
        public String toLrc() {
            if (lyricLines.isEmpty()) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (String header : headerLines) {
                sb.append(header).append('\n');
            }
            for (LyricLine line : lyricLines) {
                sb.append(formatLrcLine(line.startMs, line.text)).append('\n');
            }
            return sb.toString().trim();
        }

        /**
         * 生成 LRC 格式的翻译歌词，无翻译时返回空字符串
         */
        public String toTranslationLrc() {
            if (!hasTranslation() || lyricLines.isEmpty()) {
                return "";
            }

            int count = Math.min(lyricLines.size(), translationTexts.size());
            StringBuilder sb = new StringBuilder();
            for (String header : headerLines) {
                sb.append(header).append('\n');
            }
            for (int i = 0; i < count; i++) {
                String text = translationTexts.get(i);
                if (text == null) {
                    text = "";
                }
                text = text.trim();
                if ("//".equals(text)) {
                    text = "";
                }
                sb.append(formatLrcLine(lyricLines.get(i).startMs, text)).append('\n');
            }
            return sb.toString().trim();
        }

        /**
         * 获取正文歌词行数（可用于校验/调试）
         */
        public int getLineCount() {
            return lyricLines.size();
        }

        /**
         * 获取原始的翻译文本列表（未做 LRC 格式化），无翻译返回空列表
         */
        public List<String> getRawTranslationTexts() {
            return translationTexts == null
                    ? Collections.emptyList()
                    : Collections.unmodifiableList(translationTexts);
        }
    }

    // =========================== 内部工具方法 ===========================

    /**
     * 将毫秒时间转换为 LRC 行，如 [00:05.49]xxx
     */
    private static String formatLrcLine(long ms, String text) {
        long minutes = ms / 60000;
        long seconds = (ms % 60000) / 1000;
        long centis = (ms % 1000) / 10;
        return String.format("[%02d:%02d.%02d]%s", minutes, seconds, centis, text);
    }

    /**
     * 从 KRC 中提取翻译文本列表（按行对应正文歌词行），无翻译则返回 null
     */
    private static List<String> extractTranslationTexts(String krc) {
        String base64 = extractLanguageBase64(krc);
        if (base64 == null || base64.isEmpty()) {
            return null;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(base64);
            String json = new String(decoded, StandardCharsets.UTF_8);

            JsonNode root = OBJECT_MAPPER.readTree(json);
            JsonNode contentArray = root.get("content");
            if (contentArray == null || !contentArray.isArray() || contentArray.size() == 0) {
                return null;
            }

            for (JsonNode item : contentArray) {
                JsonNode typeNode = item.get("type");
                if (typeNode != null && typeNode.asInt() == 1) {
                    JsonNode lyricContent = item.get("lyricContent");
                    if (lyricContent == null || !lyricContent.isArray()) {
                        return null;
                    }
                    List<String> lines = new ArrayList<>();
                    for (JsonNode lineNode : lyricContent) {
                        if (lineNode.isArray() && lineNode.size() > 0) {
                            lines.add(lineNode.get(0).asText(""));
                        } else {
                            lines.add("");
                        }
                    }
                    return lines;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 KRC 原文中提取 [language:xxx] 里的 base64 字符串
     */
    private static String extractLanguageBase64(String krc) {
        int idx = krc.indexOf("[language:");
        if (idx < 0) {
            return null;
        }
        int start = idx + "[language:".length();
        int end = krc.indexOf(']', start);
        if (end < 0) {
            return null;
        }
        return krc.substring(start, end).trim();
    }
}
