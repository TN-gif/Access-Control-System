package com.rbac.dao;

import com.rbac.model.User;
import com.rbac.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问对象 - 负责用户表的CRUD操作
 * 
 * <p>本类封装了对数据库表users的所有访问操作，包括增删改查。
 * 
 * <p><b>数据库表结构（users）：</b>
 * <ul>
 *   <li>id - 主键（自增）</li>
 *   <li>username - 用户名（唯一索引）</li>
 *   <li>password_hash - 密码哈希</li>
 *   <li>salt - 密码盐值</li>
 *   <li>status - 状态（0=正常，1=冻结）</li>
 *   <li>created_at - 创建时间（自动生成）</li>
 *   <li>updated_at - 更新时间（自动更新）</li>
 * </ul>
 * 
 * @author RBAC Team
 * @see User
 */
public class UserDao {
    
    /**
     * 插入新用户记录
     * 
     * <p>执行INSERT操作，created_at和updated_at由数据库自动生成
     * 
     * @param user 要插入的用户对象（必须包含username、passwordHash、salt、status）
     * @return 新插入用户的ID（主键），如果插入失败返回-1
     * @throws RuntimeException 当数据库操作失败时
     */
    public int insert(User user) {
        String sql = "INSERT INTO users (username, password_hash, salt, status) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getSalt());
            pstmt.setInt(4, user.getStatus());
            
            pstmt.executeUpdate();
            
            // 获取自动生成的主键
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("插入用户失败: " + user.getUsername(), e);
        }
    }
    
    /**
     * 根据ID删除用户
     * 
     * <p>执行DELETE操作，会级联删除相关的用户-角色关联数据（由数据库外键约束处理）
     * 
     * @param id 要删除的用户ID
     * @return 如果删除成功返回true，如果用户不存在返回false
     * @throws RuntimeException 当数据库操作失败时
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("删除用户失败，ID: " + id, e);
        }
    }
    
    /**
     * 更新用户状态
     * 
     * <p>只更新status字段，updated_at由数据库自动更新
     * 
     * @param id 用户ID
     * @param status 新状态（0=正常，1=冻结）
     * @return 如果更新成功返回true，如果用户不存在返回false
     * @throws RuntimeException 当数据库操作失败时
     */
    public boolean updateStatus(int id, int status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, status);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("更新用户状态失败，ID: " + id, e);
        }
    }
    
    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户对象，如果不存在返回null
     * @throws RuntimeException 当数据库操作失败时
     */
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("查询用户失败，ID: " + id, e);
        }
    }
    
    /**
     * 根据用户名查询用户
     * 
     * <p>username字段有唯一索引，查询效率高
     * 
     * @param username 用户名
     * @return 用户对象，如果不存在返回null
     * @throws RuntimeException 当数据库操作失败时
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("查询用户失败，用户名: " + username, e);
        }
    }
    
    /**
     * 查询所有用户
     * 
     * <p>按ID升序排列，建议在用户数量较大时添加分页
     * 
     * @return 所有用户的列表（可能为空列表）
     * @throws RuntimeException 当数据库操作失败时
     */
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("查询用户列表失败", e);
        }
    }
    
    /**
     * 检查用户名是否已存在
     * 
     * <p>用于在创建用户前验证用户名唯一性
     * 
     * @param username 要检查的用户名
     * @return 如果用户名已存在返回true，否则返回false
     * @throws RuntimeException 当数据库操作失败时
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("检查用户名失败: " + username, e);
        }
    }
    
    /**
     * 将ResultSet映射为User对象
     * 
     * <p>此方法处理数据库类型到Java类型的转换，特别是Timestamp到LocalDateTime的转换。
     * 需要单独处理Timestamp是因为直接调用getObject可能返回null导致NullPointerException。
     * 
     * @param rs 数据库查询结果集（当前指向某一行）
     * @return 映射后的User对象
     * @throws SQLException 当读取ResultSet失败时
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setSalt(rs.getString("salt"));
        user.setStatus(rs.getInt("status"));
        
        // 安全处理时间戳字段（可能为null）
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return user;
    }
}
