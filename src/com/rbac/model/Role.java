package com.rbac.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 角色实体类 - 对应数据库表roles
 * 
 * <p>本类表示RBAC模型中的角色实体，角色是权限的集合，用户通过角色间接获得权限。
 * 
 * <p><b>字段说明：</b>
 * <ul>
 *   <li><b>id</b>：角色唯一标识（主键）</li>
 *   <li><b>roleCode</b>：角色编码（如ROLE_ADMIN、ROLE_USER，唯一）</li>
 *   <li><b>roleName</b>：角色名称（如"系统管理员"、"普通用户"）</li>
 *   <li><b>description</b>：角色描述（说明角色的职责和权限范围）</li>
 *   <li><b>createdAt</b>：创建时间</li>
 * </ul>
 * 
 * <p><b>RBAC设计：</b>
 * <ul>
 *   <li>用户通过 user_roles 表关联到角色</li>
 *   <li>角色通过 role_permissions 表关联到权限</li>
 *   <li>用户-角色-权限 多对多关系</li>
 * </ul>
 * 
 * @author RBAC Team
 * @see Permission
 */
public class Role {
    
    /** 角色ID（主键） */
    private Integer id;
    
    /** 角色编码（唯一，如ROLE_ADMIN） */
    private String roleCode;
    
    /** 角色名称（如"系统管理员"） */
    private String roleName;
    
    /** 角色描述 */
    private String description;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /**
     * 默认构造函数
     */
    public Role() {
    }
    
    /**
     * 带参数的构造函数
     * 
     * @param id 角色ID
     * @param roleCode 角色编码
     * @param roleName 角色名称
     * @param description 角色描述
     */
    public Role(Integer id, String roleCode, String roleName, String description) {
        this.id = id;
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.description = description;
    }
    
    // ==================== Getters and Setters ====================
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getRoleCode() {
        return roleCode;
    }
    
    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
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
    
    // ==================== Object方法重写 ====================
    
    /**
     * 根据角色ID判断角色是否相等
     * 
     * @param o 要比较的对象
     * @return 如果角色ID相同返回true，否则返回false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id);
    }
    
    /**
     * 根据角色ID生成哈希码
     * 
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    /**
     * 返回角色的字符串表示
     * 
     * @return 角色信息字符串
     */
    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", roleCode='" + roleCode + '\'' +
                ", roleName='" + roleName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
