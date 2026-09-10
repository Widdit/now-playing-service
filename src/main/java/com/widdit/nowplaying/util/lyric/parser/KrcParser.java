package com.widdit.nowplaying.util.lyric.parser;

import com.widdit.nowplaying.util.lyric.model.LyricLine;
import com.widdit.nowplaying.util.lyric.model.LyricSyllable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 酷狗音乐 krc 格式（逐字歌词）解析器。
 *
 * <p>krc 歌词内容通常由以下几种行组成：</p>
 * <ul>
 *     <li>元数据行：形如 [id:xxx]、[ar:xxx]、[ti:xxx]、[by:xxx]、[hash:xxx]、[al:xxx]、
 *     [sign:xxx]、[qq:xxx]、[total:xxx]、[offset:xxx]、[language:xxx]（Base64 编码的翻译数据）等，
 *     与逐字歌词内容无关，会被本解析器丢弃。</li>
 *     <li>歌词行：格式形如
 *     [行起始时间,行持续时长]&lt;音节相对起始时间,音节持续时长,标志位&gt;音节文字&lt;...&gt;音节文字...
 *     其中音节的起始时间是相对于行起始时间的偏移量，需与行起始时间相加得到绝对时间。</li>
 * </ul>
 *
 * <p>解析结果为 {@link LyricLine} 列表，每个 LyricLine 内部包含若干 {@link LyricSyllable}。</p>
 */
public class KrcParser {

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
}
