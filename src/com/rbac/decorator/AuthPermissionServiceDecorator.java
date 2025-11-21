package com.rbac.decorator;

import com.rbac.audit.AuditLogger;
import com.rbac.common.PermissionConsts;
import com.rbac.model.Permission;
import com.rbac.model.Role;
import com.rbac.service.AuthService;
import com.rbac.service.PermissionService;
import com.rbac.service.RoleService;

import java.util.List;

/**
 * 权限服务权限装饰器
 */
public class AuthPermissionServiceDecorator implements PermissionService {
    
    private final PermissionService delegate;
    private final AuthService authService;
    private final RoleService roleService;
    
    public AuthPermissionServiceDecorator(PermissionService delegate, AuthService authService, RoleService roleService) {
        this.delegate = delegate;
        this.authService = authService;
        this.roleService = roleService;
    }
    
    @Override
    public void createPermission(String permissionCode, String description) {
        authService.checkPermission(PermissionConsts.PERM_CREATE);
        try {
            delegate.createPermission(permissionCode, description);
            AuditLogger.logCritical("CREATE_PERMISSION", permissionCode, "创建权限成功");
        } catch (Exception e) {
            AuditLogger.logFail("CREATE_PERMISSION", permissionCode, "创建权限失败: " + e.getMessage());
            throw e;
        }
    }
    
    @Override
    public void deletePermission(int permissionId) {
        authService.checkPermission(PermissionConsts.PERM_DELETE);
        try {
            Permission permission = delegate.getPermissionById(permissionId);
            delegate.deletePermission(permissionId);
            AuditLogger.logCritical("DELETE_PERMISSION", permission.getPermissionCode(), "删除权限成功");
        } catch (Exception e) {
            AuditLogger.logFail("DELETE_PERMISSION", String.valueOf(permissionId), "删除权限失败: " + e.getMessage());
            throw e;
        }
    }
    
    @Override
    public List<Permission> listPermissions() {
        authService.checkPermission(PermissionConsts.PERM_LIST);
        return delegate.listPermissions();
    }
    
    @Override
    public Permission getPermissionById(int permissionId) {
        authService.checkPermission(PermissionConsts.PERM_LIST);
        return delegate.getPermissionById(permissionId);
    }
    
    @Override
    public Permission getPermissionByCode(String permissionCode) {
        authService.checkPermission(PermissionConsts.PERM_LIST);
        return delegate.getPermissionByCode(permissionCode);
    }
    
    @Override
    public void assignPermissionToRole(int roleId, int permissionId) {
        authService.checkPermission(PermissionConsts.PERM_ASSIGN);
        try {
            Role role = roleService.getRoleById(roleId);
            Permission permission = delegate.getPermissionById(permissionId);
            delegate.assignPermissionToRole(roleId, permissionId);
            AuditLogger.logCritical("ASSIGN_PERMISSION", role.getRoleCode(), 
                    String.format("为角色分配权限 [%s] 成功", permission.getPermissionCode()));
        } catch (Exception e) {
            AuditLogger.logFail("ASSIGN_PERMISSION", String.valueOf(roleId), 
                    "分配权限失败: " + e.getMessage());
            throw e;
        }
    }
    
    @Override
    public void removePermissionFromRole(int roleId, int permissionId) {
        authService.checkPermission(PermissionConsts.PERM_REVOKE);
        try {
            Role role = roleService.getRoleById(roleId);
            Permission permission = delegate.getPermissionById(permissionId);
            delegate.removePermissionFromRole(roleId, permissionId);
            AuditLogger.logCritical("REMOVE_PERMISSION", role.getRoleCode(), 
                    String.format("移除角色权限 [%s] 成功", permission.getPermissionCode()));
        } catch (Exception e) {
            AuditLogger.logFail("REMOVE_PERMISSION", String.valueOf(roleId), 
                    "移除权限失败: " + e.getMessage());
            throw e;
        }
    }
    
    @Override
    public List<Permission> getRolePermissions(int roleId) {
        authService.checkPermission(PermissionConsts.PERM_LIST);
        return delegate.getRolePermissions(roleId);
    }
}

