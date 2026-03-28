package com.widdit.nowplaying.service.qq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrcLyric {

    private String qrc = "";

    private String trans = "";

}
