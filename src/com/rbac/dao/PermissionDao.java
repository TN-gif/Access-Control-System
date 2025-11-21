package com.rbac.dao;

import com.rbac.model.Permission;
import com.rbac.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限数据访问对象
 */
public class PermissionDao {
    
    /**
     * 插入新权限
     */
    public int insert(Permission permission) {
        String sql = "INSERT INTO permissions (permission_code, description) VALUES (?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, permission.getPermissionCode());
            pstmt.setString(2, permission.getDescription());
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("插入权限失败", e);
        }
    }
    
    /**
     * 根据ID删除权限
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM permissions WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("删除权限失败", e);
        }
    }
    
    /**
     * 根据ID查询权限
     */
    public Permission findById(int id) {
        String sql = "SELECT * FROM permissions WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPermission(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("查询权限失败", e);
        }
    }
    
    /**
     * 根据编码查询权限
     */
    public Permission findByCode(String code) {
        String sql = "SELECT * FROM permissions WHERE permission_code = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, code);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPermission(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("查询权限失败", e);
        }
    }
    
    /**
     * 查询所有权限
     */
    public List<Permission> findAll() {
        String sql = "SELECT * FROM permissions ORDER BY id";
        List<Permission> permissions = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                permissions.add(mapResultSetToPermission(rs));
            }
            return permissions;
        } catch (SQLException e) {
            throw new RuntimeException("查询权限列表失败", e);
        }
    }
    
    /**
     * 查询用户拥有的权限（通过角色）
     */
    public List<Permission> findByUserId(int userId) {
        String sql = "SELECT DISTINCT p.* FROM permissions p " +
                "JOIN role_permissions rp ON p.id = rp.permission_id " +
                "JOIN user_roles ur ON rp.role_id = ur.role_id " +
                "WHERE ur.user_id = ?";
        List<Permission> permissions = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    permissions.add(mapResultSetToPermission(rs));
                }
            }
            return permissions;
        } catch (SQLException e) {
            throw new RuntimeException("查询用户权限失败", e);
        }
    }
    
    /**
     * 查询角色拥有的权限
     */
    public List<Permission> findByRoleId(int roleId) {
        String sql = "SELECT p.* FROM permissions p " +
                "JOIN role_permissions rp ON p.id = rp.permission_id " +
                "WHERE rp.role_id = ?";
        List<Permission> permissions = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roleId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    permissions.add(mapResultSetToPermission(rs));
                }
            }
            return permissions;
        } catch (SQLException e) {
            throw new RuntimeException("查询角色权限失败", e);
        }
    }
    
    /**
     * 为角色分配权限
     */
    public boolean assignPermissionToRole(int roleId, int permissionId) {
        String sql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roleId);
            pstmt.setInt(2, permissionId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("分配权限失败", e);
        }
    }
    
    /**
     * 移除角色权限
     */
    public boolean removePermissionFromRole(int roleId, int permissionId) {
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
     * 检查权限编码是否存在
     */
    public boolean existsByCode(String permissionCode) {
        String sql = "SELECT COUNT(*) FROM permissions WHERE permission_code = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, permissionCode);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("检查权限编码失败", e);
        }
    }

    /**
     * 映射ResultSet到Permission对象
     */
    private Permission mapResultSetToPermission(ResultSet rs) throws SQLException {
        Permission permission = new Permission();
        permission.setId(rs.getInt("id"));
        permission.setPermissionCode(rs.getString("permission_code"));
        permission.setDescription(rs.getString("description"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            permission.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return permission;
    }
}
