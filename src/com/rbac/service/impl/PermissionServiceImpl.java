package com.rbac.service.impl;

import com.rbac.dao.PermissionDao;
import com.rbac.dao.RoleDao;
import com.rbac.dao.RolePermissionDao;
import com.rbac.exception.BusinessException;
import com.rbac.model.Permission;
import com.rbac.model.Role;
import com.rbac.service.PermissionService;

import java.util.List;

/**
 * 权限服务实现类
 */
public class PermissionServiceImpl implements PermissionService {
    
    private final PermissionDao permissionDao;
    private final RoleDao roleDao;
    private final RolePermissionDao rolePermissionDao;
    
    public PermissionServiceImpl() {
        this.permissionDao = new PermissionDao();
        this.roleDao = new RoleDao();
        this.rolePermissionDao = new RolePermissionDao();
    }
    
    @Override
    public void createPermission(String permissionCode, String description) {
        // 验证输入
        if (permissionCode == null || permissionCode.trim().isEmpty()) {
            throw new BusinessException("权限编码不能为空");
        }
        
        // 检查权限编码是否已存在
        if (permissionDao.existsByCode(permissionCode)) {
            throw new BusinessException("权限编码已存在: " + permissionCode);
        }
        
        // 创建权限对象
        Permission permission = new Permission();
        permission.setPermissionCode(permissionCode);
        permission.setDescription(description);
        
        // 保存到数据库
        int permissionId = permissionDao.insert(permission);
        if (permissionId <= 0) {
            throw new BusinessException("创建权限失败");
        }
    }
    
    @Override
    public void deletePermission(int permissionId) {
        // 检查权限是否存在
        Permission permission = permissionDao.findById(permissionId);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }
        
        // 删除权限
        boolean success = permissionDao.deleteById(permissionId);
        if (!success) {
            throw new BusinessException("删除权限失败");
        }
    }
    
    @Override
    public List<Permission> listPermissions() {
        return permissionDao.findAll();
    }
    
    @Override
    public Permission getPermissionById(int permissionId) {
        Permission permission = permissionDao.findById(permissionId);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }
        return permission;
    }
    
    @Override
    public Permission getPermissionByCode(String permissionCode) {
        Permission permission = permissionDao.findByCode(permissionCode);
        if (permission == null) {
            throw new BusinessException("权限不存在: " + permissionCode);
        }
        return permission;
    }
    
    @Override
    public void assignPermissionToRole(int roleId, int permissionId) {
        // 检查角色是否存在
        Role role = roleDao.findById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        
        // 检查权限是否存在
        Permission permission = permissionDao.findById(permissionId);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }
        
        // 检查是否已经分配
        if (rolePermissionDao.hasPermission(roleId, permissionId)) {
            throw new BusinessException("角色已拥有该权限");
        }
        
        // 分配权限
        boolean success = rolePermissionDao.assignPermission(roleId, permissionId);
        if (!success) {
            throw new BusinessException("分配权限失败");
        }
    }
    
    @Override
    public void removePermissionFromRole(int roleId, int permissionId) {
        // 检查角色是否存在
        Role role = roleDao.findById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        
        // 检查权限是否存在
        Permission permission = permissionDao.findById(permissionId);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }
        
        // 检查是否拥有该权限
        if (!rolePermissionDao.hasPermission(roleId, permissionId)) {
            throw new BusinessException("角色未拥有该权限");
        }
        
        // 移除权限
        boolean success = rolePermissionDao.removePermission(roleId, permissionId);
        if (!success) {
            throw new BusinessException("移除权限失败");
        }
    }
    
    @Override
    public List<Permission> getRolePermissions(int roleId) {
        // 检查角色是否存在
        Role role = roleDao.findById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        
        return permissionDao.findByRoleId(roleId);
    }
}
