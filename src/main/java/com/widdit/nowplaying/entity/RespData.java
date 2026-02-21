package com.widdit.nowplaying.entity;

import lombok.*;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class RespData<T> {

    private T data;

}
