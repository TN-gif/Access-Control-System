package com.rbac.service;

import com.rbac.model.Role;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {
    
    /**
     * 创建角色
     */
    void createRole(String roleCode, String roleName, String description);
    
    /**
     * 删除角色
     */
    void deleteRole(int roleId);
    
    /**
     * 查询所有角色
     */
    List<Role> listRoles();
    
    /**
     * 根据ID查询角色
     */
    Role getRoleById(int roleId);
    
    /**
     * 根据编码查询角色
     */
    Role getRoleByCode(String roleCode);
    
    /**
     * 为用户分配角色
     */
    void assignRoleToUser(int userId, int roleId);
    
    /**
     * 取消用户角色
     */
    void removeRoleFromUser(int userId, int roleId);
    
    /**
     * 查询用户的所有角色
     */
    List<Role> getUserRoles(int userId);
}
