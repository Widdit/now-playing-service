package com.widdit.nowplaying.entity.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionInfo {

    private String latestVersion;

    private String updateDate;

    private String updateLog;

}
