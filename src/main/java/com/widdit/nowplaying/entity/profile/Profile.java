package com.widdit.nowplaying.entity.profile;

import com.widdit.nowplaying.entity.Settings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    private String profile_name;

    private Settings profile_settings;

    private String _id;

}
