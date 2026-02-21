package com.widdit.nowplaying.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogStatus {

    private Boolean mainLogExist = false;

    private Boolean desktopLogExist = false;

    private Boolean virtualCameraLogExist = false;

}
