package com.rbac.dao;

import com.rbac.model.Role;
import com.rbac.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色数据访问对象 - 负责角色表的CRUD操作
 * 
 * <p>本类封装了对数据库表roles和关联表user_roles的所有访问操作。
 * 
 * <p><b>数据库表结构（roles）：</b>
 * <ul>
 *   <li>id - 主键（自增）</li>
 *   <li>role_code - 角色编码（唯一索引，如ROLE_ADMIN）</li>
 *   <li>role_name - 角色名称（如"系统管理员"）</li>
 *   <li>description - 角色描述</li>
 *   <li>created_at - 创建时间（自动生成）</li>
 * </ul>
 * 
 * <p><b>关联表（user_roles）：</b>
 * <ul>
 *   <li>user_id - 用户ID（外键）</li>
 *   <li>role_id - 角色ID（外键）</li>
 * </ul>
 * 
 * @author RBAC Team
 * @see Role
 */
public class RoleDao {
    
    /**
     * 插入新角色记录
     * 
     * @param role 要插入的角色对象（必须包含roleCode、roleName、description）
     * @return 新插入角色的ID（主键），如果插入失败返回-1
     * @throws RuntimeException 当数据库操作失败时
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
            throw new RuntimeException("插入角色失败: " + role.getRoleCode(), e);
        }
    }
    
    /**
     * 根据ID删除角色
     * 
     * <p>会级联删除user_roles和role_permissions中的关联记录（由数据库外键约束处理）
     * 
     * @param id 要删除的角色ID
     * @return 如果删除成功返回true，如果角色不存在返回false
     * @throws RuntimeException 当数据库操作失败时
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM roles WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("删除角色失败，ID: " + id, e);
        }
    }
    
    /**
     * 根据ID查询角色
     * 
     * @param id 角色ID
     * @return 角色对象，如果不存在返回null
     * @throws RuntimeException 当数据库操作失败时
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
            throw new RuntimeException("查询角色失败，ID: " + id, e);
        }
    }
    
    /**
     * 根据角色编码查询角色
     * 
     * <p>role_code字段有唯一索引，查询效率高
     * 
     * @param code 角色编码（如ROLE_ADMIN）
     * @return 角色对象，如果不存在返回null
     * @throws RuntimeException 当数据库操作失败时
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
            throw new RuntimeException("查询角色失败，编码: " + code, e);
        }
    }
    
    /**
     * 查询所有角色
     * 
     * @return 所有角色的列表（可能为空列表）
     * @throws RuntimeException 当数据库操作失败时
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
     * 查询用户拥有的所有角色
     * 
     * <p>通过JOIN user_roles关联表查询，使用RBAC模型的用户-角色多对多关系
     * 
     * @param userId 用户ID
     * @return 用户的角色列表（可能为空列表）
     * @throws RuntimeException 当数据库操作失败时
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
            throw new RuntimeException("查询用户角色失败，用户ID: " + userId, e);
        }
    }
    
    /**
     * 为用户分配角色（插入user_roles关联记录）
     * 
     * <p>注意：此方法只负责数据库操作，不检查用户和角色是否存在，
     * 业务逻辑验证应在Service层完成
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 如果分配成功返回true
     * @throws RuntimeException 当数据库操作失败时
     */
    public boolean assignRoleToUser(int userId, int roleId) {
        String sql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, roleId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("分配角色失败，用户ID: " + userId + ", 角色ID: " + roleId, e);
        }
    }
    
    /**
     * 移除用户的角色（删除user_roles关联记录）
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 如果移除成功返回true，如果关联不存在返回false
     * @throws RuntimeException 当数据库操作失败时
     */
    public boolean removeRoleFromUser(int userId, int roleId) {
        String sql = "DELETE FROM user_roles WHERE user_id = ? AND role_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, roleId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("移除角色失败，用户ID: " + userId + ", 角色ID: " + roleId, e);
        }
    }
    
    /**
     * 检查角色编码是否已存在
     * 
     * <p>用于在创建角色前验证编码唯一性
     * 
     * @param roleCode 要检查的角色编码
     * @return 如果角色编码已存在返回true，否则返回false
     * @throws RuntimeException 当数据库操作失败时
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
            throw new RuntimeException("检查角色编码失败: " + roleCode, e);
        }
    }
    
    /**
     * 将ResultSet映射为Role对象
     * 
     * @param rs 数据库查询结果集（当前指向某一行）
     * @return 映射后的Role对象
     * @throws SQLException 当读取ResultSet失败时
     */
    private Role mapResultSetToRole(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getInt("id"));
        role.setRoleCode(rs.getString("role_code"));
        role.setRoleName(rs.getString("role_name"));
        role.setDescription(rs.getString("description"));
        
        // 安全处理时间戳字段（可能为null）
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            role.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return role;
    }
}
