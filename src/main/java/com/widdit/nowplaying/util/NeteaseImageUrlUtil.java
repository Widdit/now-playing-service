package com.widdit.nowplaying.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class NeteaseImageUrlUtil {

    /**
     * 生成网易云音乐封面图片 URL
     *
     * URL格式：https://p1.music.126.net/{enc}/{picId}.jpg
     *
     * @param picId 网易云音乐封面图片 ID
     * @return 封面 URL
     */
    public static String buildCoverUrl(String picId) {
        String enc = encryptId(picId);
        return "https://p1.music.126.net/" + enc + "/" + picId + ".jpg";
    }

    /**
     * 生成指定尺寸的网易云音乐封面图片 URL
     *
     * @param picId 网易云音乐封面图片 ID
     * @param size 封面图片的尺寸
     * @return 带尺寸参数的封面 URL
     */
    public static String buildCoverUrl(String picId, int size) {
        return buildCoverUrl(picId) + "?param=" + size + "y" + size;
    }

    /**
     * 对网易云音乐的 picId 进行加密处理
     *
     * 核心逻辑：使用固定魔法字符串异或原始 ID，再进行 MD5 哈希，最后 Base64 编码并替换特殊字符
     *
     * @param id 需要加密的原始 picId
     * @return 加密后的字符串（用于拼接封面 URL 的 enc 部分）
     */
    public static String encryptId(String id) {
        byte[] magic = "3go8&$8*3*3h0k(2)2".getBytes(StandardCharsets.UTF_8);
        byte[] bytes = id.getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (bytes[i] ^ magic[i % magic.length]);
        }

        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(bytes);

            String b64 = Base64.getEncoder().encodeToString(digest);
            return b64.replace("/", "_").replace("+", "-");
        } catch (Exception e) {
            throw new RuntimeException("获取网易云音乐封面 URL 的 encryptId 失败：", e);
        }
    }
}
