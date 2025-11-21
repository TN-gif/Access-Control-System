package com.rbac.cli;

import com.rbac.audit.AuditAnalyzer;
import com.rbac.common.PermissionConsts;
import com.rbac.exception.AuthenticationException;
import com.rbac.exception.BusinessException;
import com.rbac.exception.PermissionDeniedException;
import com.rbac.model.Permission;
import com.rbac.model.Role;
import com.rbac.model.User;
import com.rbac.service.AuthService;
import com.rbac.service.PermissionService;
import com.rbac.service.RoleService;
import com.rbac.service.UserService;

import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * 菜单处理器 - 处理各个菜单的交互逻辑
 */
public class MenuHandler {
    
    private final Scanner scanner;
    private final AuthService authService;
    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final AuditAnalyzer auditAnalyzer;
    
    public MenuHandler(Scanner scanner, AuthService authService, UserService userService,
                      RoleService roleService, PermissionService permissionService,
                      AuditAnalyzer auditAnalyzer) {
        this.scanner = scanner;
        this.authService = authService;
        this.userService = userService;
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.auditAnalyzer = auditAnalyzer;
    }
    
    /**
     * 询问用户是否重试
     */
    private boolean askForRetry() {
        System.out.print("是否重试？(y/n): ");
        String retry = scanner.nextLine().trim().toLowerCase();
        return retry.equals("y") || retry.equals("yes");
    }
    
    /**
     * 可取消的输入
     * @param prompt 提示信息
     * @return 用户输入，如果用户取消则返回null
     */
    private String cancelableInput(String prompt) {
        System.out.print(prompt + " (输入 '0' 或 'q' 返回): ");
        String input = scanner.nextLine().trim();
        if (input.equals("0") || input.equalsIgnoreCase("q")) {
            System.out.println("✓ 操作已取消");
            return null;
        }
        return input;
    }

    
    /**
     * 处理登录
     */
    public void handleLogin() {
        System.out.println("\n========== 用户登录 ==========");
        
        // 允许重试
        while (true) {
            System.out.print("请输入用户名: ");
            String username = scanner.nextLine().trim();
            
            System.out.print("请输入密码: ");
            String password = scanner.nextLine().trim();
            
            try {
                User user = authService.login(username, password);
                System.out.println("✓ 登录成功！欢迎，" + user.getUsername());
                break; // 成功后退出
            } catch (AuthenticationException e) {
                System.out.println("✗ 登录失败: " + e.getMessage());
                if (!askForRetry()) {
                    break; // 用户选择不重试
                }
            }
        }
    }
    
    /**
     * 处理登出
     */
    public void handleLogout() {
        try {
            User currentUser = authService.getCurrentUser();
            authService.logout();
            System.out.println("✓ " + currentUser.getUsername() + " 已登出系统");
        } catch (AuthenticationException e) {
            System.out.println("✗ 当前未登录");
        }
    }
    
    /**
     * 用户管理菜单
     */
    public void handleUserMenu() {
        while (true) {
            System.out.println("\n------ 用户管理 ------");
            System.out.println("1. 创建用户");
            System.out.println("2. 删除用户");
            System.out.println("3. 冻结用户");
            System.out.println("4. 解冻用户");
            System.out.println("5. 查看所有用户");
            System.out.println("0. 返回上级菜单");
            System.out.println("----------------------");
            System.out.print("请输入操作编号: ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    handleCreateUser();
                    break;
                case "2":
                    handleDeleteUser();
                    break;
                case "3":
                    handleFreezeUser();
                    break;
                case "4":
                    handleUnfreezeUser();
                    break;
                case "5":
                    handleListUsers();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("✗ 无效的选项，请重新输入");
            }
        }
    }
    
