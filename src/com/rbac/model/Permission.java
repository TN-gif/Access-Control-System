package com.rbac.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 权限实体类 - 对应数据库表permissions
 * 
 * <p>本类表示RBAC模型中的权限实体，权限是系统中最小的访问控制单元。
 * 
 * <p><b>字段说明：</b>
 * <ul>
 *   <li><b>id</b>：权限唯一标识（主键）</li>
 *   <li><b>permissionCode</b>：权限编码（如USER:CREATE、ROLE:DELETE，唯一）</li>
 *   <li><b>description</b>：权限描述（说明权限的具体用途）</li>
 *   <li><b>createdAt</b>：创建时间</li>
 * </ul>
 * 
 * <p><b>权限编码规范：</b>
 * <ul>
 *   <li>格式：资源:操作（如USER:CREATE、ROLE:DELETE）</li>
 *   <li>资源：USER、ROLE、PERM等</li>
 *   <li>操作：CREATE、DELETE、UPDATE、LIST等</li>
 * </ul>
 * 
 * <p><b>RBAC设计：</b>
 * <ul>
 *   <li>权限通过 role_permissions 表关联到角色</li>
 *   <li>用户通过角色间接获得权限</li>
 *   <li>权限粒度应该足够细，但不宜过细</li>
 * </ul>
 * 
 * @author RBAC Team
 * @see Role
 * @see com.rbac.common.PermissionConsts
 */
public class Permission {
    
    /** 权限ID（主键） */
    private Integer id;
    
    /** 权限编码（唯一，如USER:CREATE） */
    private String permissionCode;
    
    /** 权限描述 */
    private String description;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /**
     * 默认构造函数
     */
    public Permission() {
    }
    
    /**
     * 带参数的构造函数
     * 
     * @param id 权限ID
     * @param permissionCode 权限编码
     * @param description 权限描述
     */
    public Permission(Integer id, String permissionCode, String description) {
        this.id = id;
        this.permissionCode = permissionCode;
        this.description = description;
    }
    
    // ==================== Getters and Setters ====================
    
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
    
    // ==================== Object方法重写 ====================
    
    /**
     * 根据权限ID判断权限是否相等
     * 
     * @param o 要比较的对象
     * @return 如果权限ID相同返回true，否则返回false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(id, that.id);
    }
    
    /**
     * 根据权限ID生成哈希码
     * 
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    /**
     * 返回权限的字符串表示
     * 
     * @return 权限信息字符串
     */
    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", permissionCode='" + permissionCode + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
