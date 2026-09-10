package com.widdit.nowplaying.util.lyric.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 逐字歌词行，包含若干音节
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LyricLine {

    /**
     * 起始时间（毫秒）
     */
    private long startTime;

    /**
     * 持续时长（毫秒）
     */
    private long duration;

    /**
     * 音节列表
     */
    private List<LyricSyllable> syllables = new ArrayList<>();

}
