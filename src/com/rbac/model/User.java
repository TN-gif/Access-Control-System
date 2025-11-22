package com.rbac.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户实体类 - 对应数据库表users
 * 
 * <p>本类表示系统中的用户实体，包含用户的基本信息和认证信息。
 * 
 * <p><b>字段说明：</b>
 * <ul>
 *   <li><b>id</b>：用户唯一标识（主键）</li>
 *   <li><b>username</b>：用户名（登录凭证，唯一）</li>
 *   <li><b>passwordHash</b>：密码哈希值（SHA-256加盐哈希，不存储明文密码）</li>
 *   <li><b>salt</b>：密码盐值（用于增强密码安全性）</li>
 *   <li><b>status</b>：用户状态（0=正常，1=冻结）</li>
 *   <li><b>createdAt</b>：创建时间</li>
 *   <li><b>updatedAt</b>：最后更新时间</li>
 * </ul>
 * 
 * <p><b>业务规则：</b>
 * <ul>
 *   <li>被冻结的用户无法登录系统</li>
 *   <li>密码采用加盐哈希存储，无法逆向为明文</li>
 *   <li>用户名在系统中必须唯一</li>
 * </ul>
 * 
 * @author RBAC Team
 * @see com.rbac.util.PasswordUtil
 */
public class User {
    
    /** 用户ID（主键） */
    private Integer id;
    
    /** 用户名（唯一） */
    private String username;
    
    /** 密码哈希值（SHA-256加盐） */
    private String passwordHash;
    
    /** 密码盐值（用于密码加密） */
    private String salt;
    
    /** 用户状态：0=正常，1=冻结 */
    private Integer status;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 最后更新时间 */
    private LocalDateTime updatedAt;
    
    /**
     * 默认构造函数
     */
    public User() {
    }
    
    /**
     * 带参数的构造函数
     * 
     * @param id 用户ID
     * @param username 用户名
     * @param passwordHash 密码哈希
     * @param salt 盐值
     * @param status 状态
     */
    public User(Integer id, String username, String passwordHash, String salt, Integer status) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.status = status;
    }
    
    // ==================== Getters and Setters ====================
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getSalt() {
        return salt;
    }
    
    public void setSalt(String salt) {
        this.salt = salt;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // ==================== 业务方法 ====================
    
    /**
     * 判断用户是否被冻结
     * 
     * @return 如果用户状态为冻结（status=1）返回true，否则返回false
     */
    public boolean isFrozen() {
        return status != null && status == 1;
    }
    
    /**
     * 判断用户是否正常
     * 
     * @return 如果用户状态为正常（status=0）返回true，否则返回false
     */
    public boolean isNormal() {
        return status != null && status == 0;
    }
    
    // ==================== Object方法重写 ====================
    
    /**
     * 根据用户ID判断用户是否相等
     * 
     * @param o 要比较的对象
     * @return 如果用户ID相同返回true，否则返回false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    
    /**
     * 根据用户ID生成哈希码
     * 
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    /**
     * 返回用户的字符串表示（不包含敏感信息）
     * 
     * @return 用户信息字符串
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", status=" + (isFrozen() ? "冻结" : "正常") +
                ", createdAt=" + createdAt +
                '}';
    }
}
