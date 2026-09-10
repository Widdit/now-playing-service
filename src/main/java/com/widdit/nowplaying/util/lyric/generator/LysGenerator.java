package com.widdit.nowplaying.util.lyric.generator;

import com.widdit.nowplaying.util.lyric.model.LyricLine;
import com.widdit.nowplaying.util.lyric.model.LyricSyllable;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * lys（Lyricify Syllable）格式逐字歌词生成器。
 *
 * <p>lys 歌词行格式形如：</p>
 * <pre>
 * [property]Word (start,duration)Word(start,duration)...
 * </pre>
 *
 * <p>由于内部数据模型 {@link LyricLine}、{@link LyricSyllable} 并未记录歌词行属性
 * （背景人声、对唱视图等信息），因此生成时统一将 property 设为 0（普通歌词）。</p>
 */
public class LysGenerator {

    /**
     * lys 歌词行统一使用的属性值，0 表示普通歌词（未设置背景人声、未设置对唱视图）。
     */
    private static final String DEFAULT_PROPERTY = "[0]";

    private LysGenerator() {
        // 工具类，禁止实例化
    }

    /**
     * 根据逐字歌词行列表生成 lys 格式的歌词字符串（不含 from 来源标记行）。
     *
     * @param lyricLines 逐字歌词行列表
     * @return lys 格式歌词字符串；若 lyricLines 为空，返回空字符串
     */
    public static String generate(List<LyricLine> lyricLines) {
        return generate(lyricLines, null);
    }

    /**
     * 根据逐字歌词行列表生成 lys 格式的歌词字符串。
     *
     * @param lyricLines 逐字歌词行列表
     * @param from       歌词来源标记（例如 "yrc"、"qrc"、"krc"），若非空，
     *                   会在生成内容顶部添加一行 "[from:xxx]"；若为 null 或空字符串，则不添加该行
     * @return lys 格式歌词字符串；若 lyricLines 为空且 from 也为空，返回空字符串
     */
    public static String generate(List<LyricLine> lyricLines, String from) {
        StringBuilder sb = new StringBuilder();

        if (StringUtils.hasText(from)) {
            sb.append("[from:").append(from).append("]\n");
        }

        if (lyricLines != null) {
            for (LyricLine line : lyricLines) {
                appendLine(sb, line);
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
     * 将单个 LyricLine 拼接为 lys 格式的一行文本，追加到 StringBuilder 中。
     *
     * @param sb   目标 StringBuilder
     * @param line 待拼接的歌词行
     */
    private static void appendLine(StringBuilder sb, LyricLine line) {
        sb.append(DEFAULT_PROPERTY);

        if (line == null || line.getSyllables() == null) {
            return;
        }

        for (LyricSyllable syllable : line.getSyllables()) {
            if (syllable == null) {
                continue;
            }
            String word = syllable.getWord();
            sb.append(word != null ? word : "");
            sb.append('(')
                    .append(syllable.getStartTime())
                    .append(',')
                    .append(syllable.getDuration())
                    .append(')');
        }
    }
}
