package com.widdit.nowplaying.service.netease;

import lombok.NonNull;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.util.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 网易云 API 三种模式加密解密
 * 代码来源：https://github.com/jnwang95/music-java-api
 */
public class CryptoUtil {

    private static final String presetKey = "0CoJUm6Qyw8W8jud";
    private static final String pubKey = "010001";
    private static final String modulus = "00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7";
    private static final String iv = "0102030405060708";
    private static final String eapiKey = "e82ckenh8dichen8";

    /**
     * 产生16位的随机字符串
     */
    public static String createSecretKey() {

        String keys = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            double index = Math.floor(Math.random() * keys.length());
            key.append(keys.charAt((int) index));
        }
        return key.toString();
    }

    /**
     * aes加密
     *
     */
    private static String aesEncrypt(@NonNull String content,@NonNull String key, AesEncryptEnum aesEncryptEnum, String iv) {

        String result = null;
        try {
            Cipher cipher = Cipher.getInstance("AES/" + aesEncryptEnum.getType() + "/PKCS5Padding");
            byte[] bytes;

            switch (aesEncryptEnum) {
                case CBC:
                    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES"),
                            new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8)));
                    bytes = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
                    result = Base64.getEncoder().encodeToString(bytes);
                    break;
                case ECB:
                    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES"));
                    bytes = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
                    result = HexUtils.toHexString(bytes);
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }

    /**
     * 长度不够前面补充0
     */
    private static String zFill(String str) {
        StringBuilder strBuilder = new StringBuilder(str);
        while (strBuilder.length() < 256) {
            strBuilder.insert(0, "0");
        }
        str = strBuilder.toString();
        return str;
    }

    /**
     * rsa加密
     */
    private static String rsaEncrypt(String text) {

        // 反转字符串
        text = new StringBuffer(text).reverse().toString();

        BigInteger biText = new BigInteger(strToHex(text), 16);
        BigInteger biEx = new BigInteger(CryptoUtil.pubKey, 16);
        BigInteger biMod = new BigInteger(CryptoUtil.modulus, 16);
        BigInteger biRet = biText.modPow(biEx, biMod);

        return zFill(biRet.toString(16));

    }

    /**
     * 字符串转成16进制字符串
     */
    public static String strToHex(String s) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str.append(s4);
        }
        return str.toString();
    }

    /**
     * 5 加密方法
     */
    public static String eapiEncrypt(String url,String content) {

        String message = "nobody" + url + "use" + content + "md5forencrypt";
        String digest = getMd5(message);
        String data = url + "-36cd479b6b5-" + content + "-36cd479b6b5-" + digest;
        return aesEncrypt(data, eapiKey, AesEncryptEnum.ECB, "").toUpperCase();
    }

    /**
     * 5 加密方法
     */
    public static String[] weapiEncrypt(String content) {
        String[] result = new String[2];
        String key = createSecretKey();

        String encText = aesEncrypt(aesEncrypt(content, presetKey, AesEncryptEnum.CBC, iv), key, AesEncryptEnum.CBC,
                iv);
        String encSecKey = rsaEncrypt(key);
        result[0] = encText;
        result[1] = encSecKey;
        return result;
    }

    /**
     * MD5加密
     *
     */
    public static String getMd5(String content) {
        String result = null;
        try {
            result = DigestUtils.md5DigestAsHex(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
