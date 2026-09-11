package com.widdit.nowplaying.util.lyric.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.widdit.nowplaying.util.lyric.model.LyricLine;
import com.widdit.nowplaying.util.lyric.model.LyricSyllable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 酷狗音乐 krc 格式（逐字歌词）解析器。
 *
 * <p>krc 歌词内容通常由以下几种行组成：</p>
 * <ul>
 *     <li>元数据行：形如 [id:xxx]、[ar:xxx]、[ti:xxx]、[by:xxx]、[hash:xxx]、[al:xxx]、
 *     [sign:xxx]、[qq:xxx]、[total:xxx]、[offset:xxx] 等，
 *     与逐字歌词内容无关，会被本解析器丢弃。</li>
 *     <li>歌词行：格式形如
 *     [行起始时间,行持续时长]&lt;音节相对起始时间,音节持续时长,标志位&gt;音节文字&lt;...&gt;音节文字...
 *     其中音节的起始时间是相对于行起始时间的偏移量，需与行起始时间相加得到绝对时间。</li>
 * </ul>
 *
 * <p>解析结果为 {@link LyricLine} 列表，每个 LyricLine 内部包含若干 {@link LyricSyllable}。</p>
 *
 * <p>此外，本解析器还支持从 krc 内容中的 [language:xxx] 行提取翻译歌词。该行内容为 Base64
 * 编码的 JSON 数据，解码后形如：</p>
 * <pre>
 * {"content":[{"type":1,"language":0,"lyricContent":[["翻译文本1"],["翻译文本2"], ...]}],"version":1}
 * </pre>
 * <p>其中 lyricContent 数组与 krc 中的逐行歌词（含标题、作词等伪歌词行）一一对应，
 * 若某行无翻译，则对应位置内容通常为空格或 "//"。</p>
 */
public class KrcParser {

    /**
     * 每分钟对应的厘秒（centisecond，1/100 秒）数量。
     */
    private static final long CENTISECONDS_PER_MINUTE = 6000;

    /**
     * 每秒对应的厘秒数量。
     */
    private static final long CENTISECONDS_PER_SECOND = 100;

    /**
     * 匹配歌词行行首的 [lineStartTime,lineDuration]，并将剩余内容作为一个分组捕获，供后续解析音节使用。
     * 由于方括号内要求为纯数字，天然可以将元数据行（如 [id:xxx]、[language:xxx]）排除在外。
     */
    private static final Pattern LINE_HEADER_PATTERN = Pattern.compile("^\\[(\\d+),(\\d+)](.*)$");

    /**
     * 匹配歌词行内的音节片段：&lt;relativeStartTime,duration,flag&gt;word
     * word 为从当前尖括号结束处到下一个 '&lt;' 之前的所有字符。
     */
    private static final Pattern SYLLABLE_PATTERN = Pattern.compile("<(\\d+),(\\d+),(\\d+)>([^<]*)");

    /**
     * 匹配 [language:xxx] 行，捕获其中 Base64 编码的翻译数据。
     */
    private static final Pattern LANGUAGE_PATTERN = Pattern.compile("\\[language:([^]]*)]");

    private KrcParser() {
        // 工具类，禁止实例化
    }

    /**
     * 解析 krc 格式的逐字歌词文本。
     *
     * <p>会自动丢弃元数据行（如 [id:xxx]、[ar:xxx] 等）以及包含翻译数据的 [language:xxx] 行，
     * 仅保留并解析真正的逐字歌词内容。</p>
     *
     * @param krcContent krc 格式歌词原始文本（可能包含多行，含元数据行与歌词行）
     * @return 解析后的逐字歌词行列表，若输入为空或无有效歌词行，返回空列表
     */
    public static List<LyricLine> parse(String krcContent) {
        List<LyricLine> lyricLines = new ArrayList<>();
        if (krcContent == null || krcContent.isEmpty()) {
            return lyricLines;
        }

        String[] rawLines = krcContent.split("\\r?\\n");
        for (String rawLine : rawLines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            // 非 "[" 开头的行直接丢弃（正常情况下 krc 每行都应以 "[" 开头）
            if (line.charAt(0) != '[') {
                continue;
            }
            LyricLine lyricLine = parseLine(line);
            if (lyricLine != null) {
                lyricLines.add(lyricLine);
            }
        }
        return lyricLines;
    }

