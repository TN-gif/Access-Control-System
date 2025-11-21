package com.rbac.test;

import com.rbac.audit.AuditAnalyzer;
import com.rbac.common.PermissionConsts;
import com.rbac.dao.UserDao;
import com.rbac.decorator.AuthPermissionServiceDecorator;
import com.rbac.decorator.AuthRoleServiceDecorator;
import com.rbac.decorator.AuthUserServiceDecorator;
import com.rbac.exception.BusinessException;
import com.rbac.model.Permission;
import com.rbac.model.Role;
import com.rbac.model.User;
import com.rbac.service.AuthService;
import com.rbac.service.PermissionService;
import com.rbac.service.RoleService;
import com.rbac.service.UserService;
import com.rbac.service.impl.AuthServiceImpl;
import com.rbac.service.impl.PermissionServiceImpl;
import com.rbac.service.impl.RoleServiceImpl;
import com.rbac.service.impl.UserServiceImpl;
import com.rbac.util.ConfigUtil;
import com.rbac.util.DBUtil;
import com.rbac.util.SessionContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 自动化测试运行器
 * 用于快速验证系统核心功能，包括密码复杂度、权限控制、审计分析等
 */
public class AutomatedTestRunner {

    private static UserService userService;
    private static RoleService roleService;
    private static PermissionService permissionService;
    private static AuthService authService;
    private static UserDao userDao;

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("   RBAC 系统自动化测试脚本");
        System.out.println("=================================");

