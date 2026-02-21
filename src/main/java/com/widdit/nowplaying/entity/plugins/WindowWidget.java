package com.widdit.nowplaying.entity.plugins;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WindowWidget {

    private WindowWidgetComponent songWindow = new WindowWidgetComponent();

    private WindowWidgetComponent lyricWindow = new WindowWidgetComponent();

    private WindowWidgetComponent customWindow = new WindowWidgetComponent();

}