    /**
     * 解析单行歌词文本，形如：
     * [790,3661]&lt;0,1072,0&gt;Lately &lt;1072,533,0&gt;I've &lt;1605,471,0&gt;been...
     *
     * <p>若该行不符合 [数字,数字]... 的格式（例如元数据行 [id:xxx]、[language:xxx] 等），返回 null。</p>
     *
     * @param line 单行歌词文本（已去除首尾空白）
     * @return 解析后的 LyricLine，若该行格式不匹配或不含有效音节，返回 null
     */
    private static LyricLine parseLine(String line) {
        Matcher headerMatcher = LINE_HEADER_PATTERN.matcher(line);
        if (!headerMatcher.matches()) {
            // 方括号内不是纯数字格式（如 [id:xxx]、[language:xxx] 等元数据行），丢弃
            return null;
        }

        long lineStartTime;
        long lineDuration;
        try {
            lineStartTime = Long.parseLong(headerMatcher.group(1));
            lineDuration = Long.parseLong(headerMatcher.group(2));
        } catch (NumberFormatException e) {
            return null;
        }

        String syllablesPart = headerMatcher.group(3);
        List<LyricSyllable> syllables = new ArrayList<>();

        Matcher syllableMatcher = SYLLABLE_PATTERN.matcher(syllablesPart);
        while (syllableMatcher.find()) {
            long relativeStartTime;
            long wordDuration;
            try {
                relativeStartTime = Long.parseLong(syllableMatcher.group(1));
                wordDuration = Long.parseLong(syllableMatcher.group(2));
            } catch (NumberFormatException e) {
                continue;
            }
            // group(3) 为标志位，逐字歌词渲染中通常无实际意义，此处不使用
            String word = syllableMatcher.group(4);

            syllables.add(LyricSyllable.builder()
                    // 音节的绝对起始时间 = 行起始时间 + 音节相对起始时间
                    .startTime(lineStartTime + relativeStartTime)
                    .duration(wordDuration)
                    .word(word)
                    .build());
        }

        if (syllables.isEmpty()) {
            return null;
        }

        return LyricLine.builder()
                .startTime(lineStartTime)
                .duration(lineDuration)
                .syllables(syllables)
                .build();
    }

    /**
     * 从 krc 内容中提取翻译歌词，并生成 LRC 格式的翻译歌词字符串。
     *
     * <p>翻译内容与 {@code lyricLines}（即 {@link #parse(String)} 的解析结果）按下标一一对应，
     * 每一条翻译文本使用对应歌词行的起始时间作为时间戳。若某行没有翻译文本（内容为空白或 "//"），
     * 该行会以空文本的形式保留，以维持与原始歌词行数一致。</p>
     *
     * @param krcContent krc 格式歌词原始文本
     * @param lyricLines 通过 {@link #parse(String)} 解析得到的逐字歌词行列表，用于提供每行的起始时间
     * @return LRC 格式的翻译歌词字符串；若 krc 中不包含翻译数据，或翻译数据为空，返回 null
     */
    public static String parseTranslationLrc(String krcContent, List<LyricLine> lyricLines) {
        if (lyricLines == null || lyricLines.isEmpty()) {
            return null;
        }

        List<String> translations = extractTranslationLines(krcContent);
        if (translations == null || translations.isEmpty()) {
            return null;
        }

        int count = Math.min(lyricLines.size(), translations.size());
        StringBuilder lrcBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            String text = translations.get(i);
            if (text == null) {
                text = "";
            }
            text = text.trim();
            // "//" 是酷狗翻译数据中用于表示无翻译内容的占位符
            if ("//".equals(text)) {
                text = "";
            }
            long startTime = lyricLines.get(i).getStartTime();
            lrcBuilder.append(formatLrcTimeTag(startTime)).append(text).append('\n');
        }

