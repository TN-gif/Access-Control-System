package com.rbac.test;

import com.rbac.dao.UserDao;
import com.rbac.model.User;
import com.rbac.util.PasswordUtil;

/**
 * 登录调试工具 - 详细显示登录验证的每一步
 */
public class DebugLogin {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("    登录调试工具");
        System.out.println("========================================\n");
        
        // 测试数据
        String testUsername = "admin";
        String testPassword = "admin123";
        
        System.out.println("【测试数据】");
        System.out.println("用户名: " + testUsername);
        System.out.println("密码: " + testPassword);
        System.out.println();
        
        // 第一步：查询用户
        System.out.println("【步骤1】从数据库查询用户");
        UserDao userDao = new UserDao();
        User user = userDao.findByUsername(testUsername);
        
        if (user == null) {
            System.out.println("✗ 错误：用户不存在！");
            System.out.println("请检查数据库中是否有 admin 用户");
            return;
        }
        
        System.out.println("✓ 用户存在");
        System.out.println("  用户ID: " + user.getId());
        System.out.println("  用户名: " + user.getUsername());
        System.out.println("  状态: " + user.getStatus());
        System.out.println("  数据库中的盐值: " + user.getSalt());
        System.out.println("  数据库中的哈希: " + user.getPasswordHash());
        System.out.println();
        
        // 第二步：生成输入密码的哈希
        System.out.println("【步骤2】计算输入密码的哈希");
        String calculatedHash = PasswordUtil.hashPassword(testPassword, user.getSalt());
        System.out.println("  计算出的哈希: " + calculatedHash);
        System.out.println();
        
        // 第三步：对比哈希值
        System.out.println("【步骤3】对比哈希值");
        System.out.println("  数据库哈希: " + user.getPasswordHash());
        System.out.println("  计算出哈希: " + calculatedHash);
        System.out.println("  是否匹配: " + user.getPasswordHash().equals(calculatedHash));
        System.out.println();
        
        // 第四步：使用 verifyPassword 方法验证
        System.out.println("【步骤4】使用 PasswordUtil.verifyPassword 验证");
        boolean isValid = PasswordUtil.verifyPassword(testPassword, user.getSalt(), user.getPasswordHash());
        System.out.println("  验证结果: " + (isValid ? "✓ 密码正确" : "✗ 密码错误"));
        System.out.println();
        
        // 第五步：检查用户状态
        System.out.println("【步骤5】检查用户状态");
        System.out.println("  状态: " + user.getStatus());
        System.out.println("  是否冻结: " + user.isFrozen());
        System.out.println();
        
        // 总结
        System.out.println("========================================");
        System.out.println("    诊断结果");
        System.out.println("========================================");
        
        if (user == null) {
            System.out.println("✗ 问题：用户不存在");
            System.out.println("  解决方案：执行 init_data.sql 或运行 GeneratePasswordHash.java");
        } else if (!isValid) {
            System.out.println("✗ 问题：密码哈希不匹配");
            System.out.println("  原因：数据库中的盐值/哈希值不正确");
            System.out.println("  解决方案：");
            System.out.println("    1. 运行 GeneratePasswordHash.java 生成正确的 SQL");
            System.out.println("    2. 在 MySQL Shell 中执行生成的 SQL");
            System.out.println();
            System.out.println("  生成正确密码的步骤：");
            System.out.println("    String salt = PasswordUtil.generateSalt();");
            System.out.println("    String hash = PasswordUtil.hashPassword(\"admin123\", salt);");
            System.out.println("    然后更新数据库：");
            System.out.println("    UPDATE users SET password_hash='[hash]', salt='[salt]' WHERE username='admin';");
        } else if (user.isFrozen()) {
            System.out.println("✗ 问题：账户已被冻结");
            System.out.println("  解决方案：");
            System.out.println("    UPDATE users SET status='ACTIVE' WHERE username='admin';");
        } else {
            System.out.println("✓ 所有检查通过！");
            System.out.println("  用户应该可以正常登录");
            System.out.println("  如果还是登录失败，请检查：");
            System.out.println("    1. 输入的用户名/密码是否完全正确（没有多余空格）");
            System.out.println("    2. 数据库连接是否正常");
            System.out.println("    3. 查看审计日志: logs/audit.log");
        }
        
        System.out.println("========================================");
    }
}
