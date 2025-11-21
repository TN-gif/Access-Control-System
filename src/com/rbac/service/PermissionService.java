package com.rbac.service;

import com.rbac.model.Permission;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService {
    
    /**
     * 创建权限
     */
    void createPermission(String permissionCode, String description);
    
    /**
     * 删除权限
     */
    void deletePermission(int permissionId);
    
    /**
     * 查询所有权限
     */
    List<Permission> listPermissions();
    
    /**
     * 根据ID查询权限
     */
    Permission getPermissionById(int permissionId);
    
    /**
     * 根据编码查询权限
     */
    Permission getPermissionByCode(String permissionCode);
    
    /**
     * 为角色分配权限
     */
    void assignPermissionToRole(int roleId, int permissionId);
    
    /**
     * 取消角色权限
     */
    void removePermissionFromRole(int roleId, int permissionId);
    
    /**
     * 查询角色的所有权限
     */
    List<Permission> getRolePermissions(int roleId);
}
