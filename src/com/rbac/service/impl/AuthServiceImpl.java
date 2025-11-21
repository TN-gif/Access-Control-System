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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 认证服务实现类
 */
public class AuthServiceImpl implements AuthService {
    
    private final UserDao userDao;
    private final PermissionDao permissionDao;
    
    public AuthServiceImpl() {
        this.userDao = new UserDao();
        this.permissionDao = new PermissionDao();
    }
    
    @Override
    public User login(String username, String password) {
        // 验证输入
        if (username == null || username.trim().isEmpty()) {
            AuditLogger.logLoginFail(username, "用户名为空");
            throw new AuthenticationException("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            AuditLogger.logLoginFail(username, "密码为空");
            throw new AuthenticationException("密码不能为空");
        }
        
        // 查询用户
        User user = userDao.findByUsername(username);
        if (user == null) {
            AuditLogger.logLoginFail(username, "用户不存在");
            throw new AuthenticationException("用户名或密码错误");
        }
        
        // 验证密码
        boolean passwordMatch = PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash());
        if (!passwordMatch) {
            AuditLogger.logLoginFail(username, "密码错误");
            throw new AuthenticationException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.isFrozen()) {
            AuditLogger.logLoginFail(username, "账户已被冻结");
            throw new AuthenticationException("账户已被冻结，请联系管理员");
        }
        
        // 设置当前会话
        SessionContext.setCurrentUser(user);
        
        // 记录登录成功
        AuditLogger.logLoginSuccess(username);
        
        return user;
    }
    
    @Override
    public void logout() {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser != null) {
            AuditLogger.logLogout(currentUser.getUsername());
            SessionContext.clear();
        }
    }
    
    @Override
    public User getCurrentUser() {
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            throw new AuthenticationException("未登录或会话已过期");
        }
        return user;
    }
    
    @Override
    public boolean hasPermission(String permissionCode) {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        Set<String> permissions = getUserPermissions(currentUser.getId());
        return permissions.contains(permissionCode);
    }
    
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
    
    @Override
    public Set<String> getUserPermissions(int userId) {
        List<Permission> permissions = permissionDao.findByUserId(userId);
        Set<String> permissionCodes = new HashSet<>();
        for (Permission permission : permissions) {
            permissionCodes.add(permission.getPermissionCode());
        }
        return permissionCodes;
    }
    
    @Override
    public List<Permission> getUserPermissionDetails(int userId) {
        return permissionDao.findByUserId(userId);
    }
}