    private void handleCreateUser() {
        // 提前检查权限
        try {
            authService.checkPermission(PermissionConsts.USER_CREATE);
        } catch (PermissionDeniedException e) {
            System.out.println("✗ 权限不足: " + e.getMessage());
            return;
        }
        
        // 允许重试
        while (true) {
            String username = cancelableInput("请输入用户名");
            if (username == null) return;  // 用户取消
            
            String password = cancelableInput("请输入密码");
            if (password == null) return;  // 用户取消
            
            try {
                userService.createUser(username, password);
                System.out.println("✓ 创建用户 [" + username + "] 成功");
                break;
            } catch (BusinessException e) {
                System.out.println("✗ 创建用户失败: " + e.getMessage());
                if (!askForRetry()) {
                    break;
                }
            }
        }
    }
    
    private void handleDeleteUser() {
        // 提前检查权限
        try {
            authService.checkPermission(PermissionConsts.USER_DELETE);
        } catch (PermissionDeniedException e) {
            System.out.println("✗ 权限不足: " + e.getMessage());
            return;
        }
        
        handleListUsers();
        String input = cancelableInput("请输入要删除的用户ID");
        if (input == null) return;  // 用户取消
        
        try {
            int userId = Integer.parseInt(input);
            userService.deleteUser(userId);
            System.out.println("✓ 删除用户成功");
        } catch (NumberFormatException e) {
            System.out.println("✗ 无效的用户ID");
        } catch (BusinessException e) {
            System.out.println("✗ 删除用户失败: " + e.getMessage());
        }
    }
    
    private void handleFreezeUser() {
        // 提前检查权限
        try {
            authService.checkPermission(PermissionConsts.USER_FREEZE);
        } catch (PermissionDeniedException e) {
            System.out.println("✗ 权限不足: " + e.getMessage());
            return;
        }
        
        handleListUsers();
        String input = cancelableInput("请输入要冻结的用户ID");
        if (input == null) return;  // 用户取消
        
        try {
            int userId = Integer.parseInt(input);
            userService.freezeUser(userId);
            System.out.println("✓ 冻结用户成功");
        } catch (NumberFormatException e) {
            System.out.println("✗ 无效的用户ID");
        } catch (BusinessException e) {
            System.out.println("✗ 冻结用户失败: " + e.getMessage());
        }
    }
    
    private void handleUnfreezeUser() {
        // 提前检查权限
        try {
            authService.checkPermission(PermissionConsts.USER_UNFREEZE);
        } catch (PermissionDeniedException e) {
            System.out.println("✗ 权限不足: " + e.getMessage());
            return;
        }
        
        handleListUsers();
        String input = cancelableInput("请输入要解冻的用户ID");
        if (input == null) return;  // 用户取消
        
        try {
            int userId = Integer.parseInt(input);
            userService.unfreezeUser(userId);
            System.out.println("✓ 解冻用户成功");
        } catch (NumberFormatException e) {
            System.out.println("✗ 无效的用户ID");
        } catch (BusinessException e) {
            System.out.println("✗ 解冻用户失败: " + e.getMessage());
        }
    }
    
