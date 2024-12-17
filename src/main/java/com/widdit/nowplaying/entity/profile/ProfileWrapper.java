package com.widdit.nowplaying.entity.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileWrapper {

    private Widget widgets;

    private String _id = "677c8a15a20ee438fe1a7a92";

    private String avatar = "https://gitee.com/widdit/now-playing/raw/master/default-avatar.jpg";

    private Boolean has_donated = true;

    private Boolean is_discord_member = true;

    private Boolean is_subscribed = true;

    private String name = "Admin";

    private String creationDate = "2024-05-01T00:00:00.000Z";

}
