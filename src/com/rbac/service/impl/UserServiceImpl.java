package com.rbac.service.impl;

import com.rbac.dao.UserDao;
import com.rbac.exception.BusinessException;
import com.rbac.model.User;
import com.rbac.service.UserService;
import com.rbac.util.PasswordUtil;

import java.util.List;

/**
 * 用户服务实现类 - 负责用户的增删改查和状态管理
 * 
 * <p>本类实现了用户管理的核心业务逻辑，包括：
 * <ul>
 *   <li>用户创建（用户名唯一性检查、密码复杂度验证）</li>
 *   <li>用户删除</li>
 *   <li>用户状态管理（冻结、解冻）</li>
 *   <li>用户查询（按ID、按用户名、列表查询）</li>
 * </ul>
 * 
 * <p><b>密码复杂度要求：</b>
 * <ul>
 *   <li>长度至少8位</li>
 *   <li>必须包含字母和数字</li>
 * </ul>
 * 
 * @author RBAC Team
 * @see UserService
 */
public class UserServiceImpl implements UserService {
    
    private final UserDao userDao;
    
    /**
     * 构造函数 - 初始化DAO依赖
     */
    public UserServiceImpl() {
        this.userDao = new UserDao();
    }
    
    /**
     * 创建新用户
     * 
     * <p>执行以下步骤：
     * <ol>
     *   <li>验证用户名和密码非空</li>
     *   <li>验证密码复杂度（至少8位，包含字母和数字）</li>
     *   <li>检查用户名是否已存在</li>
     *   <li>生成盐值和密码哈希</li>
     *   <li>保存到数据库</li>
     * </ol>
     * 
     * @param username 用户名，不能为空
     * @param password 密码明文，不能为空
     * @throws BusinessException 当输入为空、密码不符合复杂度要求或用户名已存在时
     */
    @Override
    public void createUser(String username, String password) {
        // 步骤1：验证基本输入
        validateUserInput(username, password);
        
        // 步骤2：验证密码复杂度
        validatePasswordStrength(password);
        
        // 步骤3：检查用户名唯一性
        if (userDao.existsByUsername(username)) {
            throw new BusinessException("用户名已存在: " + username);
        }
        
        // 步骤4：生成盐值和密码哈希
        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword(password, salt);
        
        // 步骤5：创建并保存用户
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setSalt(salt);
        user.setStatus(0); // 0=正常, 1=冻结
        
        int userId = userDao.insert(user);
        if (userId <= 0) {
            throw new BusinessException("创建用户失败");
        }
    }
    
    /**
     * 验证用户输入参数
     * 
     * @param username 用户名
     * @param password 密码
     * @throws BusinessException 当用户名或密码为空时
     */
    private void validateUserInput(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
    }
    
    /**
     * 验证密码强度
     * 
     * <p>密码必须满足以下条件：
     * <ul>
     *   <li>长度至少8位</li>
     *   <li>包含至少一个字母（a-z或A-Z）</li>
     *   <li>包含至少一个数字（0-9）</li>
     * </ul>
     * 
     * @param password 密码明文
     * @throws BusinessException 当密码不符合强度要求时
     */
    private void validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new BusinessException("密码长度至少为8位");
        }
        if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*")) {
            throw new BusinessException("密码必须包含字母和数字");
        }
    }
    
    /**
     * 删除用户
     * 
     * @param userId 要删除的用户ID
     * @throws BusinessException 当用户不存在或删除失败时
     */
    @Override
    public void deleteUser(int userId) {
        // 检查用户是否存在
        checkUserExists(userId);
        
        // 删除用户
        boolean success = userDao.deleteById(userId);
        if (!success) {
            throw new BusinessException("删除用户失败");
        }
    }
    
    /**
     * 冻结用户账户
     * 
     * <p>被冻结的用户无法登录系统
     * 
     * @param userId 要冻结的用户ID
     * @throws BusinessException 当用户不存在、已被冻结或操作失败时
     */
    @Override
    public void freezeUser(int userId) {
        User user = getUserById(userId);
        
        if (user.isFrozen()) {
            throw new BusinessException("用户已被冻结");
        }
        
        updateUserStatusInternal(userId, 1, "冻结");
    }
    
    /**
     * 解冻用户账户
     * 
     * <p>解冻后用户可以正常登录系统
     * 
     * @param userId 要解冻的用户ID
     * @throws BusinessException 当用户不存在、已是正常状态或操作失败时
     */
    @Override
    public void unfreezeUser(int userId) {
        User user = getUserById(userId);
        
        if (user.isNormal()) {
            throw new BusinessException("用户已是正常状态");
        }
        
        updateUserStatusInternal(userId, 0, "解冻");
    }
    
    /**
     * 内部方法：更新用户状态
     * 
     * <p>此方法封装了状态更新的通用逻辑，避免freezeUser和unfreezeUser之间的重复代码
     * 
     * @param userId 用户ID
     * @param status 目标状态（0=正常, 1=冻结）
     * @param operationName 操作名称（用于错误消息）
     * @throws BusinessException 当更新失败时
     */
    private void updateUserStatusInternal(int userId, int status, String operationName) {
        boolean success = userDao.updateStatus(userId, status);
        if (!success) {
            throw new BusinessException(operationName + "用户失败");
        }
    }
    
    /**
     * 查询所有用户列表
     * 
     * @return 所有用户列表（按ID排序）
     */
    @Override
    public List<User> listUsers() {
        return userDao.findAll();
    }
    
    /**
     * 根据用户ID查询用户
     * 
     * @param userId 用户ID
     * @return 用户对象
     * @throws BusinessException 当用户不存在时
     */
    @Override
    public User getUserById(int userId) {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户对象，如果不存在则返回null
     */
    @Override
    public User getUserByUsername(String username) {
        return userDao.findByUsername(username);
    }
    
    /**
     * 检查用户是否存在
     * 
     * @param userId 用户ID
     * @throws BusinessException 当用户不存在时
     */
    private void checkUserExists(int userId) {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
    }
}
