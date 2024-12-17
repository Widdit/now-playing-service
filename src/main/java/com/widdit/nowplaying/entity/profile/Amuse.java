package com.widdit.nowplaying.entity.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Amuse {

    private String music_service = "youtube";

    private List<Profile> profiles;

}
