package com.campus.ai.util;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class Md5Util {
    public static String generateSalt() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String encrypt(String rawPassword, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String combined = salt + rawPassword + salt;
            byte[] digest = md.digest(combined.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }

    public static boolean verify(String rawPassword, String salt, String encryptedPassword) {
        return encrypt(rawPassword, salt).equals(encryptedPassword);
    }
}