        try {
            // 1. 初始化服务
            initServices();

            // 2. 准备测试数据
            prepareTestData();

            // 3. 执行测试用例
            testLogin();
            testUserLifecycle();
            testPasswordComplexity(); // 新增：密码复杂度测试
            testRolePermissionLifecycle();
            testAuditAnalysis();      // 优化：审计分析测试

            System.out.println("\n=================================");
            System.out.println("   所有测试用例执行完毕！");
            System.out.println("=================================");

        } catch (Exception e) {
            System.err.println("测试过程中发生未捕获异常:");
            e.printStackTrace();
        }
    }

    private static void initServices() {
        System.out.println("\n[INIT] 初始化服务...");
        
        // 基础服务
        UserService baseUserService = new UserServiceImpl();
        RoleService baseRoleService = new RoleServiceImpl();
        PermissionService basePermissionService = new PermissionServiceImpl();
        authService = new AuthServiceImpl();
        userDao = new UserDao();

        // 装饰器包装
        userService = new AuthUserServiceDecorator(baseUserService, authService);
        roleService = new AuthRoleServiceDecorator(baseRoleService, authService, baseUserService);
        permissionService = new AuthPermissionServiceDecorator(basePermissionService, authService, baseRoleService);
        
        System.out.println("[INIT] 服务初始化完成");
    }

    private static void prepareTestData() {
        System.out.println("\n[SETUP] 准备测试数据...");
        // 清理可能存在的测试数据
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM users WHERE username LIKE 'test%'");
            stmt.executeUpdate("DELETE FROM roles WHERE role_code LIKE 'TEST%'");
            stmt.executeUpdate("DELETE FROM permissions WHERE permission_code LIKE 'TEST%'");
            System.out.println("[SETUP] 清理旧测试数据完成");
        } catch (Exception e) {
            System.err.println("[SETUP] 清理数据失败: " + e.getMessage());
        }
        
        // 确保 admin 存在 (假设已通过 FixAdminPassword 修复)
        // 这里为了测试方便，我们模拟登录 admin
        try {
            User admin = userDao.findByUsername("admin");
            if (admin != null) {
                SessionContext.setCurrentUser(admin);
                System.out.println("[SETUP] 已模拟 admin 登录");
                
                // 确保 ADMIN 角色拥有新的撤销权限 (修复旧数据库数据缺失问题)
                ensureAdminHasRevokePermissions();
            } else {
                throw new RuntimeException("Admin 用户不存在，请先初始化数据库");
            }
        } catch (Exception e) {
            throw new RuntimeException("准备测试数据失败", e);
        }
    }

    private static void ensureAdminHasRevokePermissions() {
        try {
            Role adminRole = roleService.getRoleByCode("ADMIN");
            
            // 检查并创建 ROLE:REVOKE
            Permission roleRevoke = null;
            try {
                roleRevoke = permissionService.getPermissionByCode("ROLE:REVOKE");
            } catch (BusinessException e) {
                permissionService.createPermission("ROLE:REVOKE", "移除角色");
                roleRevoke = permissionService.getPermissionByCode("ROLE:REVOKE");
                System.out.println("[SETUP] 自动补全缺失权限: ROLE:REVOKE");
            }
            
            // 检查并创建 PERMISSION:REVOKE
            Permission permRevoke = null;
            try {
                permRevoke = permissionService.getPermissionByCode("PERMISSION:REVOKE");
            } catch (BusinessException e) {
                permissionService.createPermission("PERMISSION:REVOKE", "移除权限");
                permRevoke = permissionService.getPermissionByCode("PERMISSION:REVOKE");
                System.out.println("[SETUP] 自动补全缺失权限: PERMISSION:REVOKE");
            }

            // 分配给 ADMIN
            try {
                permissionService.assignPermissionToRole(adminRole.getId(), roleRevoke.getId());
            } catch (BusinessException e) { /* 忽略已存在 */ }
            
            try {
                permissionService.assignPermissionToRole(adminRole.getId(), permRevoke.getId());
            } catch (BusinessException e) { /* 忽略已存在 */ }
            
        } catch (Exception e) {
            System.err.println("[SETUP] 警告: 尝试修复 ADMIN 权限失败: " + e.getMessage());
        }
    }

    private static void testLogin() {
        System.out.println("\n[TEST] 1. 登录功能测试");
        
        // 模拟退出
        SessionContext.clear();
        
        // 1.1 正常登录
        try {
            User user = authService.login("admin", "admin123");
            if (user != null) {
                System.out.println("  ✓ 正常登录成功");
            } else {
                System.err.println("  ✗ 正常登录失败");
            }
        } catch (Exception e) {
            System.err.println("  ✗ 正常登录异常: " + e.getMessage());
        }

        // 1.2 密码错误
        try {
            authService.login("admin", "wrongpass");
            System.err.println("  ✗ 密码错误未拦截");
        } catch (BusinessException e) {
            System.out.println("  ✓ 密码错误拦截成功: " + e.getMessage());
        }

        // 恢复 Admin 会话
        User admin = userDao.findByUsername("admin");
        SessionContext.setCurrentUser(admin);
    }

    private static void testUserLifecycle() {
        System.out.println("\n[TEST] 2. 用户生命周期测试");
        String username = "testuser_life";
        String password = "Password123"; // 符合复杂度要求

        // 2.1 创建用户
        try {
            userService.createUser(username, password);
            System.out.println("  ✓ 创建用户成功");
        } catch (Exception e) {
            System.err.println("  ✗ 创建用户失败: " + e.getMessage());
            return;
        }

        // 2.2 查询用户
        User user = userDao.findByUsername(username);
        if (user != null) {
            System.out.println("  ✓ 查询用户成功");
        } else {
            System.err.println("  ✗ 查询用户失败");
        }

        // 2.3 删除用户
        try {
            userService.deleteUser(user.getId());
            System.out.println("  ✓ 删除用户成功");
        } catch (Exception e) {
            System.err.println("  ✗ 删除用户失败: " + e.getMessage());
        }
    }

    private static void testPasswordComplexity() {
        System.out.println("\n[TEST] 3. 密码复杂度测试 (新功能)");
        
        // 3.1 长度不足
        try {
            userService.createUser("test_short", "Pass1");
            System.err.println("  ✗ 长度不足未拦截");
        } catch (BusinessException e) {
            System.out.println("  ✓ 长度不足拦截成功: " + e.getMessage());
        }

        // 3.2 纯数字
        try {
            userService.createUser("test_digit", "12345678");
            System.err.println("  ✗ 纯数字未拦截");
        } catch (BusinessException e) {
            System.out.println("  ✓ 纯数字拦截成功: " + e.getMessage());
        }

        // 3.3 纯字母
        try {
            userService.createUser("test_alpha", "abcdefgh");
            System.err.println("  ✗ 纯字母未拦截");
        } catch (BusinessException e) {
            System.out.println("  ✓ 纯字母拦截成功: " + e.getMessage());
        }

        // 3.4 合法密码
        try {
            userService.createUser("test_valid", "ValidPass123");
            System.out.println("  ✓ 合法密码创建成功");
            // 清理
            User u = userDao.findByUsername("test_valid");
            if (u != null) userService.deleteUser(u.getId());
        } catch (Exception e) {
            System.err.println("  ✗ 合法密码创建失败: " + e.getMessage());
        }
    }

    private static void testRolePermissionLifecycle() {
        System.out.println("\n[TEST] 4. 角色与权限生命周期测试");
        
        String roleCode = "TEST_ROLE";
        String permCode = "TEST:PERM";
        
        // 4.1 创建权限
        try {
            permissionService.createPermission(permCode, "测试权限");
            System.out.println("  ✓ 创建权限成功");
        } catch (Exception e) {
            System.err.println("  ✗ 创建权限失败: " + e.getMessage());
        }

        // 4.2 创建角色
        try {
            roleService.createRole(roleCode, "测试角色", "描述");
            System.out.println("  ✓ 创建角色成功");
        } catch (Exception e) {
            System.err.println("  ✗ 创建角色失败: " + e.getMessage());
        }

        Role role = roleService.getRoleByCode(roleCode);
        Permission perm = permissionService.getPermissionByCode(permCode);

        // 4.3 分配权限给角色
        try {
            permissionService.assignPermissionToRole(role.getId(), perm.getId());
            System.out.println("  ✓ 分配权限给角色成功");
        } catch (Exception e) {
            System.err.println("  ✗ 分配权限失败: " + e.getMessage());
        }

        // 4.4 移除权限 (测试新加的 PERMISSION:REVOKE)
        try {
            permissionService.removePermissionFromRole(role.getId(), perm.getId());
            System.out.println("  ✓ 移除角色权限成功");
        } catch (Exception e) {
            System.err.println("  ✗ 移除权限失败: " + e.getMessage());
        }

        // 清理
        try {
            roleService.deleteRole(role.getId());
            permissionService.deletePermission(perm.getId());
            System.out.println("  ✓ 清理角色和权限成功");
        } catch (Exception e) {
            System.err.println("  ✗ 清理失败: " + e.getMessage());
        }
    }

    private static void testAuditAnalysis() {
        System.out.println("\n[TEST] 5. 智能审计分析测试 (内存优化验证)");
        
        // 5.1 生成模拟日志文件
        String logPathStr = ConfigUtil.getString("audit.log.path", "logs/audit.log");
        Path logPath = Paths.get(logPathStr);
        
        try {
            // 确保目录存在
            Files.createDirectories(logPath.getParent());
            
            // 写入 10 条失败记录，模拟暴力破解
            StringBuilder sb = new StringBuilder();
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            for (int i = 0; i < 10; i++) {
                sb.append(String.format("%s [AUDIT_FAIL] user=hacker action=LOGIN target=system msg=密码错误 result=FAIL%n", now));
            }
            
            Files.write(logPath, sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("  ✓ 模拟审计日志写入成功");
            
        } catch (IOException e) {
            System.err.println("  ✗ 写入日志失败: " + e.getMessage());
            return;
        }

        // 5.2 执行分析
        try {
            AuditAnalyzer analyzer = new AuditAnalyzer();
            List<String> warnings = analyzer.analyze();
            
            if (!warnings.isEmpty()) {
                System.out.println("  ✓ 审计分析成功，发现告警: " + warnings.size() + " 条");
                for (String w : warnings) {
                    System.out.println("    - " + w);
                }
            } else {
                System.err.println("  ✗ 审计分析未发现预期告警");
            }
        } catch (Exception e) {
            System.err.println("  ✗ 审计分析执行异常: " + e.getMessage());
        }
    }
}
