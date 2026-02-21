package com.widdit.nowplaying.service.qq;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;

/**
 * 原作者：WXRIW
 * 代码链接：https://github.com/WXRIW/Lyricify-Lyrics-Helper/blob/master/Lyricify.Lyrics.Helper/Decrypter/Qrc/Decrypter.cs
 * Licensed under the Apache License, Version 2.0
 *
 * 修改者：Widdit
 */
public class Decrypter {

    private static final byte[] QQ_KEY = "!@#)(*$%123ZXC!@!@#)(NHL".getBytes(StandardCharsets.US_ASCII);

    private static final Pattern AMP_REGEX = Pattern.compile("&(?![a-zA-Z]{2,6};|#[0-9]{2,4};)");
    private static final Pattern QUOT_REGEX = Pattern.compile(
            "(\\s+[\\w:.-]+\\s*=\\s*\")(([^\"]*)((\")((?!\\s+[\\w:.-]+\\s*=\\s*\"|\\s*(?:/?|\\?)>))[^\"]*)*)\"");

    /**
     * 解密 QRC 歌词
     */
    public static String decryptLyrics(String encryptedLyrics) {
        try {
            byte[] encryptedTextByte = hexStringToByteArray(encryptedLyrics);
            byte[] data = new byte[encryptedTextByte.length];

            byte[][][] schedule = new byte[3][16][6];

            DESHelper.tripleDESKeySetup(QQ_KEY, schedule, DESHelper.DECRYPT);

            for (int i = 0; i < encryptedTextByte.length; i += 8) {
                byte[] temp = new byte[8];
                byte[] inputBlock = Arrays.copyOfRange(encryptedTextByte, i, i + 8);
                DESHelper.tripleDESCrypt(inputBlock, temp, schedule);
                System.arraycopy(temp, 0, data, i, 8);
            }

            byte[] unzip = decompress(data);

            // 移除字符串头部的 BOM 标识 (如果有)
            byte[] utf8Bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            if (unzip.length >= 3 && unzip[0] == utf8Bom[0] && unzip[1] == utf8Bom[1] && unzip[2] == utf8Bom[2]) {
                unzip = Arrays.copyOfRange(unzip, 3, unzip.length);
            }

            return new String(unzip, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] decompress(byte[] data) throws Exception {
        Inflater inflater = new Inflater();
        inflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];

        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }

        outputStream.close();
        inflater.end();

        return outputStream.toByteArray();
    }

    private static byte[] hexStringToByteArray(String hexString) {
        int length = hexString.length();
        byte[] bytes = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(hexString.substring(i, i + 2), 16);
        }
        return bytes;
    }

    // ==================== XML 工具方法 ====================

    /**
     * 创建 XML DOM
     */
    public static Document createXmlDocument(String content) throws Exception {
        content = removeIllegalContent(content);
        content = replaceAmp(content);
        String contentFixed = replaceQuot(content);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        try {
            return builder.parse(new ByteArrayInputStream(contentFixed.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return builder.parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        }
    }

    private static String replaceAmp(String content) {
        return AMP_REGEX.matcher(content).replaceAll("&amp;");
    }

    private static String replaceQuot(String content) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = QUOT_REGEX.matcher(content);
        int currentPos = 0;

        while (matcher.find()) {
            sb.append(content, currentPos, matcher.start());
            String replacement = matcher.group(1) + matcher.group(2).replace("\"", "&quot;") + "\"";
            sb.append(replacement);
            currentPos = matcher.end();
        }

        sb.append(content.substring(currentPos));
        return sb.toString();
    }

    private static String removeIllegalContent(String content) {
        int left = 0, i = 0;
        while (i < content.length()) {
            if (content.charAt(i) == '<') {
                left = i;
            }

            if (i > 0 && content.charAt(i) == '>' && content.charAt(i - 1) == '/') {
                String part = content.substring(left, i + 1);

                if (part.contains("=") && part.indexOf("=") == part.lastIndexOf("=")) {
                    String part1 = content.substring(left, left + part.indexOf("="));
                    if (!part1.trim().contains(" ")) {
                        content = content.substring(0, left) + content.substring(i + 1);
                        i = 0;
                        continue;
                    }
                }
            }
            i++;
        }
        return content.trim();
    }

    /**
     * 递归查找 XML DOM
     */
    public static void recursionFindElement(Node xmlNode, Map<String, String> mappingDict, Map<String, Node> resDict) {
        String nodeName = xmlNode.getNodeName();
        if (mappingDict.containsKey(nodeName)) {
            resDict.put(mappingDict.get(nodeName), xmlNode);
        }

        if (!xmlNode.hasChildNodes()) {
            return;
        }

        NodeList childNodes = xmlNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            recursionFindElement(childNodes.item(i), mappingDict, resDict);
        }
    }

    /**
     * 获取节点的文本内容
     */
    public static String getNodeText(Node node) {
        if (node == null) return null;
        return node.getTextContent();
    }

    /**
     * 获取节点的属性值
     */
    public static String getNodeAttribute(Node node, String attrName) {
        if (node == null || node.getAttributes() == null) return null;
        Node attr = node.getAttributes().getNamedItem(attrName);
        return attr != null ? attr.getTextContent() : null;
    }
}
