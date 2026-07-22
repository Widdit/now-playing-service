package com.widdit.nowplaying.util;

public class SongUtil {

    /**
     * 构造去除括号译名/注释后的回退搜索词，同时保留歌手信息。
     */
    public static String buildSearchKeywordWithoutAnnotations(String windowTitle) {
        if (windowTitle == null || windowTitle.isBlank()) {
            return "";
        }

        String[] parsed = parseWindowTitle(windowTitle);
        String title = parsed[0]
                .replace('（', '(').replace('）', ')')
                .replace('[', '(').replace(']', ')')
                .replace('【', '(').replace('】', ')')
                .replace('「', '(').replace('」', ')')
                .replace('『', '(').replace('』', ')')
                .replace('〔', '(').replace('〕', ')')
                .replace('〈', '(').replace('〉', ')');
        String baseTitle = title.replaceAll("\\s*\\([^)]*\\)\\s*", " ")
                .trim()
                .replaceAll("\\s+", " ");

        if (baseTitle.isEmpty() || baseTitle.equals(parsed[0])) {
            return windowTitle;
        }
        return parsed[1].isEmpty() ? baseTitle : baseTitle + " - " + parsed[1];
    }

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
