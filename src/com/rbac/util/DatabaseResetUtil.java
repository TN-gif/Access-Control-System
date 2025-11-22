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
            System.out.print("  [1/9] 清空用户角色关联表... ");
            stmt.executeUpdate("DELETE FROM user_roles");
            System.out.println("✓");
            
            System.out.print("  [2/9] 清空角色权限关联表... ");
            stmt.executeUpdate("DELETE FROM role_permissions");
            System.out.println("✓");
            
            System.out.print("  [3/9] 清空用户表（保留admin）... ");
            stmt.executeUpdate("DELETE FROM users WHERE username != 'admin'");
            System.out.println("✓");
            
            System.out.print("  [4/9] 清空角色表（保留系统角色）... ");
            stmt.executeUpdate("DELETE FROM roles WHERE role_code NOT IN ('ADMIN', 'USER_MANAGER', 'AUDITOR', 'GUEST')");
            System.out.println("✓");
            
            System.out.print("  [5/9] 清空权限表（保留系统权限）... ");
            stmt.executeUpdate("DELETE FROM permissions WHERE permission_code NOT LIKE 'USER:%' " +
                    "AND permission_code NOT LIKE 'ROLE:%' " +
                    "AND permission_code NOT LIKE 'PERMISSION:%' " +
                    "AND permission_code NOT LIKE 'AUDIT:%'");
            System.out.println("✓");
            
            // 2. 恢复所有系统角色的权限（完全匹配 init_data.sql）
            System.out.print("  [6/9] 恢复ADMIN角色的所有权限... ");
            stmt.executeUpdate(
                "INSERT INTO role_permissions (role_id, permission_id) " +
                "SELECT r.id, p.id " +
                "FROM roles r, permissions p " +
                "WHERE r.role_code = 'ADMIN'"
            );
            System.out.println("✓");
            
            System.out.print("  [7/9] 恢复USER_MANAGER角色的权限... ");
            int userManagerRows = stmt.executeUpdate(
                "INSERT INTO role_permissions (role_id, permission_id) " +
                "SELECT r.id, p.id " +
                "FROM roles r, permissions p " +
                "WHERE r.role_code = 'USER_MANAGER' " +
                "AND p.permission_code IN ('USER:CREATE', 'USER:DELETE', 'USER:UPDATE', 'USER:LIST', 'USER:FREEZE', 'USER:UNFREEZE', 'ROLE:LIST', 'ROLE:ASSIGN')"
            );
            System.out.println("✓ (" + userManagerRows + "个权限)");
            
            System.out.print("  [8/9] 恢复AUDITOR角色的权限... ");
            int auditorRows = stmt.executeUpdate(
                "INSERT INTO role_permissions (role_id, permission_id) " +
                "SELECT r.id, p.id " +
                "FROM roles r, permissions p " +
                "WHERE r.role_code = 'AUDITOR' " +
                "AND p.permission_code IN ('AUDIT:VIEW', 'AUDIT:ANALYZE', 'USER:LIST', 'ROLE:LIST', 'PERMISSION:LIST')"
            );
            System.out.println("✓ (" + auditorRows + "个权限)");
            
            System.out.print("  [9/9] 恢复GUEST角色的权限... ");
            int guestRows = stmt.executeUpdate(
                "INSERT INTO role_permissions (role_id, permission_id) " +
                "SELECT r.id, p.id " +
                "FROM roles r, permissions p " +
                "WHERE r.role_code = 'GUEST' " +
                "AND p.permission_code IN ('USER:LIST', 'ROLE:LIST', 'PERMISSION:LIST')"
            );
            System.out.println("✓ (" + guestRows + "个权限)");
            
            // 3. 恢复admin用户的ADMIN角色
            System.out.print("  [额外] 恢复admin用户的ADMIN角色... ");
            stmt.executeUpdate(
                "INSERT INTO user_roles (user_id, role_id) " +
                "SELECT u.id, r.id " +
                "FROM users u, roles r " +
                "WHERE u.username = 'admin' AND r.role_code = 'ADMIN'"
            );
            System.out.println("✓");
            
            // 4. 重置admin用户状态
            System.out.print("  [额外] 重置admin用户状态... ");
            stmt.executeUpdate("UPDATE users SET status = 0 WHERE username = 'admin'");
            System.out.println("✓");
            
            // 5. 清空数据库审计日志表
            System.out.print("  [额外] 清空数据库审计日志表... ");
            try {
                stmt.executeUpdate("DELETE FROM audit_logs");
                System.out.println("✓");
            } catch (Exception e) {
                System.out.println("⚠️ (表可能不存在)");
            }
            
            // 6. 清空文件审计日志
            System.out.print("  [额外] 清空文件审计日志... ");
            java.nio.file.Path logPath = java.nio.file.Paths.get(
                    ConfigUtil.getString("audit.log.path", "logs/audit.log"));
            if (java.nio.file.Files.exists(logPath)) {
                java.nio.file.Files.delete(logPath);
            }
            System.out.println("✓");
            
            System.out.println();
            System.out.println("数据库已重置为初始状态：");
            System.out.println("  - 保留 admin 用户（密码: admin123）");
            System.out.println("  - 保留 4 个系统角色: ADMIN, USER_MANAGER, AUDITOR, GUEST");
            System.out.println("  - 恢复所有角色的标准权限配置");
            System.out.println("  - 保留所有系统权限 (23个)");
            System.out.println("  - 清空所有测试数据");
            System.out.println("  - 清空审计日志（文件 + 数据库）");
        }
    }
}
