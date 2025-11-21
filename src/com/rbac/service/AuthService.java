package com.rbac.service;

import com.rbac.model.Permission;
import com.rbac.model.User;

import java.util.List;
import java.util.Set;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户对象
     */
    User login(String username, String password);
    
    /**
     * 用户登出
     */
    void logout();
    
    /**
     * 获取当前登录用户
     */
    User getCurrentUser();
    
    /**
     * 检查用户是否拥有指定权限
     * @param permissionCode 权限编码
     * @return 是否拥有权限
     */
    boolean hasPermission(String permissionCode);
    
    /**
     * 检查权限，无权限时抛出异常
     * @param permissionCode 权限编码
     */
    void checkPermission(String permissionCode);
    
    /**
     * 获取用户的所有权限
     * @param userId 用户ID
     * @return 权限编码集合
     */
    Set<String> getUserPermissions(int userId);
    
    /**
     * 获取用户的所有权限详情（包含权限描述）
     * @param userId 用户ID
     * @return 权限对象列表
     */
    List<Permission> getUserPermissionDetails(int userId);
}
