package com.widdit.nowplaying.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    private Boolean hasSong = false;

    private Boolean isPaused = true;

    private Double volumePercent = 0.0;

    private Integer seekbarCurrentPosition = 0;

    private String seekbarCurrentPositionHuman = "0:00";

    private Double statePercent = 0.0;

    private String likeStatus = "INDIFFERENT";

    private String repeatType = "NONE";

}