package com.widdit.nowplaying.service.kugou;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.InflaterInputStream;

/**
 * 原作者：WXRIW
 * 代码链接：https://github.com/WXRIW/Lyricify-Lyrics-Helper/blob/master/Lyricify.Lyrics.Helper/Decrypter/Krc/Decrypter.cs
 * Licensed under the Apache License, Version 2.0
 *
 * 修改者：Widdit
 */
public class Decrypter {

    protected static final byte[] DECRYPT_KEY = {
        (byte) 0x40, (byte) 0x47, (byte) 0x61, (byte) 0x77,
        (byte) 0x5e, (byte) 0x32, (byte) 0x74, (byte) 0x47,
        (byte) 0x51, (byte) 0x36, (byte) 0x31, (byte) 0x2d,
        (byte) 0xce, (byte) 0xd2, (byte) 0x6e, (byte) 0x69
    };

    /**
     * 解密 KRC 歌词
     *
     * @param encryptedLyrics 加密的歌词
     * @return 解密后的 KRC 歌词
     */
    public static String decryptLyrics(String encryptedLyrics) throws IOException {
        // Base64 解码后跳过前 4 个字节
        byte[] decoded = Base64.getDecoder().decode(encryptedLyrics);
        byte[] data = new byte[decoded.length - 4];
        System.arraycopy(decoded, 4, data, 0, data.length);

        // 异或解密
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (data[i] ^ DECRYPT_KEY[i % DECRYPT_KEY.length]);
        }

        // 解压并转换为字符串，跳过第一个字符
        String res = new String(decompress(data), StandardCharsets.UTF_8);
        return res.substring(1);
    }

    /**
     * 使用 Inflate 算法解压数据
     *
     * @param data 压缩的字节数组
     * @return 解压后的字节数组
     */
    protected static byte[] decompress(byte[] data) throws IOException {
        ByteArrayInputStream compressed = new ByteArrayInputStream(data);
        ByteArrayOutputStream decompressed = new ByteArrayOutputStream();

        try (InflaterInputStream inflaterInputStream = new InflaterInputStream(compressed)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inflaterInputStream.read(buffer)) != -1) {
                decompressed.write(buffer, 0, len);
            }
        }

        return decompressed.toByteArray();
    }
}
