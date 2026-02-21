package com.widdit.nowplaying.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsLyric {

    private Long lastUpdateTime = 0L;

    private String primaryFont = "MicrosoftYaHei";

    private String fallbackFont = "MicrosoftYaHei";

    private String fontSize = "medium";

    private Double letterSpacing = 0.0;

    private Boolean boldEnabled = false;

    private Boolean shadowEnabled = false;

    private Integer shadowXOffset = 2;

    private Integer shadowYOffset = 2;

    private Integer shadowBlur = 3;

    private String shadowColor = "rgba(17, 255, 255, 0.9)";

    private Boolean strokeEnabled = false;

    private String strokeColor = "rgba(163, 99, 217, 1)";

    private Double alignPosition = 0.5;

    private Boolean hidePassedLines = false;

    private Boolean backgroundEnabled = true;

    private String backgroundRenderer = "MeshGradientRenderer";

    private String color = "rgba(255, 255, 255, 1)";

    private Boolean blurEffectEnabled = true;

    private Boolean scaleEffectEnabled = true;

    private Boolean springAnimationEnabled = true;

    private String textAlign = "left";

    private Integer interludeDotsPosition = 0;

    private Boolean showTranslation = true;

    private Double subLineFontSize = 0.5;

    private Boolean karaokeLyricEnabled = true;

    private Integer timeOffset = 0;

    private Boolean showTitleWhenNoLyric = false;

    private String noLyricText = "纯音乐，请欣赏";

    private Double opacity = 1.0;

    private Double brightness = 1.0;

    private Double contrast = 1.0;

    private Double saturate = 1.0;

    private Integer translateX = 0;

    private Integer translateY = 0;

    private Integer perspective = 800;

    private Integer rotateX = 0;

    private Integer rotateY = 0;

}
