package com.widdit.nowplaying.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Track {

    private String author = "";

    private String title = "";

    private String album = "";

    private String cover = "";

    private Integer duration = 0;

    private String durationHuman = "0:00";

    private String url = "";

    private String id = "";

    private Boolean isVideo = false;

    private Boolean isAdvertisement = false;

    private Boolean inLibrary = false;

}