        if (lrcBuilder.length() == 0) {
            return null;
        }
        // 去除末尾多余的换行符
        return lrcBuilder.substring(0, lrcBuilder.length() - 1);
    }

    /**
     * 从 krc 内容中提取翻译歌词的原始文本列表（按行对应，未附加时间戳）。
     *
     * <p>krc 中的翻译数据位于 [language:xxx] 行，xxx 为 Base64 编码的 JSON 数据，解码后形如：</p>
     * <pre>
     * {"content":[{"type":1,"language":0,"lyricContent":[["翻译文本1"],["翻译文本2"], ...]}],"version":1}
     * </pre>
     *
     * @param krcContent krc 格式歌词原始文本
     * @return 翻译文本列表（与 krc 中歌词行按下标一一对应），若不存在翻译数据或解析失败，返回 null
     */
    private static List<String> extractTranslationLines(String krcContent) {
        if (krcContent == null || krcContent.isEmpty()) {
            return null;
        }

        Matcher languageMatcher = LANGUAGE_PATTERN.matcher(krcContent);
        if (!languageMatcher.find()) {
            return null;
        }

        String base64Language = languageMatcher.group(1);
        if (base64Language == null || base64Language.trim().isEmpty()) {
            return null;
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Language.trim());
            String json = new String(decodedBytes, StandardCharsets.UTF_8);

            JSONObject root = JSON.parseObject(json);
            if (root == null) {
                return null;
            }

            JSONArray contentArray = root.getJSONArray("content");
            if (contentArray == null || contentArray.isEmpty()) {
                return null;
            }

            JSONObject translationObj = null;
            for (int i = 0; i < contentArray.size(); i++) {
                JSONObject item = contentArray.getJSONObject(i);
                if (item != null && item.getIntValue("type") == 1) {
                    translationObj = item;
                    break;
                }
            }
            if (translationObj == null) {
                return null;
            }

            JSONArray lyricContentArray = translationObj.getJSONArray("lyricContent");
            if (lyricContentArray == null || lyricContentArray.isEmpty()) {
                return null;
            }

            List<String> translations = new ArrayList<>();
            for (int i = 0; i < lyricContentArray.size(); i++) {
                JSONArray lineArray = lyricContentArray.getJSONArray(i);
                String text = "";
                if (lineArray != null && !lineArray.isEmpty()) {
                    String raw = lineArray.getString(0);
                    text = raw == null ? "" : raw;
                }
                translations.add(text);
            }
            return translations;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将毫秒时间戳格式化为 lrc 标准的 [mm:ss.xx] 格式（分:秒.厘秒，厘秒为两位数）。
     *
     * <p>该转换逻辑与 {@link com.widdit.nowplaying.util.lyric.generator.LrcGenerator} 保持一致，
     * 均采用"先四舍五入为厘秒，再拆分为分、秒、厘秒"的方式，以保证同一时间点在原始 lrc 与
     * 翻译 lrc 中生成完全相同的时间戳，避免出现两者时间戳不一致的问题。</p>
     *
     * @param startTimeMs 起始时间（毫秒）
     * @return 形如 "[00:14.87]" 的时间戳字符串
     */
    private static String formatLrcTimeTag(long startTimeMs) {
        long totalCentiseconds = Math.round(startTimeMs / 10.0);
        long minutes = totalCentiseconds / CENTISECONDS_PER_MINUTE;
        long seconds = (totalCentiseconds / CENTISECONDS_PER_SECOND) % 60;
        long centiseconds = totalCentiseconds % CENTISECONDS_PER_SECOND;
        return String.format("[%02d:%02d.%02d]", minutes, seconds, centiseconds);
    }
}
