package com.rbac.dao;

import com.rbac.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用户-角色关联数据访问对象
 */
public class UserRoleDao {
    
    /**
     * 为用户分配角色
     */
    public boolean assignRole(int userId, int roleId) {
        String sql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, roleId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // 如果是唯一键冲突，说明已经分配过了
            if (e.getMessage().contains("Duplicate entry")) {
                return false;
            }
            throw new RuntimeException("分配角色失败", e);
        }
    }
    
    /**
     * 取消用户角色
     */
    public boolean removeRole(int userId, int roleId) {
        String sql = "DELETE FROM user_roles WHERE user_id = ? AND role_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, roleId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("移除角色失败", e);
        }
    }
    
    /**
     * 检查用户是否拥有某角色
     */
    public boolean hasRole(int userId, int roleId) {
        String sql = "SELECT COUNT(*) FROM user_roles WHERE user_id = ? AND role_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, roleId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("检查用户角色失败", e);
        }
    }
    
    /**
     * 删除用户的所有角色
     */
    public boolean removeAllRolesByUserId(int userId) {
        String sql = "DELETE FROM user_roles WHERE user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("删除用户角色失败", e);
        }
    }
}
