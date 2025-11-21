package com.rbac.decorator;

import com.rbac.audit.AuditLogger;
import com.rbac.common.PermissionConsts;
import com.rbac.model.Role;
import com.rbac.model.User;
import com.rbac.service.AuthService;
import com.rbac.service.RoleService;
import com.rbac.service.UserService;

import java.util.List;

/**
 * 角色服务权限装饰器
 */
public class AuthRoleServiceDecorator implements RoleService {
    
    private final RoleService delegate;
    private final AuthService authService;
    private final UserService userService;
    
    public AuthRoleServiceDecorator(RoleService delegate, AuthService authService, UserService userService) {
        this.delegate = delegate;
        this.authService = authService;
        this.userService = userService;
    }
    
    @Override
    public void createRole(String roleCode, String roleName, String description) {
        authService.checkPermission(PermissionConsts.ROLE_CREATE);
        try {
            delegate.createRole(roleCode, roleName, description);
            AuditLogger.logCritical("CREATE_ROLE", roleCode, "创建角色成功");
        } catch (Exception e) {
            AuditLogger.logFail("CREATE_ROLE", roleCode, "创建角色失败: " + e.getMessage());
            throw e;
        }
    }
    
    @Override
    public void deleteRole(int roleId) {
        authService.checkPermission(PermissionConsts.ROLE_DELETE);
        try {
            Role role = delegate.getRoleById(roleId);
            delegate.deleteRole(roleId);
            AuditLogger.logCritical("DELETE_ROLE", role.getRoleCode(), "删除角色成功");
        } catch (Exception e) {
            AuditLogger.logFail("DELETE_ROLE", String.valueOf(roleId), "删除角色失败: " + e.getMessage());
            throw e;
        }
    }
    
    @Override
    public List<Role> listRoles() {
        authService.checkPermission(PermissionConsts.ROLE_LIST);
        return delegate.listRoles();
    }
    
    @Override
    public Role getRoleById(int roleId) {
        authService.checkPermission(PermissionConsts.ROLE_LIST);
        return delegate.getRoleById(roleId);
    }
    
    @Override
    public Role getRoleByCode(String roleCode) {
        authService.checkPermission(PermissionConsts.ROLE_LIST);
        return delegate.getRoleByCode(roleCode);
    }
    
    @Override
    public void assignRoleToUser(int userId, int roleId) {
        authService.checkPermission(PermissionConsts.ROLE_ASSIGN);
        try {
            User user = userService.getUserById(userId);
            Role role = delegate.getRoleById(roleId);
            delegate.assignRoleToUser(userId, roleId);
            AuditLogger.logCritical("ASSIGN_ROLE", user.getUsername(), 
                    String.format("为用户分配角色 [%s] 成功", role.getRoleCode()));
        } catch (Exception e) {
            AuditLogger.logFail("ASSIGN_ROLE", String.valueOf(userId), 
                    "分配角色失败: " + e.getMessage());
            throw e;
        }
    }
    
    @Override
    public void removeRoleFromUser(int userId, int roleId) {
        authService.checkPermission(PermissionConsts.ROLE_REVOKE);
        try {
            User user = userService.getUserById(userId);
            Role role = delegate.getRoleById(roleId);
            delegate.removeRoleFromUser(userId, roleId);
            AuditLogger.logCritical("REMOVE_ROLE", user.getUsername(), 
                    String.format("移除用户角色 [%s] 成功", role.getRoleCode()));
        } catch (Exception e) {
            AuditLogger.logFail("REMOVE_ROLE", String.valueOf(userId), 
                    "移除角色失败: " + e.getMessage());
            throw e;
        }
    }
    
    @Override
    public List<Role> getUserRoles(int userId) {
        authService.checkPermission(PermissionConsts.ROLE_LIST);
        return delegate.getUserRoles(userId);
    }
}
