package com.rbac.service.impl;

import com.rbac.dao.UserDao;
import com.rbac.exception.BusinessException;
import com.rbac.model.User;
import com.rbac.service.UserService;
import com.rbac.util.PasswordUtil;

import java.util.List;

/**
 * 用户服务实现类
 */
public class UserServiceImpl implements UserService {
    
    private final UserDao userDao;
    
    public UserServiceImpl() {
        this.userDao = new UserDao();
    }
    
    @Override
    public void createUser(String username, String password) {
        // 验证输入
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        
        // 验证密码复杂度：至少8位，包含字母和数字
        if (password.length() < 8) {
            throw new BusinessException("密码长度至少为8位");
        }
        if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*")) {
            throw new BusinessException("密码必须包含字母和数字");
        }
        
        // 检查用户名是否已存在
        if (userDao.existsByUsername(username)) {
            throw new BusinessException("用户名已存在: " + username);
        }
        
        // 生成盐值和密码哈希
        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword(password, salt);
        
        // 创建用户对象
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setSalt(salt);
        user.setStatus(0); // 默认正常状态
        
        // 保存到数据库
        int userId = userDao.insert(user);
        if (userId <= 0) {
            throw new BusinessException("创建用户失败");
        }
    }
    
    @Override
    public void deleteUser(int userId) {
        // 检查用户是否存在
        User user = userDao.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 删除用户
        boolean success = userDao.deleteById(userId);
        if (!success) {
            throw new BusinessException("删除用户失败");
        }
    }
    
    @Override
    public void freezeUser(int userId) {
        // 检查用户是否存在
        User user = userDao.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查是否已经被冻结
        if (user.isFrozen()) {
            throw new BusinessException("用户已被冻结");
        }
        
        // 冻结用户
        boolean success = userDao.updateStatus(userId, 1);
        if (!success) {
            throw new BusinessException("冻结用户失败");
        }
    }
    
    @Override
    public void unfreezeUser(int userId) {
        // 检查用户是否存在
        User user = userDao.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查是否已经是正常状态
        if (user.isNormal()) {
            throw new BusinessException("用户已是正常状态");
        }
        
        // 解冻用户
        boolean success = userDao.updateStatus(userId, 0);
        if (!success) {
            throw new BusinessException("解冻用户失败");
        }
    }
    
    @Override
    public List<User> listUsers() {
        return userDao.findAll();
    }
    
    @Override
    public User getUserById(int userId) {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
    
    @Override
    public User getUserByUsername(String username) {
        return userDao.findByUsername(username);
    }
}
