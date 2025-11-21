package com.rbac.test;

import com.rbac.util.ConfigUtil;
import com.rbac.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 数据库连接测试类
 */
public class TestConnection {
    public static void main(String[] args) {
        System.out.println("======== 数据库连接测试 ========\n");
        
        // 打印配置信息（调试用）
        System.out.println("【配置信息】");
        String url = ConfigUtil.getString("db.url");
        String user = ConfigUtil.getString("db.user");
        String password = ConfigUtil.getString("db.password");
        System.out.println("URL: " + url);
        System.out.println("User: " + user);
        System.out.println("Password: [" + password + "]");
        System.out.println("Password length: " + (password != null ? password.length() : "null"));
        System.out.println();
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            // 测试连接
            conn = DBUtil.getConnection();
            System.out.println("✓ 数据库连接成功！\n");
            
            // 查询表列表
            ps = conn.prepareStatement("SHOW TABLES");
            rs = ps.executeQuery();
            System.out.println("数据库表列表：");
            while (rs.next()) {
                System.out.println("  - " + rs.getString(1));
            }
            
            // 查询用户数量
            DBUtil.close(rs);
            DBUtil.close(ps);
            ps = conn.prepareStatement("SELECT COUNT(*) FROM users");
            rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("\n用户数量：" + rs.getInt(1));
            }
            
            System.out.println("\n✓ 测试通过！可以运行 MainApp.java");
            
        } catch (Exception e) {
            System.err.println("✗ 连接失败：" + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(rs, ps, conn);
        }
    }
}