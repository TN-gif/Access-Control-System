package com.rbac.util;

import com.rbac.model.User;

/**
 * 会话上下文 - 管理当前登录用户的会话信息
 * 
 * <p>本类使用<b>ThreadLocal</b>实现线程隔离的会话管理，确保在多线程环境下，
 * 每个线程都有自己独立的用户会话，互不干扰。
 * 
 * <p><b>设计理由：</b>
 * <ul>
 *   <li><b>线程安全</b>：ThreadLocal确保每个线程有独立的用户对象副本</li>
 *   <li><b>简化传参</b>：避免在各个方法间传递用户对象</li>
 *   <li><b>避免全局状态</b>：相比静态变量，ThreadLocal不会产生并发问题</li>
 * </ul>
 * 
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>用户登出时必须调用 {@link #clear()}，避免内存泄漏</li>
 *   <li>在使用线程池的环境下，需要在任务结束时清理ThreadLocal</li>
 * </ul>
 * 
 * @author RBAC Team
 * @see ThreadLocal
 * @see User
 */
public class SessionContext {
    
    /** 线程本地变量，存储每个线程的当前登录用户 */
    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();
    
    /**
     * 设置当前登录用户
     * 
     * <p>在用户成功登录后调用，将用户对象绑定到当前线程
     * 
     * @param user 登录的用户对象
     */
    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }
    
    /**
     * 获取当前登录用户
     * 
     * @return 当前线程绑定的用户对象，如果未登录则返回null
     */
    public static User getCurrentUser() {
        return currentUser.get();
    }
    
    /**
     * 检查当前线程是否已登录
     * 
     * @return 如果已登录返回true，否则返回false
     */
    public static boolean isLoggedIn() {
        return currentUser.get() != null;
    }
    
    /**
     * 清除当前会话
     * 
     * <p>在用户登出时调用，从ThreadLocal中移除用户对象，避免内存泄漏
     */
    public static void clear() {
        currentUser.remove();
    }
}
