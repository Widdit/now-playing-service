package com.widdit.nowplaying.service.wesing;

import com.widdit.nowplaying.entity.Lyric;
import com.widdit.nowplaying.service.qq.Decrypter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class WeSingHelper {

    // QRC 头部长度
    private static final int QRC_HEADER_LENGTH = 11;

    // QRC 逐字歌词的时间格式: [startMs,durationMs]
    private static final Pattern QRC_TIME_PATTERN = Pattern.compile("^\\[(\\d+),(\\d+)]");

    // 标准 LRC 时间格式: [MM:SS.ms]
    private static final Pattern LRC_TIME_PATTERN = Pattern.compile("\\[(\\d{2}):(\\d{2})[.](\\d{2,3})]");

    /**
     * 解密本地 .qrc 文件
     */
    public static String decryptLocalQrc(File qrcFile) {
        try {
            byte[] fileBytes = java.nio.file.Files.readAllBytes(qrcFile.toPath());

            int offset = 0;
            if (fileBytes.length > QRC_HEADER_LENGTH) {
                String header = new String(fileBytes, 0, QRC_HEADER_LENGTH);
                if (header.startsWith("[offset:")) {
                    for (int i = 0; i < Math.min(fileBytes.length, 50); i++) {
                        if (fileBytes[i] == '\n') {
                            offset = i + 1;
                            break;
                        }
                    }
                }
            }

            byte[] dataBytes = Arrays.copyOfRange(fileBytes, offset, fileBytes.length);
            StringBuilder hexString = new StringBuilder(dataBytes.length * 2);
            for (byte b : dataBytes) {
                hexString.append(String.format("%02x", b & 0xFF));
            }

            return Decrypter.decryptLyrics(hexString.toString());
        } catch (Exception e) {
            log.error("解密 QRC 文件失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 根据 QRC XML 生成 Lyric 对象
     * 注意: LyricContent 作为 XML 属性值时，换行符会被 DOM 解析器规范化为空格，
     * 因此直接从原始 XML 字符串中用正则提取 LyricContent，保留原始换行。
     */
    public static Lyric buildLyric(String qrcXml, String songTitle) {
        Lyric lyric = new Lyric();
        lyric.setSource("wesing");
        lyric.setTitle(songTitle);

        try {
            // 从原始 XML 字符串中直接提取 LyricContent（避免 DOM 属性换行规范化问题）
            String content = extractLyricContent(qrcXml);

            if (content != null && !content.isEmpty()) {
                lyric.setKaraokeLyric(content);
                lyric.setHasKaraokeLyric(true);

                // 提取歌手名
                String author = extractAuthorFromLyricContent(content);
                lyric.setAuthor(author);

                String lrc = convertQrcToLrc(content);
                if (lrc != null && !lrc.isEmpty()) {
                    lyric.setLrc(lrc);
                    lyric.setHasLyric(true);
                }
            }

            // 提取歌曲时长
            int duration = extractDurationFromQrcXml(qrcXml);
            lyric.setDuration(duration);

            // 翻译歌词仍用 DOM 提取（无换行问题）
            Document doc = Decrypter.createXmlDocument(qrcXml);
            Map<String, String> mappingDict = new HashMap<>();
            mappingDict.put("BDLyric", "translatedLyric");
            Map<String, Node> resDict = new HashMap<>();
            Decrypter.recursionFindElement(doc.getDocumentElement(), mappingDict, resDict);

            Node translatedNode = resDict.get("translatedLyric");
            if (translatedNode != null) {
                String translatedContent = Decrypter.getNodeText(translatedNode);
                if (translatedContent != null && !translatedContent.isEmpty()) {
                    lyric.setTranslatedLyric(translatedContent);
                    lyric.setHasTranslatedLyric(true);
                }
            }

        } catch (Exception e) {
            log.error("解析 QRC XML 失败: {}", e.getMessage());
            if (qrcXml.contains("[") && qrcXml.contains("]")) {
                lyric.setLrc(qrcXml);
                lyric.setHasLyric(true);
            }
        }

        return lyric;
    }

    /**
     * 从原始 QRC XML 字符串中提取 LyricContent 属性值
     * 直接用字符串查找，避免 DOM 解析器将属性中的换行符规范化为空格
     * 使用转义感知的引号查找，处理歌词中可能包含 &quot; 转义引号的情况
     */
    public static String extractLyricContent(String qrcXml) {
        String marker = "LyricContent=\"";
        int start = qrcXml.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();

        // 转义感知的结束引号查找：跳过被转义的引号（如 &quot;）
        int pos = start;
        while (pos < qrcXml.length()) {
            int quotePos = qrcXml.indexOf("\"", pos);
            if (quotePos < 0) {
                return null;
            }
            // 检查这个引号前面是否是 &quot 的一部分（即 &quot;）
            // &quot; 中 " 前面紧跟 &quot 共5个字符
            if (quotePos >= 5 && qrcXml.substring(quotePos - 5, quotePos + 1).equals("&quot;")) {
                pos = quotePos + 1;
                continue;
            }
            return qrcXml.substring(start, quotePos);
        }

        return null;
    }

    /**
     * 将 QRC 逐字歌词转换为标准 LRC 歌词
     * QRC 格式: [startMs,durationMs]字(charStartMs,charDuration)字(charStartMs,charDuration)...
     * LRC 格式: [MM:SS.xx]歌词文本
     */
    public static String convertQrcToLrc(String qrcContent) {
        if (qrcContent == null || qrcContent.isEmpty()) {
            return null;
        }

        StringBuilder lrc = new StringBuilder();
        String[] lines = qrcContent.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 跳过元信息行 [ti:] [ar:] [al:] [by:] [offset:]
            if (line.startsWith("[ti:") || line.startsWith("[ar:") ||
                    line.startsWith("[al:") || line.startsWith("[by:") ||
                    line.startsWith("[offset:")) {
                continue;
            }

            // 先尝试匹配 QRC 毫秒格式 [startMs,durationMs]
            Matcher qrcMatcher = QRC_TIME_PATTERN.matcher(line);
            if (qrcMatcher.find()) {
                int startMs = Integer.parseInt(qrcMatcher.group(1));
                String timeTag = msToLrcTime(startMs);

                // 提取歌词文本：去掉行首的 [ms,ms]，再去掉逐字时间标签 (ms,ms)
                String text = line.substring(qrcMatcher.end());
                text = text.replaceAll("\\(\\d+,\\d+\\)", "");
                text = text.trim();

                if (!text.isEmpty()) {
                    lrc.append(timeTag).append(text).append("\n");
                }
                continue;
            }

            // 再尝试匹配标准 LRC 格式 [MM:SS.ms]
            Matcher lrcMatcher = LRC_TIME_PATTERN.matcher(line);
            if (lrcMatcher.find()) {
                String timeTag = lrcMatcher.group(0);
                String text = line.substring(lrcMatcher.end());
                text = text.replaceAll("\\(\\d+,\\d+\\)", "");
                text = text.replaceAll("\\[\\d+,\\d+]", "");
                text = text.trim();

                if (!text.isEmpty()) {
                    lrc.append(timeTag).append(text).append("\n");
                }
            }
        }

        return lrc.toString();
    }

    /**
     * 将毫秒转换为 LRC 时间标签 [MM:SS.xx]
     */
    public static String msToLrcTime(int ms) {
        int totalSec = ms / 1000;
        int minutes = totalSec / 60;
        int seconds = totalSec % 60;
        int hundredths = (ms % 1000) / 10;
        return String.format("[%02d:%02d.%02d]", minutes, seconds, hundredths);
    }

    /**
     * 从 LyricContent 中提取歌手名
     * @param content LyricContent 内容
     * @return 歌手名
     */
    private static String extractAuthorFromLyricContent(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        // 匹配 [ar:xxx] 格式，提取歌手名
        Pattern pattern = Pattern.compile("\\[ar:([^\\]]*)\\]");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String author = matcher.group(1);
            return author != null ? author.trim() : "";
        }

        return "";
    }

    /**
     * 从 QRC XML 中提取歌曲时长
     * @param qrcXml QRC XML 字符串
     * @return 歌曲时长。如果不存在或小于 30 则返回兜底值 300
     */
    private static int extractDurationFromQrcXml(String qrcXml) {
        final int DEFAULT_DURATION = 300;

        if (qrcXml == null || qrcXml.isEmpty()) {
            return DEFAULT_DURATION;
        }

        try {
            // 匹配 SaveTime="xxx" 格式，提取时长
            Pattern pattern = Pattern.compile("SaveTime=\"(\\d+)\"");
            Matcher matcher = pattern.matcher(qrcXml);

            if (matcher.find()) {
                int duration = Integer.parseInt(matcher.group(1));
                if (duration < 30) {
                    return DEFAULT_DURATION;
                }
                return duration;
            }
        } catch (Exception e) {}

        return DEFAULT_DURATION;
    }
    
}
