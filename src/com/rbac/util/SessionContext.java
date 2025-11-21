package com.rbac.util;

import com.rbac.model.User;

/**
 * 会话上下文 - 管理当前登录用户
 */
public class SessionContext {
    
    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();
    
    /**
     * 设置当前登录用户
     */
    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }
    
    /**
     * 获取当前登录用户
     */
    public static User getCurrentUser() {
        return currentUser.get();
    }
    
    /**
     * 检查是否已登录
     */
    public static boolean isLoggedIn() {
        return currentUser.get() != null;
    }
    
    /**
     * 清除当前会话
     */
    public static void clear() {
        currentUser.remove();
    }
}
