package com.rbac.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户实体类
 */
public class User {
    
    private Integer id;
    private String username;
    private String passwordHash;
    private String salt;
    private Integer status;  // 0=正常, 1=冻结
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public User() {
    }
    
    public User(Integer id, String username, String passwordHash, String salt, Integer status) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.status = status;
    }
    
    // Getters and Setters
    
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
    
    /**
     * 判断用户是否被冻结
     */
    public boolean isFrozen() {
        return status != null && status == 1;
    }
    
    /**
     * 判断用户是否正常
     */
    public boolean isNormal() {
        return status != null && status == 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
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
