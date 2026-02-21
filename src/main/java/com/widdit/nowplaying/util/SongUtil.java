package com.widdit.nowplaying.util;

public class SongUtil {

    /**
     * 将窗口标题解析为单独的歌名和歌手名
     * @param windowTitle 窗口标题
     * @return 字符串数组，第一个元素是歌名，第二个元素是歌手名
     */
    public static String[] parseWindowTitle(String windowTitle) {
        String pivot = " - ";
        String title;
        String author;

        if (windowTitle.contains(pivot)) {
            int pos = windowTitle.lastIndexOf(pivot);
            title = windowTitle.substring(0, pos).trim();
            author = windowTitle.substring(pos + pivot.length()).trim();
        } else {
            title = windowTitle;
            author = "";
        }

        return new String[] {title, author};
    }

}
