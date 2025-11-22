package com.rbac.service.impl;

import com.rbac.audit.AuditLogger;
import com.rbac.dao.PermissionDao;
import com.rbac.dao.UserDao;
import com.rbac.exception.AuthenticationException;
import com.rbac.exception.PermissionDeniedException;
import com.rbac.model.Permission;
import com.rbac.model.User;
import com.rbac.service.AuthService;
import com.rbac.util.PasswordUtil;
import com.rbac.util.SessionContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证服务实现类 - 负责用户认证、权限验证和会话管理
 * 
 * <p>本类实现了基于RBAC模型的完整认证流程，包括：
 * <ul>
 *   <li>用户登录认证（用户名密码验证、账户状态检查）</li>
 *   <li>用户登出和会话管理</li>
 *   <li>权限验证（基于角色的权限检查）</li>
 *   <li>审计日志记录（登录成功/失败、权限检查失败）</li>
 * </ul>
 * 
 * @author RBAC Team
 * @see AuthService
 * @see SessionContext
 */
public class AuthServiceImpl implements AuthService {
    
    private final UserDao userDao;
    private final PermissionDao permissionDao;
    
    /**
     * 构造函数 - 初始化DAO依赖
     */
    public AuthServiceImpl() {
        this.userDao = new UserDao();
        this.permissionDao = new PermissionDao();
    }
    
    /**
     * 用户登录
     * 
     * <p>执行完整的登录验证流程：
     * <ol>
     *   <li>验证输入参数（用户名和密码非空）</li>
     *   <li>查询用户并验证凭据（密码哈希比对）</li>
     *   <li>检查账户状态（是否被冻结）</li>
     *   <li>建立会话并记录审计日志</li>
     * </ol>
     * 
     * @param username 用户名，不能为空
     * @param password 密码明文，不能为空
     * @return 登录成功的用户对象
     * @throws AuthenticationException 当输入为空、用户不存在、密码错误或账户被冻结时
     */
    @Override
    public User login(String username, String password) {
        // 步骤1：验证输入参数
        validateLoginInput(username, password);
        
        // 步骤2：验证用户凭据（查询用户并验证密码）
        User user = authenticateCredentials(username, password);
        
        // 步骤3：检查账户状态
        validateUserStatus(user, username);
        
        // 步骤4：建立会话并记录成功日志
        SessionContext.setCurrentUser(user);
        AuditLogger.logLoginSuccess(username);
        
        return user;
    }
    
    /**
     * 验证登录输入参数
     * 
     * @param username 用户名
     * @param password 密码
     * @throws AuthenticationException 当用户名或密码为空时
     */
    private void validateLoginInput(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            AuditLogger.logLoginFail(username, "用户名为空");
            throw new AuthenticationException("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            AuditLogger.logLoginFail(username, "密码为空");
            throw new AuthenticationException("密码不能为空");
        }
    }
    
    /**
     * 验证用户凭据（查询用户并验证密码）
     * 
     * <p>为了安全考虑，当用户不存在或密码错误时，返回相同的错误消息
     * 
     * @param username 用户名
     * @param password 密码明文
     * @return 验证通过的用户对象
     * @throws AuthenticationException 当用户不存在或密码错误时
     */
    private User authenticateCredentials(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user == null) {
            AuditLogger.logLoginFail(username, "用户不存在");
            throw new AuthenticationException("用户名或密码错误");
        }
        
        boolean passwordMatch = PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash());
        if (!passwordMatch) {
            AuditLogger.logLoginFail(username, "密码错误");
            throw new AuthenticationException("用户名或密码错误");
        }
        
        return user;
    }
    
    /**
     * 验证用户账户状态
     * 
     * @param user 用户对象
     * @param username 用户名（用于日志记录）
     * @throws AuthenticationException 当账户被冻结时
     */
    private void validateUserStatus(User user, String username) {
        if (user.isFrozen()) {
            AuditLogger.logLoginFail(username, "账户已被冻结");
            throw new AuthenticationException("账户已被冻结，请联系管理员");
        }
    }
    
    /**
     * 用户登出
     * 
     * <p>清除当前会话并记录登出日志
     * <p>如果当前未登录，则此方法不执行任何操作
     */
    @Override
    public void logout() {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser != null) {
            AuditLogger.logLogout(currentUser.getUsername());
            SessionContext.clear();
        }
    }
    
    /**
     * 获取当前登录用户
     * 
     * @return 当前登录的用户对象
     * @throws AuthenticationException 当用户未登录或会话已过期时
     */
    @Override
    public User getCurrentUser() {
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            throw new AuthenticationException("未登录或会话已过期");
        }
        return user;
    }
    
    /**
     * 检查当前用户是否拥有指定权限
     * 
     * @param permissionCode 权限编码
     * @return 如果用户拥有该权限返回true，否则返回false；未登录时返回false
     */
    @Override
    public boolean hasPermission(String permissionCode) {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        Set<String> permissions = getUserPermissions(currentUser.getId());
        return permissions.contains(permissionCode);
    }
    
    /**
     * 检查权限，无权限时抛出异常
     * 
     * <p>此方法用于需要强制权限验证的场景，失败时会记录审计日志
     * 
     * @param permissionCode 权限编码
     * @throws PermissionDeniedException 当用户未登录或没有该权限时
     */
    @Override
    public void checkPermission(String permissionCode) {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) {
            throw new PermissionDeniedException("未登录，无法执行此操作");
        }
        
        if (!hasPermission(permissionCode)) {
            String message = String.format("权限不足：需要权限 [%s]", permissionCode);
            AuditLogger.logFail("PERMISSION_CHECK", permissionCode, message);
            throw new PermissionDeniedException(message);
        }
    }
    
    /**
     * 获取用户的所有权限编码集合
     * 
     * <p>使用Stream API优化集合转换，避免手动循环
     * 
     * @param userId 用户ID
     * @return 权限编码集合
     */
    @Override
    public Set<String> getUserPermissions(int userId) {
        return permissionDao.findByUserId(userId).stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toSet());
    }
    
    /**
     * 获取用户的所有权限详情（包含权限描述）
     * 
     * @param userId 用户ID
     * @return 权限对象列表
     */
    @Override
    public List<Permission> getUserPermissionDetails(int userId) {
        return permissionDao.findByUserId(userId);
    }
}
