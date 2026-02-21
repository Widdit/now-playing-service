package com.widdit.nowplaying.entity.plugins;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VirtualCamera {

    private Boolean enabled = false;

    private Boolean installed = false;

    private String content = "song";

    private String resolution = "720p";

    private Integer width = 1280;

    private Integer height = 720;

    private Integer fps = 60;

}
