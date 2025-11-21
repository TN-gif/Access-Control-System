package com.rbac.test;

import com.rbac.util.PasswordUtil;

/**
 * 生成密码哈希工具
 * 用于生成 init_data.sql 中需要的密码哈希值
 */
public class GeneratePasswordHash {
    
    public static void main(String[] args) {
        // 生成 admin123 的密码哈希
        String password = "admin123";
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);
        
        System.out.println("========================================");
        System.out.println("密码哈希生成工具");
        System.out.println("========================================");
        System.out.println();
        System.out.println("原始密码: " + password);
        System.out.println("盐值 (salt): " + salt);
        System.out.println("哈希值 (hash): " + hash);
        System.out.println();
        System.out.println("========================================");
        System.out.println("SQL 插入语句：");
        System.out.println("========================================");
        System.out.println();
        System.out.println("-- 删除旧的 admin 用户");
        System.out.println("DELETE FROM user_roles WHERE user_id = (SELECT id FROM users WHERE username = 'admin');");
        System.out.println("DELETE FROM users WHERE username = 'admin';");
        System.out.println();
        System.out.println("-- 插入新的 admin 用户");
        System.out.println("INSERT INTO users (username, password_hash, salt, status, created_at, updated_at)");
        System.out.println("VALUES ('admin', '" + hash + "', '" + salt + "', 'ACTIVE', NOW(), NOW());");
        System.out.println();
        System.out.println("-- 为 admin 分配 ADMIN 角色");
        System.out.println("INSERT INTO user_roles (user_id, role_id)");
        System.out.println("SELECT u.id, r.id FROM users u, roles r");
        System.out.println("WHERE u.username = 'admin' AND r.role_code = 'ADMIN';");
        System.out.println();
        System.out.println("========================================");
        System.out.println("验证密码：");
        System.out.println("========================================");
        
        // 验证密码
        boolean isValid = PasswordUtil.verifyPassword(password, salt, hash);
        System.out.println("密码验证结果: " + (isValid ? "✓ 通过" : "✗ 失败"));
    }
}
