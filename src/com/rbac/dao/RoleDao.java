package com.rbac.dao;

import com.rbac.model.Role;
import com.rbac.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色数据访问对象
 */
public class RoleDao {
    
    /**
     * 插入新角色
     */
    public int insert(Role role) {
        String sql = "INSERT INTO roles (role_code, role_name, description) VALUES (?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, role.getRoleCode());
            pstmt.setString(2, role.getRoleName());
            pstmt.setString(3, role.getDescription());
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("插入角色失败", e);
        }
    }
    
    /**
     * 根据ID删除角色
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM roles WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("删除角色失败", e);
        }
    }
    
    /**
     * 根据ID查询角色
     */
    public Role findById(int id) {
        String sql = "SELECT * FROM roles WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRole(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("查询角色失败", e);
        }
    }
    
    /**
     * 根据编码查询角色
     */
    public Role findByCode(String code) {
        String sql = "SELECT * FROM roles WHERE role_code = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, code);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRole(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("查询角色失败", e);
        }
    }
    
    /**
     * 查询所有角色
     */
    public List<Role> findAll() {
        String sql = "SELECT * FROM roles ORDER BY id";
        List<Role> roles = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                roles.add(mapResultSetToRole(rs));
            }
            return roles;
        } catch (SQLException e) {
            throw new RuntimeException("查询角色列表失败", e);
        }
    }
    
    /**
     * 查询用户拥有的角色
     */
    public List<Role> findByUserId(int userId) {
        String sql = "SELECT r.* FROM roles r " +
                "JOIN user_roles ur ON r.id = ur.role_id " +
                "WHERE ur.user_id = ?";
        List<Role> roles = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(mapResultSetToRole(rs));
                }
            }
            return roles;
        } catch (SQLException e) {
            throw new RuntimeException("查询用户角色失败", e);
        }
    }
    
    /**
     * 为用户分配角色
     */
    public boolean assignRoleToUser(int userId, int roleId) {
        String sql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, roleId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("分配角色失败", e);
        }
    }
    
    /**
     * 移除用户角色
     */
    public boolean removeRoleFromUser(int userId, int roleId) {
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
     * 检查角色编码是否存在
     */
    public boolean existsByCode(String roleCode) {
        String sql = "SELECT COUNT(*) FROM roles WHERE role_code = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, roleCode);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("检查角色编码失败", e);
        }
    }
    
    /**
     * 映射ResultSet到Role对象
     */
    private Role mapResultSetToRole(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getInt("id"));
        role.setRoleCode(rs.getString("role_code"));
        role.setRoleName(rs.getString("role_name"));
        role.setDescription(rs.getString("description"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            role.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return role;
    }
}
