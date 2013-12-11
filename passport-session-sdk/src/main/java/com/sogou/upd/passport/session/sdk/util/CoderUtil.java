package com.sogou.upd.passport.session.sdk.util;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;

/**
 * 各种加密算法
 * 包括：URLEncode、MD5、SHA、HMAC
 */
public class CoderUtil {
    private static Logger logger = LoggerFactory.getLogger(CoderUtil.class);

    public static final String KEY_SHA = "SHA";
    public static final String KEY_MD5 = "MD5";

    private static ThreadLocal<MessageDigest> mdThreadLocal = new ThreadLocal<MessageDigest>();

    public static MessageDigest getMD() {
        MessageDigest md = mdThreadLocal.get();
        if (md == null) {
            try {
                md = MessageDigest.getInstance(KEY_MD5);
                mdThreadLocal.set(md);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return md;
    }


    /**
     * MAC算法可选以下多种算法
     * <p/>
     * <pre>
     * HmacMD5
     * HmacSHA1
     * HmacSHA256
     * HmacSHA384
     * HmacSHA512
     * </pre>
     */
    public static final String KEY_MAC = "HmacSHA1";

    /**
     * 将参数URLEncode为UTF-8
     */
    public static String encodeUTF8(String params) {
        try {
            String en = URLEncoder.encode(params, CommonConfigUtil.DEFAULT_CONTENT_CHARSET);
            en = en.replace("+", "%20");
            en = en.replace("*", "%2A");
            return en;
        } catch (UnsupportedEncodingException problem) {
            throw new IllegalArgumentException(problem);
        }
    }

    /**
     * 将参数URLEncode，默认为UTF-8
     */
    public static String encode(String params, String charset) {
        try {
            String en = URLEncoder.encode(params, charset != null ? charset : CommonConfigUtil.DEFAULT_CONTENT_CHARSET);
            en = en.replace("+", "%20");
            en = en.replace("*", "%2A");
            return en;
        } catch (UnsupportedEncodingException problem) {
            throw new IllegalArgumentException(problem);
        }
    }

    /**
     * BASE64解密
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] decryptBASE64(String key) {
        return Base64.decodeBase64(key);
    }

    /**
     * 解码base64
     * @param key
     * @return
     */
    public  static String decodeBASE64(String key){
        byte[] bytes= Base64.decodeBase64(key);
        return  new String(bytes);
    }


    /**
     * BASE64加密
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static String encryptBase64URLSafeString(byte[] key) throws Exception {
        return Base64.encodeBase64URLSafeString(key);
    }

    /**
     * MD5加密
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String encryptMD5(String data) throws Exception {

        // MessageDigest md5 = MessageDigest.getInstance(KEY_MD5);
        MessageDigest md5 = getMD();
        md5.reset();
        md5.update(data.getBytes());

        return toHexString(md5.digest());

    }

    /**
     * MD5加密
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] encryptMD5_Byte(String data) throws Exception {

        // MessageDigest md5 = MessageDigest.getInstance(KEY_MD5);
        MessageDigest md5 = getMD();
        md5.reset();
        md5.update(data.getBytes(CommonConfigUtil.DEFAULT_CONTENT_CHARSET));
        return md5.digest();
    }

    /**
     * MD5加密
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String encryptMD5GBK(String data) throws Exception {

        MessageDigest md5 = MessageDigest.getInstance(KEY_MD5);

        md5.update(data.getBytes("GBK"));

        return toHexString(md5.digest());

    }


    /**
     * SHA加密
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] encryptSHA(byte[] data) throws Exception {

        MessageDigest sha = MessageDigest.getInstance(KEY_SHA);
        sha.update(data);

        return sha.digest();

    }

    /**
     * 初始化HMAC密钥
     *
     * @return
     * @throws Exception
     */
    public static String initMacKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_MAC);

        SecretKey secretKey = keyGenerator.generateKey();
        return encryptBase64URLSafeString(secretKey.getEncoded());
    }

    /**
     * HMAC加密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] encryptHMAC(String data, byte[] key) throws Exception {

        SecretKey secretKey = new SecretKeySpec(key, KEY_MAC);
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        mac.init(secretKey);

        return mac.doFinal(data.getBytes(CommonConfigUtil.DEFAULT_CONTENT_CHARSET));

    }

    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte by : b) {
            sb.append(HEXCHAR[(by & 0xf0) >>> 4]);
            sb.append(HEXCHAR[by & 0x0f]);
        }
        return sb.toString();
    }

    public static byte[] toBytes(String s) {
        byte[] bytes;
        bytes = new byte[s.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    /**
     * 签名生成
     *
     * @param firstStr code算法第一个字符串，可能为userid、mobile、userid+mobile
     * @return
     * @throws Exception
     */
    public static String generatorCode(String firstStr, int clientId, String secret, long ct) {
        //计算默认的code
        String code = "";
        try {
            code = firstStr + clientId + secret + ct;
            code = encryptMD5(code);
        } catch (Exception e) {
            logger.error("calculate default code error", e);
        }
        return code;
    }

    private static char[] HEXCHAR = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
}
