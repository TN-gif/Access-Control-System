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
 * 权限服务实现类 - 负责权限的增删改查和角色-权限关联管理
 * 
 * <p>本类实现了权限管理的核心业务逻辑，包括：
 * <ul>
 *   <li>权限创建（权限编码唯一性检查）</li>
 *   <li>权限删除</li>
 *   <li>权限查询（按ID、按编码、列表查询）</li>
 *   <li>角色-权限关联管理（分配、移除、查询）</li>
 * </ul>
 * 
 * @author RBAC Team
 * @see PermissionService
 */
public class PermissionServiceImpl implements PermissionService {
    
    private final PermissionDao permissionDao;
    private final RoleDao roleDao;
    private final RolePermissionDao rolePermissionDao;
    
    /**
     * 构造函数 - 初始化DAO依赖
     */
    public PermissionServiceImpl() {
        this.permissionDao = new PermissionDao();
        this.roleDao = new RoleDao();
        this.rolePermissionDao = new RolePermissionDao();
    }
    
    /**
     * 创建新权限
     * 
     * @param permissionCode 权限编码，不能为空，必须唯一
     * @param description 权限描述，可以为空
     * @throws BusinessException 当权限编码为空、已存在或创建失败时
     */
    @Override
    public void createPermission(String permissionCode, String description) {
        // 验证输入
        validatePermissionCode(permissionCode);
        
        // 检查权限编码唯一性
        if (permissionDao.existsByCode(permissionCode)) {
            throw new BusinessException("权限编码已存在: " + permissionCode);
        }
        
        // 创建并保存权限
        Permission permission = new Permission();
        permission.setPermissionCode(permissionCode);
        permission.setDescription(description);
        
        int permissionId = permissionDao.insert(permission);
        if (permissionId <= 0) {
            throw new BusinessException("创建权限失败");
        }
    }
    
    /**
     * 验证权限编码
     * 
     * @param permissionCode 权限编码
     * @throws BusinessException 当权限编码为空时
     */
    private void validatePermissionCode(String permissionCode) {
        if (permissionCode == null || permissionCode.trim().isEmpty()) {
            throw new BusinessException("权限编码不能为空");
        }
    }
    
    /**
     * 删除权限
     * 
     * @param permissionId 要删除的权限ID
     * @throws BusinessException 当权限不存在或删除失败时
     */
    @Override
    public void deletePermission(int permissionId) {
        checkPermissionExists(permissionId);
        
        boolean success = permissionDao.deleteById(permissionId);
        if (!success) {
            throw new BusinessException("删除权限失败");
        }
    }
    
    /**
     * 查询所有权限列表
     * 
     * @return 所有权限列表（按ID排序）
     */
    @Override
    public List<Permission> listPermissions() {
        return permissionDao.findAll();
    }
    
    /**
     * 根据权限ID查询权限
     * 
     * @param permissionId 权限ID
     * @return 权限对象
     * @throws BusinessException 当权限不存在时
     */
    @Override
    public Permission getPermissionById(int permissionId) {
        Permission permission = permissionDao.findById(permissionId);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }
        return permission;
    }
    
    /**
     * 根据权限编码查询权限
     * 
     * @param permissionCode 权限编码
     * @return 权限对象
     * @throws BusinessException 当权限不存在时
     */
    @Override
    public Permission getPermissionByCode(String permissionCode) {
        Permission permission = permissionDao.findByCode(permissionCode);
        if (permission == null) {
            throw new BusinessException("权限不存在: " + permissionCode);
        }
        return permission;
    }
    
    /**
     * 为角色分配权限
     * 
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @throws BusinessException 当角色不存在、权限不存在、角色已拥有该权限或分配失败时
     */
    @Override
    public void assignPermissionToRole(int roleId, int permissionId) {
        // 验证角色和权限的存在性
        checkRoleExists(roleId);
        checkPermissionExists(permissionId);
        
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
    
    /**
     * 移除角色的权限
     * 
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @throws BusinessException 当角色不存在、权限不存在、角色未拥有该权限或移除失败时
     */
    @Override
    public void removePermissionFromRole(int roleId, int permissionId) {
        // 验证角色和权限的存在性
        checkRoleExists(roleId);
        checkPermissionExists(permissionId);
        
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
    
    /**
     * 查询角色的所有权限
     * 
     * @param roleId 角色ID
     * @return 角色的权限列表
     * @throws BusinessException 当角色不存在时
     */
    @Override
    public List<Permission> getRolePermissions(int roleId) {
        checkRoleExists(roleId);
        return permissionDao.findByRoleId(roleId);
    }
    
    /**
     * 检查角色是否存在
     * 
     * @param roleId 角色ID
     * @throws BusinessException 当角色不存在时
     */
    private void checkRoleExists(int roleId) {
        Role role = roleDao.findById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
    }
    
    /**
     * 检查权限是否存在
     * 
     * @param permissionId 权限ID
     * @throws BusinessException 当权限不存在时
     */
    private void checkPermissionExists(int permissionId) {
        Permission permission = permissionDao.findById(permissionId);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }
    }
}
