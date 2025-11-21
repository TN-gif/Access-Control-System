package com.rbac.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码工具类 - 负责密码加密和验证
 */
public class PasswordUtil {
    
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    
    /**
     * 生成随机盐值
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * 对密码进行哈希处理
     * @param password 原始密码
     * @param salt 盐值
     * @return 哈希后的密码
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            String saltedPassword = password + salt;
            byte[] hash = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    /**
     * 验证密码是否正确
     * @param rawPassword 原始密码
     * @param salt 盐值
     * @param hashedPassword 存储的哈希密码
     * @return 密码是否匹配
     */
    public static boolean verifyPassword(String rawPassword, String salt, String hashedPassword) {
        String hash = hashPassword(rawPassword, salt);
        return hash.equals(hashedPassword);
    }
}
