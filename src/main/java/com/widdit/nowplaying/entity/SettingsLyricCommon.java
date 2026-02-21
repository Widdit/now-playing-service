package com.widdit.nowplaying.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsLyricCommon {

    private String lyricSource = "netease";

    private Boolean autoSelectBestLyric = true;

}
