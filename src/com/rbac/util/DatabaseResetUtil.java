package com.rbac.util;

import java.sql.Connection;
import java.sql.Statement;

/**
 * 数据库重置工具
 * 用于在演示前将数据库恢复到干净的初始状态
 */
public class DatabaseResetUtil {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   RBAC 数据库重置工具");
        System.out.println("========================================");
        System.out.println();
        
        try {
            resetDatabase();
            System.out.println("\n✓ 数据库重置完成！");
            System.out.println("现在可以开始演示了。");
        } catch (Exception e) {
            System.err.println("\n✗ 数据库重置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 重置数据库到初始状态
     */
    private static void resetDatabase() throws Exception {
        System.out.println("正在重置数据库...");
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 1. 清空所有表数据（保留结构）
            System.out.print("  [1/5] 清空用户角色关联表... ");
            stmt.executeUpdate("DELETE FROM user_roles");
            System.out.println("✓");
            
            System.out.print("  [2/5] 清空角色权限关联表... ");
            stmt.executeUpdate("DELETE FROM role_permissions");
            System.out.println("✓");
            
            System.out.print("  [3/5] 清空用户表（保留admin）... ");
            stmt.executeUpdate("DELETE FROM users WHERE username != 'admin'");
            System.out.println("✓");
            
            System.out.print("  [4/5] 清空角色表（保留系统角色）... ");
            stmt.executeUpdate("DELETE FROM roles WHERE role_code NOT IN ('ADMIN', 'GUEST')");
            System.out.println("✓");
            
            System.out.print("  [5/5] 清空权限表（保留系统权限）... ");
            stmt.executeUpdate("DELETE FROM permissions WHERE permission_code NOT LIKE 'USER:%' " +
                    "AND permission_code NOT LIKE 'ROLE:%' " +
                    "AND permission_code NOT LIKE 'PERMISSION:%' " +
                    "AND permission_code NOT LIKE 'AUDIT:%'");
            System.out.println("✓");
            
            // 2. 重置admin用户状态
            System.out.print("  [额外] 重置admin用户状态... ");
            stmt.executeUpdate("UPDATE users SET status = 0 WHERE username = 'admin'");
            System.out.println("✓");
            
            // 3. 清空审计日志
            System.out.print("  [额外] 清空审计日志... ");
            java.nio.file.Path logPath = java.nio.file.Paths.get(
                    ConfigUtil.getString("audit.log.path", "logs/audit.log"));
            if (java.nio.file.Files.exists(logPath)) {
                java.nio.file.Files.delete(logPath);
            }
            System.out.println("✓");
            
            System.out.println();
            System.out.println("数据库已重置为初始状态：");
            System.out.println("  - 保留 admin 用户（密码: admin123）");
            System.out.println("  - 保留 ADMIN 和 GUEST 角色");
            System.out.println("  - 保留所有系统权限");
            System.out.println("  - 清空所有测试数据");
            System.out.println("  - 清空审计日志");
        }
    }
}