    private void handleListUsers() {
        // 提前检查权限
        try {
            authService.checkPermission(PermissionConsts.USER_LIST);
        } catch (PermissionDeniedException e) {
            System.out.println("✗ 权限不足: " + e.getMessage());
            return;
        }
        
        try {
            List<User> users = userService.listUsers();
            System.out.println("\n用户列表:");
            System.out.println("------------------------------------------------");
            System.out.printf("%-5s %-20s %-10s %-20s%n", "ID", "用户名", "状态", "创建时间");
            System.out.println("------------------------------------------------");
            for (User user : users) {
                System.out.printf("%-5d %-20s %-10s %-20s%n",
                        user.getId(),
                        user.getUsername(),
                        user.isFrozen() ? "冻结" : "正常",
                        user.getCreatedAt());
            }
            System.out.println("------------------------------------------------");
        } catch (BusinessException e) {
            System.out.println("✗ 查询用户列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 角色管理菜单
     */
    public void handleRoleMenu() {
        while (true) {
            System.out.println("\n------ 角色管理 ------");
            System.out.println("1. 创建角色");
            System.out.println("2. 删除角色");
            System.out.println("3. 查看所有角色");
            System.out.println("4. 为用户分配角色");
            System.out.println("5. 取消用户角色");
            System.out.println("6. 查看用户的角色");
            System.out.println("0. 返回上级菜单");
            System.out.println("----------------------");
            System.out.print("请输入操作编号: ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    handleCreateRole();
                    break;
                case "2":
                    handleDeleteRole();
                    break;
                case "3":
                    handleListRoles();
                    break;
                case "4":
                    handleAssignRole();
                    break;
                case "5":
                    handleRemoveRole();
                    break;
                case "6":
                    handleViewUserRoles();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("✗ 无效的选项，请重新输入");
            }
        }
    }
    
    private void handleCreateRole() {
        // 提前检查权限
        try {
            authService.checkPermission(PermissionConsts.ROLE_CREATE);
        } catch (PermissionDeniedException e) {
            System.out.println("✗ 权限不足: " + e.getMessage());
            return;
        }
        
        // 允许重试
        while (true) {
            String roleCode = cancelableInput("请输入角色编码");
            if (roleCode == null) return;  // 用户取消
            
            String roleName = cancelableInput("请输入角色名称");
            if (roleName == null) return;  // 用户取消
            
            String description = cancelableInput("请输入角色描述");
            if (description == null) return;  // 用户取消
            
            try {
                roleService.createRole(roleCode, roleName, description);
                System.out.println("✓ 创建角色 [" + roleCode + "] 成功");
                break;
            } catch (BusinessException e) {
                System.out.println("✗ 创建角色失败: " + e.getMessage());
                if (!askForRetry()) {
                    break;
                }
            }
        }
    }
    
    private void handleDeleteRole() {
        // 提前检查权限
        try {
            authService.checkPermission(PermissionConsts.ROLE_DELETE);
        } catch (PermissionDeniedException e) {
            System.out.println("✗ 权限不足: " + e.getMessage());
            return;
        }
        
        handleListRoles();
        String input = cancelableInput("请输入要删除的角色ID");
        if (input == null) return;  // 用户取消
        
        try {
            int roleId = Integer.parseInt(input);
            roleService.deleteRole(roleId);
            System.out.println("✓ 删除角色成功");
        } catch (NumberFormatException e) {
            System.out.println("✗ 无效的角色ID");
        } catch (BusinessException e) {
            System.out.println("✗ 删除角色失败: " + e.getMessage());
        }
    }
    
    private void handleListRoles() {
        // 提前检查权限
        try {
            authService.checkPermission(PermissionConsts.ROLE_LIST);
        } catch (PermissionDeniedException e) {
            System.out.println("✗ 权限不足: " + e.getMessage());
            return;
        }
        
        try {
            List<Role> roles = roleService.listRoles();
            System.out.println("\n角色列表:");
            System.out.println("------------------------------------------------------------");
            System.out.printf("%-5s %-15s %-20s %-30s%n", "ID", "角色编码", "角色名称", "描述");
            System.out.println("------------------------------------------------------------");
            for (Role role : roles) {
                System.out.printf("%-5d %-15s %-20s %-30s%n",
                        role.getId(),
                        role.getRoleCode(),
                        role.getRoleName(),
                        role.getDescription());
            }
            System.out.println("------------------------------------------------------------");
        } catch (BusinessException e) {
            System.out.println("✗ 查询角色列表失败: " + e.getMessage());
        }
    }
    
    private void handleAssignRole() {
        // 提前检查权限
        try {
            authService.checkPermission(PermissionConsts.ROLE_ASSIGN);
        } catch (PermissionDeniedException e) {
            System.out.println("✗ 权限不足: " + e.getMessage());
            return;
        }
        
        handleListUsers();
        String userIdStr = cancelableInput("请输入用户ID");
        if (userIdStr == null) return;  // 用户取消
        
        try {
            int userId = Integer.parseInt(userIdStr);
            
            handleListRoles();
            String roleIdStr = cancelableInput("请输入角色ID");
            if (roleIdStr == null) return;  // 用户取消
            
            int roleId = Integer.parseInt(roleIdStr);
            
            roleService.assignRoleToUser(userId, roleId);
            System.out.println("✓ 分配角色成功");
        } catch (NumberFormatException e) {
            System.out.println("✗ 无效的ID");
        } catch (BusinessException e) {
            System.out.println("✗ 分配角色失败: " + e.getMessage());
        }
    }
    
    private void handleRemoveRole() {
        // 提前检查权限
        try {
            authService.checkPermission(PermissionConsts.ROLE_REVOKE);
        } catch (PermissionDeniedException e) {
            System.out.println("✗ 权限不足: " + e.getMessage());
            return;
        }
        
        handleListUsers();
        String userIdStr = cancelableInput("请输入用户ID");
        if (userIdStr == null) return;  // 用户取消
        
        try {
            int userId = Integer.parseInt(userIdStr);
            
            List<Role> userRoles = roleService.getUserRoles(userId);
            System.out.println("\n用户角色:");
            for (Role role : userRoles) {
                System.out.printf("ID: %d, 编码: %s, 名称: %s%n",
                        role.getId(), role.getRoleCode(), role.getRoleName());
            }
            
            String roleIdStr = cancelableInput("请输入要移除的角色ID");
            if (roleIdStr == null) return;  // 用户取消
            
            int roleId = Integer.parseInt(roleIdStr);
            
            roleService.removeRoleFromUser(userId, roleId);
            System.out.println("✓ 移除角色成功");
        } catch (NumberFormatException e) {
            System.out.println("✗ 无效的ID");
        } catch (BusinessException e) {
            System.out.println("✗ 移除角色失败: " + e.getMessage());
        }
    }
    
    private void handleViewUserRoles() {
        // 提前检查权限
        try {
            authService.checkPermission(PermissionConsts.ROLE_LIST);
        } catch (PermissionDeniedException e) {
            System.out.println("✗ 权限不足: " + e.getMessage());
            return;
        }
        
        handleListUsers();
        String input = cancelableInput("请输入用户ID");
        if (input == null) return;  // 用户取消
        
        try {
            int userId = Integer.parseInt(input);
            List<Role> roles = roleService.getUserRoles(userId);
            
            System.out.println("\n用户角色列表:");
            System.out.println("------------------------------------------------------------");
            System.out.printf("%-5s %-15s %-20s %-30s%n", "ID", "角色编码", "角色名称", "描述");
            System.out.println("------------------------------------------------------------");
            for (Role role : roles) {
                System.out.printf("%-5d %-15s %-20s %-30s%n",
                        role.getId(),
                        role.getRoleCode(),
                        role.getRoleName(),
                        role.getDescription());
            }
            System.out.println("------------------------------------------------------------");
        } catch (NumberFormatException e) {
            System.out.println("✗ 无效的用户ID");
        } catch (BusinessException e) {
            System.out.println("✗ 查询用户角色失败: " + e.getMessage());
        }
    }
    
    /**
     * 权限管理菜单
     */
    public void handlePermissionMenu() {
        while (true) {
            System.out.println("\n------ 权限管理 ------");
            System.out.println("1. 创建权限");
            System.out.println("2. 删除权限");
            System.out.println("3. 查看所有权限");
            System.out.println("4. 为角色分配权限");
            System.out.println("5. 取消角色权限");
            System.out.println("6. 查看角色的权限");
            System.out.println("7. 查看我的权限");
            System.out.println("0. 返回上级菜单");
            System.out.println("----------------------");
            System.out.print("请输入操作编号: ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    handleCreatePermission();
                    break;
                case "2":
                    handleDeletePermission();
                    break;
                case "3":
                    handleListPermissions();
                    break;
                case "4":
                    handleAssignPermission();
                    break;
                case "5":
                    handleRemovePermission();
                    break;
                case "6":
                    handleViewRolePermissions();
                    break;
                case "7":
                    handleViewMyPermissions();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("✗ 无效的选项，请重新输入");
            }
        }
    }
    
    private void handleCreatePermission() {
        // 提前检查权限
        try {
            authService.checkPermission(PermissionConsts.PERM_CREATE);
        } catch (PermissionDeniedException e) {
            System.out.println("✗ 权限不足: " + e.getMessage());
            return;
        }
        
        // 允许重试
        while (true) {
            String permissionCode = cancelableInput("请输入权限编码");
            if (permissionCode == null) return;  // 用户取消
            
            String description = cancelableInput("请输入权限描述");
            if (description == null) return;  // 用户取消
            
            try {
                permissionService.createPermission(permissionCode, description);
                System.out.println("✓ 创建权限 [" + permissionCode + "] 成功");
                break;
            } catch (BusinessException e) {
                System.out.println("✗ 创建权限失败: " + e.getMessage());
                if (!askForRetry()) {
                    break;
                }
            }
        }
    }
    
    private void handleDeletePermission() {
        // 提前检查权限
        try {
            authService.checkPermission(PermissionConsts.PERM_DELETE);
        } catch (PermissionDeniedException e) {
            System.out.println("✗ 权限不足: " + e.getMessage());
            return;
        }
        
        handleListPermissions();
        String input = cancelableInput("请输入要删除的权限ID");
        if (input == null) return;  // 用户取消
        
        try {
            int permissionId = Integer.parseInt(input);
            permissionService.deletePermission(permissionId);
            System.out.println("✓ 删除权限成功");
        } catch (NumberFormatException e) {
            System.out.println("✗ 无效的权限ID");
        } catch (BusinessException e) {
            System.out.println("✗ 删除权限失败: " + e.getMessage());
        }
    }
    
    private void handleListPermissions() {
        // 提前检查权限
        try {
            authService.checkPermission(PermissionConsts.PERM_LIST);
        } catch (PermissionDeniedException e) {
            System.out.println("✗ 权限不足: " + e.getMessage());
            return;
        }
        
        try {
            List<Permission> permissions = permissionService.listPermissions();
            System.out.println("\n权限列表:");
            System.out.println("------------------------------------------------------------");
            System.out.printf("%-5s %-30s %-40s%n", "ID", "权限编码", "描述");
            System.out.println("------------------------------------------------------------");
            for (Permission permission : permissions) {
                System.out.printf("%-5d %-30s %-40s%n",
                        permission.getId(),
                        permission.getPermissionCode(),
                        permission.getDescription());
            }
            System.out.println("------------------------------------------------------------");
        } catch (BusinessException e) {
            System.out.println("✗ 查询权限列表失败: " + e.getMessage());
        }
    }
    
    private void handleAssignPermission() {
        // 提前检查权限
        try {
            authService.checkPermission(PermissionConsts.PERM_ASSIGN);
        } catch (PermissionDeniedException e) {
            System.out.println("✗ 权限不足: " + e.getMessage());
            return;
        }
        
        handleListRoles();
        String roleIdStr = cancelableInput("请输入角色ID");
        if (roleIdStr == null) return;  // 用户取消
        
        try {
            int roleId = Integer.parseInt(roleIdStr);
            
            handleListPermissions();
            String permIdStr = cancelableInput("请输入权限ID");
            if (permIdStr == null) return;  // 用户取消
            
            int permissionId = Integer.parseInt(permIdStr);
            
            permissionService.assignPermissionToRole(roleId, permissionId);
            System.out.println("✓ 分配权限成功");
        } catch (NumberFormatException e) {
            System.out.println("✗ 无效的ID");
        } catch (BusinessException e) {
            System.out.println("✗ 分配权限失败: " + e.getMessage());
        }
    }
    
    private void handleRemovePermission() {
        // 提前检查权限
        try {
            authService.checkPermission(PermissionConsts.PERM_REVOKE);
        } catch (PermissionDeniedException e) {
            System.out.println("✗ 权限不足: " + e.getMessage());
            return;
        }
        
        handleListRoles();
        String roleIdStr = cancelableInput("请输入角色ID");
        if (roleIdStr == null) return;  // 用户取消
        
        try {
            int roleId = Integer.parseInt(roleIdStr);
            
            List<Permission> rolePermissions = permissionService.getRolePermissions(roleId);
            System.out.println("\n角色权限:");
            for (Permission permission : rolePermissions) {
                System.out.printf("ID: %d, 编码: %s, 描述: %s%n",
                        permission.getId(), permission.getPermissionCode(), permission.getDescription());
            }
            
            String permIdStr = cancelableInput("请输入要移除的权限ID");
            if (permIdStr == null) return;  // 用户取消
            
            int permissionId = Integer.parseInt(permIdStr);
            
            permissionService.removePermissionFromRole(roleId, permissionId);
            System.out.println("✓ 移除权限成功");
        } catch (NumberFormatException e) {
            System.out.println("✗ 无效的ID");
        } catch (BusinessException e) {
            System.out.println("✗ 移除权限失败: " + e.getMessage());
        }
    }
    
    private void handleViewRolePermissions() {
        // 提前检查权限
        try {
            authService.checkPermission(PermissionConsts.PERM_LIST);
        } catch (PermissionDeniedException e) {
            System.out.println("✗ 权限不足: " + e.getMessage());
            return;
        }
        
        handleListRoles();
        String input = cancelableInput("请输入角色ID");
        if (input == null) return;  // 用户取消
        
        try {
            int roleId = Integer.parseInt(input);
            List<Permission> permissions = permissionService.getRolePermissions(roleId);
            
            System.out.println("\n角色权限列表:");
            System.out.println("------------------------------------------------------------");
            System.out.printf("%-5s %-30s %-40s%n", "ID", "权限编码", "描述");
            System.out.println("------------------------------------------------------------");
            for (Permission permission : permissions) {
                System.out.printf("%-5d %-30s %-40s%n",
                        permission.getId(),
                        permission.getPermissionCode(),
                        permission.getDescription());
            }
            System.out.println("------------------------------------------------------------");
        } catch (NumberFormatException e) {
            System.out.println("✗ 无效的角色ID");
        } catch (BusinessException e) {
            System.out.println("✗ 查询角色权限失败: " + e.getMessage());
        }
    }
    
    private void handleViewMyPermissions() {
        try {
            User currentUser = authService.getCurrentUser();
            List<Permission> permissions = authService.getUserPermissionDetails(currentUser.getId());
            
            System.out.println("\n我的权限列表:");
            System.out.println("------------------------------------------------------------");
            if (permissions.isEmpty()) {
                System.out.println("暂无权限");
            } else {
                System.out.printf("%-30s %-40s%n", "权限编码", "描述");
                System.out.println("------------------------------------------------------------");
                for (Permission permission : permissions) {
                    System.out.printf("%-30s %-40s%n",
                            permission.getPermissionCode(),
                           permission.getDescription());
                }
            }
            System.out.println("------------------------------------------------------------");
        } catch (AuthenticationException e) {
            System.out.println("✗ 未登录");
        } catch (BusinessException e) {
            System.out.println("✗ 查询权限失败: " + e.getMessage());
        }
    }
    
    /**
     * 智能审计分析
     */
    public void handleAuditAnalysis() {
        try {
            authService.checkPermission("AUDIT:ANALYZE");
            System.out.println("\n========== 智能审计分析 ==========");
            List<String> warnings = auditAnalyzer.analyze();
            
            if (warnings.isEmpty()) {
                System.out.println("未发现异常行为");
            } else {
                System.out.println("发现以下可疑行为:");
                System.out.println("----------------------------------");
                for (String warning : warnings) {
                    System.out.println("⚠ " + warning);
                }
                System.out.println("----------------------------------");
            }
        } catch (PermissionDeniedException e) {
            System.out.println("✗ 权限不足: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("✗ 审计分析失败: " + e.getMessage());
        }
    }
}
