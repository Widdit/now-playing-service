package com.widdit.nowplaying.entity.cmd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Option {

    // 命令行参数名称（需包含 "--" 前缀）
    private String name;

    // 命令行参数值
    private String value;

}
