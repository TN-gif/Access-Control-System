package com.rbac;

import com.rbac.audit.AuditAnalyzer;
import com.rbac.cli.MenuHandler;
import com.rbac.decorator.AuthPermissionServiceDecorator;
import com.rbac.decorator.AuthRoleServiceDecorator;
import com.rbac.decorator.AuthUserServiceDecorator;
import com.rbac.model.User;
import com.rbac.service.*;
import com.rbac.service.impl.*;
import com.rbac.util.SessionContext;

import java.util.Scanner;

/**
 * 主程序入口
 */
public class MainApp {
    
    private final Scanner scanner;
    private final AuthService authService;
    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final MenuHandler menuHandler;
    
    public MainApp() {
        this.scanner = new Scanner(System.in);
        
        // 初始化基础服务
        this.authService = new AuthServiceImpl();
        UserService baseUserService = new UserServiceImpl();
        RoleService baseRoleService = new RoleServiceImpl();
        PermissionService basePermissionService = new PermissionServiceImpl();
        
        // 使用装饰器包装服务，增加权限控制
        this.userService = new AuthUserServiceDecorator(baseUserService, authService);
        this.roleService = new AuthRoleServiceDecorator(baseRoleService, authService, baseUserService);
        this.permissionService = new AuthPermissionServiceDecorator(basePermissionService, authService, baseRoleService);
        
        // 初始化审计分析器
        AuditAnalyzer auditAnalyzer = new AuditAnalyzer();
        
        // 初始化菜单处理器
        this.menuHandler = new MenuHandler(scanner, authService, userService, roleService, permissionService, auditAnalyzer);
    }
    
    /**
     * 启动应用
     */
    public void start() {
        System.out.println("========================================");
        System.out.println("    RBAC 权限管理系统 v1.0");
        System.out.println("========================================");
        
        boolean running = true;
        while (running) {
            showMainMenu();
            String choice = scanner.nextLine().trim();
            
            // 根据登录状态处理不同的菜单选项
            if (!SessionContext.isLoggedIn()) {
                // 未登录状态
                switch (choice) {
                    case "1":
                        menuHandler.handleLogin();
                        break;
                    case "0":
                        System.out.println("感谢使用，再见！");
                        running = false;
                        break;
                    default:
                        System.out.println("✗ 无效的选项，请重新输入");
                }
            } else {
                // 已登录状态
                switch (choice) {
                    case "1":
                        menuHandler.handleLogout();
                        break;
                    case "2":
                        menuHandler.handleUserMenu();
                        break;
                    case "3":
                        menuHandler.handleRoleMenu();
                        break;
                    case "4":
                        menuHandler.handlePermissionMenu();
                        break;
                    case "5":
                        menuHandler.handleAuditAnalysis();
                        break;
                    case "0":
                        System.out.println("感谢使用，再见！");
                        running = false;
                        break;
                    default:
                        System.out.println("✗ 无效的选项，请重新输入");
                }
            }
        }
        
        // 清理资源
        SessionContext.clear();
        scanner.close();
    }
    
    /**
     * 显示主菜单
     */
    private void showMainMenu() {
        System.out.println("\n========== RBAC 权限管理系统 ==========");
        
        // 显示当前登录用户
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser != null) {
            System.out.println("当前用户: " + currentUser.getUsername());
        } else {
            System.out.println("当前状态: 未登录");
        }
        
        System.out.println("---------------------------------------");
        
        // 根据登录状态显示不同的菜单
        if (!SessionContext.isLoggedIn()) {
            // 未登录菜单
            System.out.println("1. 登录");
            System.out.println("0. 退出系统");
        } else {
            // 已登录菜单
            System.out.println("1. 登出");
            System.out.println("2. 用户管理");
            System.out.println("3. 角色管理");
            System.out.println("4. 权限管理");
            System.out.println("5. 智能审计分析");
            System.out.println("0. 退出系统");
        }
        
        System.out.println("=======================================");
        System.out.print("请输入操作编号: ");
    }
    
    /**
     * 主函数
     */
    public static void main(String[] args) {
        try {
            MainApp app = new MainApp();
            app.start();
        } catch (Exception e) {
            System.err.println("系统启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
