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
 * 角色服务实现类
 */
public class RoleServiceImpl implements RoleService {
    
    private final RoleDao roleDao;
    private final UserDao userDao;
    private final UserRoleDao userRoleDao;
    
    public RoleServiceImpl() {
        this.roleDao = new RoleDao();
        this.userDao = new UserDao();
        this.userRoleDao = new UserRoleDao();
    }
    
    @Override
    public void createRole(String roleCode, String roleName, String description) {
        // 验证输入
        if (roleCode == null || roleCode.trim().isEmpty()) {
            throw new BusinessException("角色编码不能为空");
        }
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new BusinessException("角色名称不能为空");
        }
        
        // 检查角色编码是否已存在
        if (roleDao.existsByCode(roleCode)) {
            throw new BusinessException("角色编码已存在: " + roleCode);
        }
        
        // 创建角色对象
        Role role = new Role();
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setDescription(description);
        
        // 保存到数据库
        int roleId = roleDao.insert(role);
        if (roleId <= 0) {
            throw new BusinessException("创建角色失败");
        }
    }
    
    @Override
    public void deleteRole(int roleId) {
        // 检查角色是否存在
        Role role = roleDao.findById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        
        // 删除角色
        boolean success = roleDao.deleteById(roleId);
        if (!success) {
            throw new BusinessException("删除角色失败");
        }
    }
    
    @Override
    public List<Role> listRoles() {
        return roleDao.findAll();
    }
    
    @Override
    public Role getRoleById(int roleId) {
        Role role = roleDao.findById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }
    
    @Override
    public Role getRoleByCode(String roleCode) {
        Role role = roleDao.findByCode(roleCode);
        if (role == null) {
            throw new BusinessException("角色不存在: " + roleCode);
        }
        return role;
    }
    
    @Override
    public void assignRoleToUser(int userId, int roleId) {
        // 检查用户是否存在
        User user = userDao.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查角色是否存在
        Role role = roleDao.findById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        
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
    
    @Override
    public void removeRoleFromUser(int userId, int roleId) {
        // 检查用户是否存在
        User user = userDao.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查角色是否存在
        Role role = roleDao.findById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        
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
    
    @Override
    public List<Role> getUserRoles(int userId) {
        // 检查用户是否存在
        User user = userDao.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        return roleDao.findByUserId(userId);
    }
}
