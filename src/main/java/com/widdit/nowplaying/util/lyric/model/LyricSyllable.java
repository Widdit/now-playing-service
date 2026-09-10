package com.widdit.nowplaying.util.lyric.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 歌词音节，逐字歌词的最小单元
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LyricSyllable {

    /**
     * 起始时间（毫秒）
     */
    private long startTime;

    /**
     * 持续时长（毫秒）
     */
    private long duration;

    /**
     * 音节文本
     */
    private String word;

}
