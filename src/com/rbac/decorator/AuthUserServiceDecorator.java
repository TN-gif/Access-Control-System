package com.rbac.decorator;

import com.rbac.audit.AuditLogger;
import com.rbac.common.PermissionConsts;
import com.rbac.model.User;
import com.rbac.service.AuthService;
import com.rbac.service.UserService;

import java.util.List;

/**
 * 用户服务权限装饰器
 */
public class AuthUserServiceDecorator implements UserService {
    
    private final UserService delegate;
    private final AuthService authService;
    
    public AuthUserServiceDecorator(UserService delegate, AuthService authService) {
        this.delegate = delegate;
        this.authService = authService;
    }
    
    @Override
    public void createUser(String username, String password) {
        authService.checkPermission(PermissionConsts.USER_CREATE);
        try {
            delegate.createUser(username, password);
            AuditLogger.logCritical("CREATE_USER", username, "创建用户成功");
        } catch (Exception e) {
            AuditLogger.logFail("CREATE_USER", username, "创建用户失败: " + e.getMessage());
            throw e;
        }
    }
    
    @Override
    public void deleteUser(int userId) {
        authService.checkPermission(PermissionConsts.USER_DELETE);
        try {
            User user = delegate.getUserById(userId);
            delegate.deleteUser(userId);
            AuditLogger.logCritical("DELETE_USER", user.getUsername(), "删除用户成功");
        } catch (Exception e) {
            AuditLogger.logFail("DELETE_USER", String.valueOf(userId), "删除用户失败: " + e.getMessage());
            throw e;
        }
    }
    
    @Override
    public void freezeUser(int userId) {
        authService.checkPermission(PermissionConsts.USER_FREEZE);
        try {
            User user = delegate.getUserById(userId);
            delegate.freezeUser(userId);
            AuditLogger.logCritical("FREEZE_USER", user.getUsername(), "冻结用户成功");
        } catch (Exception e) {
            AuditLogger.logFail("FREEZE_USER", String.valueOf(userId), "冻结用户失败: " + e.getMessage());
            throw e;
        }
    }
    
    @Override
    public void unfreezeUser(int userId) {
        authService.checkPermission(PermissionConsts.USER_UNFREEZE);
        try {
            User user = delegate.getUserById(userId);
            delegate.unfreezeUser(userId);
            AuditLogger.logCritical("UNFREEZE_USER", user.getUsername(), "解冻用户成功");
        } catch (Exception e) {
            AuditLogger.logFail("UNFREEZE_USER", String.valueOf(userId), "解冻用户失败: " + e.getMessage());
            throw e;
        }
    }
    
    @Override
    public List<User> listUsers() {
        authService.checkPermission(PermissionConsts.USER_LIST);
        return delegate.listUsers();
    }
    
    @Override
    public User getUserById(int userId) {
        authService.checkPermission(PermissionConsts.USER_LIST);
        return delegate.getUserById(userId);
    }
    
    @Override
    public User getUserByUsername(String username) {
        authService.checkPermission(PermissionConsts.USER_LIST);
        return delegate.getUserByUsername(username);
    }
}
