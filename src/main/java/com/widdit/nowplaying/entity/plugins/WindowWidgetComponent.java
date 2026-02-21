package com.widdit.nowplaying.entity.plugins;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WindowWidgetComponent {

    private Boolean enabled = false;

    private Integer width = 800;

    private Integer height = 600;

}
