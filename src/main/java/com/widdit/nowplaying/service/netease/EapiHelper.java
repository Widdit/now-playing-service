package com.widdit.nowplaying.service.netease;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * 改写自 https://github.com/WXRIW/Lyricify-Lyrics-Helper/blob/master/Lyricify.Lyrics.Helper/Providers/Web/Netease/EapiHelper.cs
 */
public class EapiHelper {
    private static final String userAgent = "Mozilla/5.0 (Linux; Android 9; PCT-AL10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.64 HuaweiBrowser/10.0.3.311 Mobile Safari/537.36";
    private static final byte[] eapiKey = "e82ckenh8dichen8".getBytes(StandardCharsets.US_ASCII);
    private static final Gson gson = new Gson();

    static {
        // 关闭 org.apache.http 和 Wire 日志
        Logger httpLogger = (Logger) LoggerFactory.getLogger("org.apache.http");
        httpLogger.setLevel(Level.OFF);
        Logger wireLogger = (Logger) LoggerFactory.getLogger("org.apache.http.wire");
        wireLogger.setLevel(Level.OFF);
    }

    public static String post(String url, Map<String, String> data) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", userAgent);
        headers.put("Referer", "https://music.163.com/");

        Map<String, String> header = new HashMap<>();
        header.put("__csrf", "");
        header.put("appver", "8.0.0");
        header.put("buildver", String.valueOf(getCurrentTotalSeconds()));
        header.put("channel", "");
        header.put("deviceId", "");
        header.put("mobilename", "");
        header.put("resolution", "1920x1080");
        header.put("os", "android");
        header.put("osver", "");
        header.put("requestId", getCurrentTotalMilliseconds() + "_" + String.format("%04d", new Random().nextInt(1000)));
        header.put("versioncode", "140");
        header.put("MUSIC_U", "");

        List<String> cookies = new ArrayList<>();
        for (Map.Entry<String, String> entry : header.entrySet()) {
            cookies.add(entry.getKey() + "=" + entry.getValue());
        }
        headers.put("Cookie", String.join("; ", cookies));
        data.put("header", gson.toJson(header));

        Map<String, String> encryptedData = eApi(url, data);
        url = url.replaceAll("\\w*api", "eapi");

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);

            // 设置请求头
            headers.forEach(httpPost::setHeader);

            // 构建表单参数
            List<org.apache.http.NameValuePair> params = new ArrayList<>();
            encryptedData.forEach((k, v) -> params.add(new org.apache.http.message.BasicNameValuePair(k, v)));
            httpPost.setEntity(new org.apache.http.client.entity.UrlEncodedFormEntity(params, "UTF-8"));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, "UTF-8");
            }
        }
    }

    private static long getCurrentTotalSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    private static String getCurrentTotalMilliseconds() {
        return String.valueOf(System.currentTimeMillis());
    }

    private static Map<String, String> eApi(String url, Map<String, String> data) throws Exception {
        url = url.replace("https://interface3.music.163.com/e", "/")
                .replace("https://interface.music.163.com/e", "/");
        String text = gson.toJson(data);
        String message = "nobody" + url + "use" + text + "md5forencrypt";
        String digest = md5(message.getBytes(StandardCharsets.UTF_8));
        String dataStr = url + "-36cd479b6b5-" + text + "-36cd479b6b5-" + digest;
        byte[] encrypted = aesEncrypt(dataStr.getBytes(StandardCharsets.UTF_8), eapiKey);
        return Collections.singletonMap("params", bytesToHex(encrypted).toUpperCase());
    }

    private static byte[] aesEncrypt(byte[] data, byte[] key) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] cipherBuffer) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(eapiKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(cipherBuffer);
    }

    private static String md5(byte[] input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input);
        return bytesToHex(digest);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String buildFormData(Map<String, String> params) throws Exception {
        StringBuilder formData = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (formData.length() != 0) formData.append("&");
            formData.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return formData.toString();
    }
}
