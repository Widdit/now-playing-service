package com.widdit.nowplaying.service.netease;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AesEncryptEnum {

    CBC("CBC"), ECB("ECB");

    private final String type;

}
