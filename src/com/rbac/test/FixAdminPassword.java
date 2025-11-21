package com.rbac.test;

import com.rbac.util.DBUtil;
import com.rbac.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 修复 admin 密码工具
 * 直接在数据库中更新 admin 用户的密码为 admin123
 */
public class FixAdminPassword {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("    修复 Admin 密码工具");
        System.out.println("========================================\n");
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            // 连接数据库
            conn = DBUtil.getConnection();
            System.out.println("✓ 数据库连接成功\n");
            
            // 步骤1：生成新的密码哈希
            String password = "admin123";
            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hashPassword(password, salt);
            
            System.out.println("【步骤1】生成新的密码哈希");
            System.out.println("  密码: " + password);
            System.out.println("  盐值: " + salt);
            System.out.println("  哈希: " + hash);
            System.out.println();
            
            // 步骤2：更新数据库
            System.out.println("【步骤2】更新数据库中的 admin 用户");
            String updateSql = "UPDATE users SET password_hash = ?, salt = ? WHERE username = 'admin'";
            ps = conn.prepareStatement(updateSql);
            ps.setString(1, hash);
            ps.setString(2, salt);
            
            int updated = ps.executeUpdate();
            
            if (updated > 0) {
                System.out.println("✓ 更新成功，影响 " + updated + " 行");
            } else {
                System.out.println("✗ 更新失败，可能 admin 用户不存在");
                System.out.println("  请先运行 init_data.sql 创建 admin 用户");
                return;
            }
            System.out.println();
            
            // 步骤3：验证更新
            System.out.println("【步骤3】验证更新结果");
            DBUtil.close(ps);
            ps = conn.prepareStatement("SELECT username, salt, password_hash, status FROM users WHERE username = 'admin'");
            rs = ps.executeQuery();
            
            if (rs.next()) {
                String dbSalt = rs.getString("salt");
                String dbHash = rs.getString("password_hash");
                int dbStatus = rs.getInt("status");
                
                System.out.println("  数据库中的信息：");
                System.out.println("    用户名: " + rs.getString("username"));
                System.out.println("    盐值: " + dbSalt);
                System.out.println("    哈希: " + dbHash);
                System.out.println("    状态: " + dbStatus + " (" + (dbStatus == 0 ? "正常" : "冻结") + ")");
                System.out.println();
                
                // 步骤4：验证密码
                System.out.println("【步骤4】验证密码是否正确");
                boolean isValid = PasswordUtil.verifyPassword(password, dbSalt, dbHash);
                System.out.println("  验证结果: " + (isValid ? "✓ 密码验证通过" : "✗ 密码验证失败"));
                System.out.println();
                
                if (isValid) {
                    System.out.println("========================================");
                    System.out.println("✓ Admin 密码修复成功！");
                    System.out.println("========================================");
                    System.out.println();
                    System.out.println("现在可以使用以下凭据登录：");
                    System.out.println("  用户名: admin");
                    System.out.println("  密码: admin123");
                    System.out.println();
                    System.out.println("请运行 MainApp.java 测试登录");
                    System.out.println("========================================");
                } else {
                    System.out.println("✗ 密码验证失败，请检查 PasswordUtil 实现");
                }
            } else {
                System.out.println("✗ 查询失败：admin 用户不存在");
            }
            
        } catch (Exception e) {
            System.err.println("✗ 操作失败：" + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(rs, ps, conn);
        }
    }
}
