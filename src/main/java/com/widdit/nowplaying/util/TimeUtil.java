package com.widdit.nowplaying.util;

public class TimeUtil {

    /**
     * 计算出格式化的时长
     * @param duration
     * @return
     */
    public static String getFormattedDuration(Integer duration) {
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

}
