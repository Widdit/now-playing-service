package com.widdit.nowplaying.util.lyric.generator;

import com.widdit.nowplaying.util.lyric.model.LyricLine;
import com.widdit.nowplaying.util.lyric.model.LyricSyllable;

import java.util.List;

/**
 * lrc（逐行歌词）格式生成器。
 *
 * <p>本工具类将 {@link LyricLine} 列表转换为标准 lrc 格式字符串，其中每行的文本内容
 * 由该行内所有 {@link LyricSyllable} 的文本按顺序拼接而成。</p>
 *
 * <p>lrc 行格式形如：</p>
 * <pre>
 * [00:14.87]Hate to give the satisfaction asking how you're doing now
 * </pre>
 */
public class LrcGenerator {

    /**
     * 每分钟对应的厘秒（centisecond，1/100 秒）数量。
     */
    private static final long CENTISECONDS_PER_MINUTE = 6000;

    /**
     * 每秒对应的厘秒数量。
     */
    private static final long CENTISECONDS_PER_SECOND = 100;

    private LrcGenerator() {
        // 工具类，禁止实例化
    }

    /**
     * 根据逐字歌词行列表生成 lrc 格式的歌词字符串。
     *
     * @param lyricLines 逐字歌词行列表（内部会被聚合为逐行文本，丢弃逐字时间信息）
     * @return lrc 格式歌词字符串；若 lyricLines 为空，返回空字符串
     */
    public static String generate(List<LyricLine> lyricLines) {
        StringBuilder sb = new StringBuilder();

        if (lyricLines != null) {
            for (LyricLine line : lyricLines) {
                if (line == null) {
                    continue;
                }
                sb.append(formatTimestamp(line.getStartTime()));
                sb.append(buildLineText(line));
                sb.append('\n');
            }
        }

        // 去除末尾多余的换行符
        int length = sb.length();
        if (length > 0 && sb.charAt(length - 1) == '\n') {
            sb.setLength(length - 1);
        }

        return sb.toString();
    }

    /**
     * 将一行内所有音节的文本按顺序拼接为完整的行文本。
     *
     * @param line 歌词行
     * @return 拼接后的行文本；若无音节，返回空字符串
     */
    private static String buildLineText(LyricLine line) {
        StringBuilder text = new StringBuilder();
        if (line.getSyllables() != null) {
            for (LyricSyllable syllable : line.getSyllables()) {
                if (syllable != null && syllable.getWord() != null) {
                    text.append(syllable.getWord());
                }
            }
        }
        return text.toString();
    }

    /**
     * 将毫秒时间戳格式化为 lrc 标准的 [mm:ss.xx] 格式（分:秒.厘秒，厘秒为两位数）。
     *
     * @param startTimeMs 起始时间（毫秒）
     * @return 形如 "[00:14.87]" 的时间戳字符串
     */
    private static String formatTimestamp(long startTimeMs) {
        long totalCentiseconds = Math.round(startTimeMs / 10.0);
        long minutes = totalCentiseconds / CENTISECONDS_PER_MINUTE;
        long seconds = (totalCentiseconds / CENTISECONDS_PER_SECOND) % 60;
        long centiseconds = totalCentiseconds % CENTISECONDS_PER_SECOND;
        return String.format("[%02d:%02d.%02d]", minutes, seconds, centiseconds);
    }
}
