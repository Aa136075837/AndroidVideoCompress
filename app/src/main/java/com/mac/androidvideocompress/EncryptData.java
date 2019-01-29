package com.mac.androidvideocompress;

import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.zip.CRC32;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class EncryptData {

    private static final String CHAT_SET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public EncryptData() {
    }

    public static byte[] SHA1(String text) throws Throwable {
        if(TextUtils.isEmpty(text)) {
            return null;
        } else {
            byte[] data = text.getBytes("utf-8");
            return SHA1(data);
        }
    }

    public static byte[] SHA1(byte[] data) throws Throwable {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(data);
        return md.digest();
    }

    public static byte[] SHA1(InputStream data) throws IOException, NoSuchAlgorithmException {
        if(data == null) {
            return null;
        } else {
            Object sha = null;

            byte[] sha1;
                byte[] t = new byte[1024];
                MessageDigest md = MessageDigest.getInstance("SHA-1");

                for(int len = data.read(t); len != -1; len = data.read(t)) {
                    md.update(t, 0, len);
                }

                sha1 = md.digest();

            return sha1;
        }
    }

    public static byte[] SHA1(File data) throws IOException, NoSuchAlgorithmException {
        if(data != null && data.exists()) {
            byte[] sha = null;


                FileInputStream e = new FileInputStream(data);
                sha = SHA1((InputStream)e);
                e.close();


            return sha;
        } else {
            return null;
        }
    }

    public static byte[] AES128Encode(String key, String text) throws Throwable {
        if(key != null && text != null) {
            byte[] keyBytes = key.getBytes("UTF-8");
            byte[] keyBytes16 = new byte[16];
            System.arraycopy(keyBytes, 0, keyBytes16, 0, Math.min(keyBytes.length, 16));
            byte[] data = text.getBytes("UTF-8");
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes16, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            cipher.init(1, keySpec);
            byte[] cipherText = new byte[cipher.getOutputSize(data.length)];
            int ctLength = cipher.update(data, 0, data.length, cipherText, 0);
            cipher.doFinal(cipherText, ctLength);
            return cipherText;
        } else {
            return null;
        }
    }

    public static byte[] AES128Encode(byte[] key, String text) throws Throwable {
        if(key != null && text != null) {
            byte[] data = text.getBytes("UTF-8");
            return AES128Encode(key, data);
        } else {
            return null;
        }
    }

    public static byte[] AES128Encode(byte[] key, byte[] data) throws Throwable {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
        cipher.init(1, keySpec);
        byte[] cipherText = new byte[cipher.getOutputSize(data.length)];
        int ctLength = cipher.update(data, 0, data.length, cipherText, 0);
        cipher.doFinal(cipherText, ctLength);
        return cipherText;
    }

    public static String AES128Decode(String key, byte[] cipherText) throws Throwable {
        if(key != null && cipherText != null) {
            byte[] keyBytes = key.getBytes("UTF-8");
            byte[] plainText = AES128Decode(keyBytes, cipherText);
            return new String(plainText, "UTF-8");
        } else {
            return null;
        }
    }

    public static byte[] AES128Decode(byte[] keyBytes, byte[] cipherText) throws Throwable {
        if(keyBytes != null && cipherText != null) {
            byte[] keyBytes16 = new byte[16];
            System.arraycopy(keyBytes, 0, keyBytes16, 0, Math.min(keyBytes.length, 16));
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes16, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "BC");
            cipher.init(2, keySpec);
            byte[] plainText = new byte[cipher.getOutputSize(cipherText.length)];
            int ptLength = cipher.update(cipherText, 0, cipherText.length, plainText, 0);
            ptLength += cipher.doFinal(plainText, ptLength);
            return plainText;
        } else {
            return null;
        }
    }

    public static String byteToHex(byte[] data) {
        return byteToHex(data, 0, data.length);
    }

    public static String byteToHex(byte[] data, int offset, int len) {
        StringBuffer buffer = new StringBuffer();
        if(data == null) {
            return buffer.toString();
        } else {
            for(int i = offset; i < len; ++i) {
                buffer.append(String.format("%02x", new Object[]{Byte.valueOf(data[i])}));
            }

            return buffer.toString();
        }
    }

    public static String base62(long value) {
        String result;
        int v;
        for(result = value == 0L?"0":""; value > 0L; result = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(v) + result) {
            v = (int)(value % 62L);
            value /= 62L;
        }

        return result;
    }

    public static String MD5(String data) throws IOException, NoSuchAlgorithmException {
        if(data == null) {
            return null;
        } else {
            byte[] tmp = rawMD5(data);
            return tmp == null?null:toHex(tmp);
        }
    }

    public static String MD5(byte[] data) throws IOException, NoSuchAlgorithmException {
        if(data == null) {
            return null;
        } else {
            byte[] tmp = rawMD5(data);
            return tmp == null?null:toHex(tmp);
        }
    }

    public static String MD5(File data) throws IOException, NoSuchAlgorithmException {
        if(data != null && data.exists()) {
            Object md5 = null;

            byte[] md51;

                FileInputStream e = new FileInputStream(data);
                md51 = rawMD5((InputStream)e);
                e.close();


            return md51 == null?null:toHex(md51);
        } else {
            return null;
        }
    }

    public static byte[] rawMD5(String data) throws IOException, NoSuchAlgorithmException {
        if(data == null) {
            return null;
        } else {
            Object md5 = null;

            byte[] md51;

                md51 = rawMD5(data.getBytes("utf-8"));


            return md51;
        }
    }

    public static byte[] rawMD5(byte[] data) throws IOException, NoSuchAlgorithmException {
        if(data == null) {
            return null;
        } else {
            Object md5 = null;

            byte[] md51;

                ByteArrayInputStream e = new ByteArrayInputStream(data);
                md51 = rawMD5((InputStream)e);
                e.close();


            return md51;
        }
    }

    public static byte[] rawMD5(InputStream data) throws NoSuchAlgorithmException, IOException {
        if(data == null) {
            return null;
        } else {
            Object md5 = null;

            byte[] md51;

                byte[] t = new byte[1024];
                MessageDigest md = MessageDigest.getInstance("MD5");

                for(int len = data.read(t); len != -1; len = data.read(t)) {
                    md.update(t, 0, len);
                }

                md51 = md.digest();


            return md51;
        }
    }

    public static String Base64AES(String msg, String key) throws Throwable {
        if(msg != null && key != null) {
            String result = null;

                result = Base64.encodeToString(AES128Encode(key, msg), 0);
                if(TextUtils.isEmpty(result)) {
                    return result;
                }

                if(result.contains("\n")) {
                    result = result.replace("\n", "");
                }


            return result;
        } else {
            return null;
        }
    }

    public static String urlEncode(String s, String enc) throws Throwable {
        String text = URLEncoder.encode(s, enc);
        return TextUtils.isEmpty(text)?text:text.replace("+", "%20");
    }

    public static String urlEncode(String s) throws Throwable {

            return urlEncode(s, "utf-8");

    }

    public static String CRC32(byte[] data) throws Throwable {
        CRC32 crc = new CRC32();
        crc.update(data);
        long value = crc.getValue();
        StringBuilder sb = new StringBuilder();
        byte b = (byte)((int)(value >>> 56));
        sb.append(String.format("%02x", new Object[]{Integer.valueOf(b & 255)}));
        b = (byte)((int)(value >>> 48));
        sb.append(String.format("%02x", new Object[]{Integer.valueOf(b & 255)}));
        b = (byte)((int)(value >>> 40));
        sb.append(String.format("%02x", new Object[]{Integer.valueOf(b & 255)}));
        b = (byte)((int)(value >>> 32));
        sb.append(String.format("%02x", new Object[]{Integer.valueOf(b & 255)}));
        b = (byte)((int)(value >>> 24));
        sb.append(String.format("%02x", new Object[]{Integer.valueOf(b & 255)}));
        b = (byte)((int)(value >>> 16));
        sb.append(String.format("%02x", new Object[]{Integer.valueOf(b & 255)}));
        b = (byte)((int)(value >>> 8));
        sb.append(String.format("%02x", new Object[]{Integer.valueOf(b & 255)}));
        b = (byte)((int)value);
        sb.append(String.format("%02x", new Object[]{Integer.valueOf(b & 255)}));

        while(sb.charAt(0) == 48) {
            sb = sb.deleteCharAt(0);
        }

        return sb.toString().toLowerCase();
    }

    public static byte[] rawRSAEncode(byte[] data, byte[] publicKey, int keySize) throws Throwable {
        int blockSize = keySize / 8 - 11;
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKey);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        RSAPublicKey key = (RSAPublicKey)factory.generatePublic(spec);
        Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
        cipher.init(1, key);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for(int offSet = 0; data.length - offSet > 0; offSet += blockSize) {
            int inputLen = Math.min(data.length - offSet, blockSize);
            byte[] cache = cipher.doFinal(data, offSet, inputLen);
            baos.write(cache, 0, cache.length);
        }

        baos.close();
        return baos.toByteArray();
    }

    public static byte[] rawRSADecode(byte[] data, byte[] privateKey, int keySize) throws Throwable {
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec pkeySpec = new PKCS8EncodedKeySpec(privateKey);
        factory = KeyFactory.getInstance("RSA");
        RSAPrivateKey key = (RSAPrivateKey)factory.generatePrivate(pkeySpec);
        Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
        cipher.init(2, key);
        int offSet = 0;
        int blockSize = keySize / 8;

        BufferedByteArrayOutputStream baos;
        for(baos = new BufferedByteArrayOutputStream(); data.length - offSet > 0; offSet += blockSize) {
            int inputLen = Math.min(data.length - offSet, blockSize);
            byte[] cache = cipher.doFinal(data, offSet, inputLen);
            baos.write(cache, 0, cache.length);
        }

        baos.close();
        return baos.toByteArray();
    }

    private static String toHex(byte[] data) {
        StringBuffer buffer = new StringBuffer();

        for(int i = 0; i < data.length; ++i) {
            buffer.append(String.format("%02x", new Object[]{Byte.valueOf(data[i])}));
        }

        return buffer.toString();
    }

    //___________________add  ----------------

    /**
     * @Description:MD5-32位小写
     * @author:liuyc
     * @time:2016年5月23日 上午11:15:33
     */
    public static String MD5_32(String encryptStr) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = md5.digest(encryptStr.getBytes());
            StringBuffer hexValue = new StringBuffer();
            for (int i = 0; i < md5Bytes.length; i++) {
                int val = ((int) md5Bytes[i]) & 0xff;
                if (val < 16) {
                    hexValue.append("0");
                }
                hexValue.append(Integer.toHexString(val));
            }
            encryptStr = hexValue.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return encryptStr;
    }

    /**
     * @Description:MD5-16位小写
     * @author:liuyc
     * @time:2016年5月23日 上午11:15:33
     */
    public static String MD5_16(String encryptStr) {
        return MD5_32(encryptStr).substring(8, 24);
    }
}

