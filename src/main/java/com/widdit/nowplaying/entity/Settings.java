package com.widdit.nowplaying.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Settings {

    private Boolean cover_blur = false;
    
    private Boolean cover_glow = false;
    
    private Boolean hide_on_pause = false;
    
    private Boolean song_change_only = false;
    
    private Integer visible_duration = 5;
    
    private Boolean player_colors = true;
    
    private Boolean hide_equalizer = false;
    
    private Integer cover = 0;
    
    private Boolean first_login = false;
    
    private Integer hide_delay = 10;
    
    private Boolean is_demo = false;
    
    private Integer player = 0;
    
    private Integer theme = 0;
    
    private String tint_color = "#40a4d8";
    
}
