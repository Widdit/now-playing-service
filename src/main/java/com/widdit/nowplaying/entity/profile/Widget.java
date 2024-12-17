package com.widdit.nowplaying.entity.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Widget {

    private Amuse amuse;

    private String widget_token = "cc8d2f328c7bab6069bbc77245ab1c43461148e8552a60b77bab0e35296a29c5";

}
