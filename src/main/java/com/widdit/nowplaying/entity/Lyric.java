package com.widdit.nowplaying.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lyric {

    // 歌词来源（netease 或 qq）
    private String source = "netease";

    // 是否有歌词
    private Boolean hasLyric = false;

    // 是否有翻译歌词
    private Boolean hasTranslatedLyric = false;

    // 是否有逐词歌词
    private Boolean hasKaraokeLyric = false;

    // 歌词
    private String lrc;

    // 翻译歌词
    private String translatedLyric;

    // 逐词歌词
    private String karaokeLyric;

}
