package com.widdit.nowplaying.util.lyric.parser;

import com.widdit.nowplaying.util.lyric.model.LyricLine;
import com.widdit.nowplaying.util.lyric.model.LyricSyllable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网易云音乐 yrc 格式（逐字歌词）解析器。
 *
 * <p>yrc 歌词内容通常由以下几种行组成：</p>
 * <ul>
 *     <li>元数据行：以 "{" 开头的 JSON，例如 {"t":0,"c":[{"tx":"作词: "}...]}，
 *     用于记录作词、作曲等信息，与歌词内容无关，会被本解析器丢弃。</li>
 *     <li>歌词行：格式形如
 *     [行起始时间,行持续时长](音节起始时间,音节持续时长,0)音节文字(音节起始时间,音节持续时长,0)音节文字...</li>
 * </ul>
 *
 * <p>解析结果为 {@link LyricLine} 列表，每个 LyricLine 内部包含若干 {@link LyricSyllable}。</p>
 */
public class YrcParser {

    /**
     * 匹配歌词行行首的 [startTime,duration]，并将剩余内容作为一个分组捕获，供后续解析音节使用。
     */
    private static final Pattern LINE_HEADER_PATTERN = Pattern.compile("^\\[(\\d+),(\\d+)](.*)$");

    /**
     * 匹配歌词行内的音节片段：(startTime,duration,flag)word
     * word 为从当前括号结束处到下一个 '(' 之前的所有字符（不含括号本身）。
     */
    private static final Pattern SYLLABLE_PATTERN = Pattern.compile("\\((\\d+),(\\d+),(\\d+)\\)([^(]*)");

    private YrcParser() {
        // 工具类，禁止实例化
    }

    /**
     * 解析 yrc 格式的逐字歌词文本。
     *
     * @param yrcContent yrc 格式歌词原始文本（可能包含多行，含元数据行与歌词行）
     * @return 解析后的逐字歌词行列表，若输入为空或无有效歌词行，返回空列表
     */
    public static List<LyricLine> parse(String yrcContent) {
        List<LyricLine> lyricLines = new ArrayList<>();
        if (yrcContent == null || yrcContent.isEmpty()) {
            return lyricLines;
        }

        String[] rawLines = yrcContent.split("\\r?\\n");
        for (String rawLine : rawLines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            // 丢弃元数据行（作词、作曲等信息，格式为 JSON，以 "{" 开头）
            if (line.charAt(0) == '{') {
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
     * [420,4440](420,1320,0)Lately(1740,0,0), (1740,570,0)I've (2310,600,0)been...
     *
     * @param line 单行歌词文本（已去除首尾空白）
     * @return 解析后的 LyricLine，若该行格式不匹配或不含有效音节，返回 null
     */
    private static LyricLine parseLine(String line) {
        Matcher headerMatcher = LINE_HEADER_PATTERN.matcher(line);
        if (!headerMatcher.matches()) {
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
            long wordStartTime;
            long wordDuration;
            try {
                wordStartTime = Long.parseLong(syllableMatcher.group(1));
                wordDuration = Long.parseLong(syllableMatcher.group(2));
            } catch (NumberFormatException e) {
                continue;
            }
            String word = syllableMatcher.group(4);

            syllables.add(LyricSyllable.builder()
                    .startTime(wordStartTime)
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
