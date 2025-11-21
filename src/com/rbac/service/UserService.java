package com.rbac.service;

import com.rbac.model.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 创建用户
     */
    void createUser(String username, String password);
    
    /**
     * 删除用户
     */
    void deleteUser(int userId);
    
    /**
     * 冻结用户
     */
    void freezeUser(int userId);
    
    /**
     * 解冻用户
     */
    void unfreezeUser(int userId);
    
    /**
     * 查询所有用户
     */
    List<User> listUsers();
    
    /**
     * 根据ID查询用户
     */
    User getUserById(int userId);
    
    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);
}
