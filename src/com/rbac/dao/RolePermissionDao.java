package com.rbac.dao;

import com.rbac.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 角色-权限关联数据访问对象
 */
public class RolePermissionDao {
    
    /**
     * 为角色分配权限
     */
    public boolean assignPermission(int roleId, int permissionId) {
        String sql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roleId);
            pstmt.setInt(2, permissionId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // 如果是唯一键冲突，说明已经分配过了
            if (e.getMessage().contains("Duplicate entry")) {
                return false;
            }
            throw new RuntimeException("分配权限失败", e);
        }
    }
    
    /**
     * 取消角色权限
     */
    public boolean removePermission(int roleId, int permissionId) {
        String sql = "DELETE FROM role_permissions WHERE role_id = ? AND permission_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roleId);
            pstmt.setInt(2, permissionId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("移除权限失败", e);
        }
    }
    
    /**
     * 检查角色是否拥有某权限
     */
    public boolean hasPermission(int roleId, int permissionId) {
        String sql = "SELECT COUNT(*) FROM role_permissions WHERE role_id = ? AND permission_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roleId);
            pstmt.setInt(2, permissionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("检查角色权限失败", e);
        }
    }
    
    /**
     * 删除角色的所有权限
     */
    public boolean removeAllPermissionsByRoleId(int roleId) {
        String sql = "DELETE FROM role_permissions WHERE role_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roleId);
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("删除角色权限失败", e);
        }
    }
}
