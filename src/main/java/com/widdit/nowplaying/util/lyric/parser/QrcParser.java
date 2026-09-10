package com.widdit.nowplaying.util.lyric.parser;

import com.widdit.nowplaying.util.lyric.model.LyricLine;
import com.widdit.nowplaying.util.lyric.model.LyricSyllable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * QQ 音乐 qrc 格式（逐字歌词）解析器。
 *
 * <p>qrc 歌词内容通常由以下几种行组成：</p>
 * <ul>
 *     <li>元数据行：形如 [ti:xxx]、[ar:xxx]、[al:xxx]、[by:xxx]、[offset:xxx]，
 *     与歌词内容无关，会被本解析器丢弃。</li>
 *     <li>歌词行：格式形如
 *     [行起始时间,行持续时长]文字1(音节起始时间,音节持续时长)文字2(音节起始时间,音节持续时长)...</li>
 * </ul>
 *
 * <p>与 yrc 格式不同，qrc 格式中每个音节的文本在时间信息 (start,duration) 之前。</p>
 *
 * <p>解析结果为 {@link LyricLine} 列表，每个 LyricLine 内部包含若干 {@link LyricSyllable}。</p>
 */
public class QrcParser {

    /**
     * 匹配歌词行行首的 [startTime,duration]，并将剩余内容作为一个分组捕获，供后续解析音节使用。
     * 若某行不满足该格式（例如元数据行 [ti:xxx]），则视为非歌词行，会被跳过。
     */
    private static final Pattern LINE_HEADER_PATTERN = Pattern.compile("^\\[(\\d+),(\\d+)](.*)$");

    /**
     * 匹配歌词行内的音节片段：word(startTime,duration)
     * word 为从上一个音节结束处（或行首）到当前 '(' 之前的所有字符（非贪婪匹配，可包含括号等特殊字符）。
     */
    private static final Pattern SYLLABLE_PATTERN = Pattern.compile("(.*?)\\((\\d+),(\\d+)\\)");

    private QrcParser() {
        // 工具类，禁止实例化
    }

    /**
     * 解析 qrc 格式的逐字歌词文本。
     *
     * @param qrcContent qrc 格式歌词原始文本（可能包含多行，含元数据行与歌词行）
     * @return 解析后的逐字歌词行列表，若输入为空或无有效歌词行，返回空列表
     */
    public static List<LyricLine> parse(String qrcContent) {
        List<LyricLine> lyricLines = new ArrayList<>();
        if (qrcContent == null || qrcContent.isEmpty()) {
            return lyricLines;
        }

        String[] rawLines = qrcContent.split("\\r?\\n");
        for (String rawLine : rawLines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
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
     * [0,4390]Stop(0,274) (274,274)And(548,274) (822,274)Stare(1096,274)...
     *
     * <p>若该行不符合 [start,duration]... 的格式（例如元数据行），返回 null。</p>
     *
     * @param line 单行歌词文本（已去除首尾空白）
     * @return 解析后的 LyricLine，若该行格式不匹配或不含有效音节，返回 null
     */
    private static LyricLine parseLine(String line) {
        Matcher headerMatcher = LINE_HEADER_PATTERN.matcher(line);
        if (!headerMatcher.matches()) {
            // 不符合 [数字,数字]... 格式，视为元数据行（如 [ti:xxx]、[offset:0] 等），丢弃
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
            String word = syllableMatcher.group(1);
            long wordStartTime;
            long wordDuration;
            try {
                wordStartTime = Long.parseLong(syllableMatcher.group(2));
                wordDuration = Long.parseLong(syllableMatcher.group(3));
            } catch (NumberFormatException e) {
                continue;
            }

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
