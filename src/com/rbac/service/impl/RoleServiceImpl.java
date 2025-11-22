package com.rbac.service.impl;

import com.rbac.dao.RoleDao;
import com.rbac.dao.UserDao;
import com.rbac.dao.UserRoleDao;
import com.rbac.exception.BusinessException;
import com.rbac.model.Role;
import com.rbac.model.User;
import com.rbac.service.RoleService;

import java.util.List;

/**
 * 角色服务实现类 - 负责角色的增删改查和用户-角色关联管理
 * 
 * <p>本类实现了角色管理的核心业务逻辑，包括：
 * <ul>
 *   <li>角色创建（角色编码唯一性检查）</li>
 *   <li>角色删除</li>
 *   <li>角色查询（按ID、按编码、列表查询）</li>
 *   <li>用户-角色关联管理（分配、移除、查询）</li>
 * </ul>
 * 
 * @author RBAC Team
 * @see RoleService
 */
public class RoleServiceImpl implements RoleService {
    
    private final RoleDao roleDao;
    private final UserDao userDao;
    private final UserRoleDao userRoleDao;
    
    /**
     * 构造函数 - 初始化DAO依赖
     */
    public RoleServiceImpl() {
        this.roleDao = new RoleDao();
        this.userDao = new UserDao();
        this.userRoleDao = new UserRoleDao();
    }
    
    /**
     * 创建新角色
     * 
     * @param roleCode 角色编码，不能为空，必须唯一
     * @param roleName 角色名称，不能为空
     * @param description 角色描述，可以为空
     * @throws BusinessException 当输入为空、角色编码已存在或创建失败时
     */
    @Override
    public void createRole(String roleCode, String roleName, String description) {
        // 验证输入
        validateRoleInput(roleCode, roleName);
        
        // 检查角色编码唯一性
        if (roleDao.existsByCode(roleCode)) {
            throw new BusinessException("角色编码已存在: " + roleCode);
        }
        
        // 创建并保存角色
        Role role = new Role();
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setDescription(description);
        
        int roleId = roleDao.insert(role);
        if (roleId <= 0) {
            throw new BusinessException("创建角色失败");
        }
    }
    
    /**
     * 验证角色输入参数
     * 
     * @param roleCode 角色编码
     * @param roleName 角色名称
     * @throws BusinessException 当角色编码或角色名称为空时
     */
    private void validateRoleInput(String roleCode, String roleName) {
        if (roleCode == null || roleCode.trim().isEmpty()) {
            throw new BusinessException("角色编码不能为空");
        }
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new BusinessException("角色名称不能为空");
        }
    }
    
    /**
     * 删除角色
     * 
     * @param roleId 要删除的角色ID
     * @throws BusinessException 当角色不存在或删除失败时
     */
    @Override
    public void deleteRole(int roleId) {
        checkRoleExists(roleId);
        
        boolean success = roleDao.deleteById(roleId);
        if (!success) {
            throw new BusinessException("删除角色失败");
        }
    }
    
    /**
     * 查询所有角色列表
     * 
     * @return 所有角色列表（按ID排序）
     */
    @Override
    public List<Role> listRoles() {
        return roleDao.findAll();
    }
    
    /**
     * 根据角色ID查询角色
     * 
     * @param roleId 角色ID
     * @return 角色对象
     * @throws BusinessException 当角色不存在时
     */
    @Override
    public Role getRoleById(int roleId) {
        Role role = roleDao.findById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }
    
    /**
     * 根据角色编码查询角色
     * 
     * @param roleCode 角色编码
     * @return 角色对象
     * @throws BusinessException 当角色不存在时
     */
    @Override
    public Role getRoleByCode(String roleCode) {
        Role role = roleDao.findByCode(roleCode);
        if (role == null) {
            throw new BusinessException("角色不存在: " + roleCode);
        }
        return role;
    }
    
    /**
     * 为用户分配角色
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     * @throws BusinessException 当用户不存在、角色不存在、用户已拥有该角色或分配失败时
     */
    @Override
    public void assignRoleToUser(int userId, int roleId) {
        // 验证用户和角色的存在性
        checkUserExists(userId);
        checkRoleExists(roleId);
        
        // 检查是否已经分配
        if (userRoleDao.hasRole(userId, roleId)) {
            throw new BusinessException("用户已拥有该角色");
        }
        
        // 分配角色
        boolean success = userRoleDao.assignRole(userId, roleId);
        if (!success) {
            throw new BusinessException("分配角色失败");
        }
    }
    
    /**
     * 移除用户的角色
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     * @throws BusinessException 当用户不存在、角色不存在、用户未拥有该角色或移除失败时
     */
    @Override
    public void removeRoleFromUser(int userId, int roleId) {
        // 验证用户和角色的存在性
        checkUserExists(userId);
        checkRoleExists(roleId);
        
        // 检查是否拥有该角色
        if (!userRoleDao.hasRole(userId, roleId)) {
            throw new BusinessException("用户未拥有该角色");
        }
        
        // 移除角色
        boolean success = userRoleDao.removeRole(userId, roleId);
        if (!success) {
            throw new BusinessException("移除角色失败");
        }
    }
    
    /**
     * 查询用户的所有角色
     * 
     * @param userId 用户ID
     * @return 用户的角色列表
     * @throws BusinessException 当用户不存在时
     */
    @Override
    public List<Role> getUserRoles(int userId) {
        checkUserExists(userId);
        return roleDao.findByUserId(userId);
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
}
