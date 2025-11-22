package com.rbac.dao;

import com.rbac.model.Permission;
import com.rbac.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限数据访问对象 - 负责权限表的CRUD操作
 * 
 * <p>本类封装了对数据库表permissions和关联表role_permissions的所有访问操作。
 * 
 * <p><b>数据库表结构（permissions）：</b>
 * <ul>
 *   <li>id - 主键（自增）</li>
 *   <li>permission_code - 权限编码（唯一索引，如USER:CREATE）</li>
 *   <li>description - 权限描述（如"创建用户"）</li>
 *   <li>created_at - 创建时间（自动生成）</li>
 * </ul>
 * 
 * <p><b>关联表（role_permissions）：</b>
 * <ul>
 *   <li>role_id - 角色ID（外键）</li>
 *   <li>permission_id - 权限ID（外键）</li>
 * </ul>
 * 
 * <p><b>RBAC权限查询：</b>
 * <ul>
 *   <li>{@link #findByUserId(int)} - 通过双重JOIN查询用户的所有权限（user_roles + role_permissions）</li>
 *   <li>{@link #findByRoleId(int)} - 查询角色的所有权限</li>
 *   <li>使用DISTINCT避免重复权限（用户可能通过多个角色获得同一权限）</li>
 * </ul>
 * 
 * @author RBAC Team
 * @see Permission
 */
public class PermissionDao {
    
    /**
     * 插入新权限记录
     * 
     * @param permission 要插入的权限对象（必须包含permissionCode、description）
     * @return 新插入权限的ID（主键），如果插入失败返回-1
     * @throws RuntimeException 当数据库操作失败时
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
            throw new RuntimeException("插入权限失败: " + permission.getPermissionCode(), e);
        }
    }
    
    /**
     * 根据ID删除权限
     * 
     * <p>会级联删除role_permissions中的关联记录（由数据库外键约束处理）
     * 
     * @param id 要删除的权限ID
     * @return 如果删除成功返回true，如果权限不存在返回false
     * @throws RuntimeException 当数据库操作失败时
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM permissions WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("删除权限失败，ID: " + id, e);
        }
    }
    
    /**
     * 根据ID查询权限
     * 
     * @param id 权限ID
     * @return 权限对象，如果不存在返回null
     * @throws RuntimeException 当数据库操作失败时
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
            throw new RuntimeException("查询权限失败，ID: " + id, e);
        }
    }
    
    /**
     * 根据权限编码查询权限
     * 
     * <p>permission_code字段有唯一索引，查询效率高
     * 
     * @param code 权限编码（如USER:CREATE）
     * @return 权限对象，如果不存在返回null
     * @throws RuntimeException 当数据库操作失败时
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
            throw new RuntimeException("查询权限失败，编码: " + code, e);
        }
    }
    
    /**
     * 查询所有权限
     * 
     * @return 所有权限的列表（可能为空列表）
     * @throws RuntimeException 当数据库操作失败时
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
     * 查询用户拥有的所有权限（通过角色间接获得）
     * 
     * <p>SQL执行双重JOIN：permissions &lt;-&gt; role_permissions &lt;-&gt; user_roles
     * <p>使用DISTINCT去重，因为用户可能通过多个角色获得同一权限
     * <p>这是RBAC模型的核心查询，实现用户-角色-权限的权限继承
     * 
     * @param userId 用户ID
     * @return 用户的权限列表（去重后，可能为空列表）
     * @throws RuntimeException 当数据库操作失败时
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
            throw new RuntimeException("查询用户权限失败，用户ID: " + userId, e);
        }
    }
    
    /**
     * 查询角色拥有的所有权限
     * 
     * <p>通过JOIN role_permissions关联表查询
     * 
     * @param roleId 角色ID
     * @return 角色的权限列表（可能为空列表）
     * @throws RuntimeException 当数据库操作失败时
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
            throw new RuntimeException("查询角色权限失败，角色ID: " + roleId, e);
        }
    }
    
    /**
     * 为角色分配权限（插入role_permissions关联记录）
     * 
     * <p>注意：此方法只负责数据库操作，不检查角色和权限是否存在，
     * 业务逻辑验证应在Service层完成
     * 
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 如果分配成功返回true
     * @throws RuntimeException 当数据库操作失败时
     */
    public boolean assignPermissionToRole(int roleId, int permissionId) {
        String sql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roleId);
            pstmt.setInt(2, permissionId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("分配权限失败，角色ID: " + roleId + ", 权限ID: " + permissionId, e);
        }
    }
    
    /**
     * 移除角色的权限（删除role_permissions关联记录）
     * 
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 如果移除成功返回true，如果关联不存在返回false
     * @throws RuntimeException 当数据库操作失败时
     */
    public boolean removePermissionFromRole(int roleId, int permissionId) {
        String sql = "DELETE FROM role_permissions WHERE role_id = ? AND permission_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roleId);
            pstmt.setInt(2, permissionId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("移除权限失败，角色ID: " + roleId + ", 权限ID: " + permissionId, e);
        }
    }
    
    /**
     * 检查权限编码是否已存在
     * 
     * <p>用于在创建权限前验证编码唯一性
     * 
     * @param permissionCode 要检查的权限编码
     * @return 如果权限编码已存在返回true，否则返回false
     * @throws RuntimeException 当数据库操作失败时
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
            throw new RuntimeException("检查权限编码失败: " + permissionCode, e);
        }
    }

    /**
     * 将ResultSet映射为Permission对象
     * 
     * @param rs 数据库查询结果集（当前指向某一行）
     * @return 映射后的Permission对象
     * @throws SQLException 当读取ResultSet失败时
     */
    private Permission mapResultSetToPermission(ResultSet rs) throws SQLException {
        Permission permission = new Permission();
        permission.setId(rs.getInt("id"));
        permission.setPermissionCode(rs.getString("permission_code"));
        permission.setDescription(rs.getString("description"));
        
        // 安全处理时间戳字段（可能为null）
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            permission.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return permission;
    }
}
