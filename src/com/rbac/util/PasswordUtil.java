package com.rbac.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码工具类 - 负责密码加密和验证
 * 
 * <p>本类采用<b>加盐哈希</b>的方式存储密码，确保即使数据库泄露也无法逆向获取明文密码。
 * 
 * <p><b>设计决策：</b>
 * <ul>
 *   <li><b>加密算法</b>：使用SHA-256单向哈希算法（不可逆）</li>
 *   <li><b>盐值长度</b>：16字节（符合OWASP安全建议）</li>
 *   <li><b>随机生成器</b>：使用SecureRandom（密码学安全的随机数生成器）</li>
 *   <li><b>编码方式</b>：UTF-8字符编码 + Base64存储编码</li>
 * </ul>
 * 
 * <p><b>使用流程：</b>
 * <ol>
 *   <li>创建用户时：调用 {@link #generateSalt()} 生成盐值，再调用 {@link #hashPassword(String, String)} 生成密码哈希</li>
 *   <li>验证密码时：调用 {@link #verifyPassword(String, String, String)} 比对用户输入和存储的哈希</li>
 * </ol>
 * 
 * @author RBAC Team
 * @see SecureRandom
 * @see MessageDigest
 */
public class PasswordUtil {
    
    /** SHA-256哈希算法（安全性高且性能较好） */
    private static final String ALGORITHM = "SHA-256";
    
    /** 盐值长度：16字节（128位），符合NIST SP 800-132建议 */
    private static final int SALT_LENGTH = 16;
    
    /**
     * 生成随机盐值
     * 
     * <p>每个用户的盐值都是唯一的，确保相同密码的用户也会产生不同的哈希值，
     * 从而抵御彩虹表攻击和批量破解。
     * 
     * @return Base64编码的盐值字符串
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * 对密码进行哈希处理
     * 
     * <p>将用户的明文密码与盐值拼接后，使用SHA-256算法进行单向哈希，
     * 生成的哈希值无法逆向为原始密码。
     * 
     * @param password 原始密码（明文）
     * @param salt 盐值（由 {@link #generateSalt()} 生成）
     * @return Base64编码的密码哈希字符串
     * @throws RuntimeException 当SHA-256算法不可用时（通常不会发生）
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            String saltedPassword = password + salt;
            byte[] hash = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            // 所有Java平台都必须支持SHA-256，此异常几乎不会发生
            throw new RuntimeException("密码加密失败：SHA-256算法不可用", e);
        }
    }
    
    /**
     * 验证密码是否正确
     * 
     * <p>将用户输入的密码使用相同的 盐值和算法进行哈希，然后与存储的哈希值比对。
     * 
     * @param rawPassword 用户输入的原始密码（明文）
     * @param salt 存储在数据库中的盐值
     * @param hashedPassword 存储在数据库中的密码哈希
     * @return 如果密码匹配返回true，否则返回false
     */
    public static boolean verifyPassword(String rawPassword, String salt, String hashedPassword) {
        String hash = hashPassword(rawPassword, salt);
        return hash.equals(hashedPassword);
    }
}
