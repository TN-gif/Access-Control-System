package com.rbac.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 权限实体类
 */
public class Permission {
    
    private Integer id;
    private String permissionCode;
    private String description;
    private LocalDateTime createdAt;
    
    public Permission() {
    }
    
    public Permission(Integer id, String permissionCode, String description) {
        this.id = id;
        this.permissionCode = permissionCode;
        this.description = description;
    }
    
    // Getters and Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getPermissionCode() {
        return permissionCode;
    }
    
    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", permissionCode='" + permissionCode + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
