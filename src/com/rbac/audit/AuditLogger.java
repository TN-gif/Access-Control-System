package com.rbac.audit;

import com.rbac.model.User;
import com.rbac.util.SessionContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 审计日志记录器 - 统一记录系统审计事件
 */
public class AuditLogger {
    
    private static final Logger logger = LogManager.getLogger("AUDIT");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    // 日志级别标记
    private static final Marker AUDIT_SUCCESS = MarkerManager.getMarker("AUDIT_SUCCESS");
    private static final Marker AUDIT_FAIL = MarkerManager.getMarker("AUDIT_FAIL");
    private static final Marker AUDIT_CRITICAL = MarkerManager.getMarker("AUDIT_CRITICAL");
    
    /**
     * 记录成功的审计事件
     */
    public static void logSuccess(String action, String target, String message) {
        String operator = getCurrentOperator();
        String logMessage = buildLogMessage(operator, action, target, message, "SUCCESS");
        logger.info(AUDIT_SUCCESS, logMessage);
    }
    
    /**
     * 记录失败的审计事件
     */
    public static void logFail(String action, String target, String message) {
        String operator = getCurrentOperator();
        String logMessage = buildLogMessage(operator, action, target, message, "FAIL");
        logger.info(AUDIT_FAIL, logMessage);
    }
    
    /**
     * 记录关键的审计事件（如权限变更、用户删除等）
     */
    public static void logCritical(String action, String target, String message) {
        String operator = getCurrentOperator();
        String logMessage = buildLogMessage(operator, action, target, message, "CRITICAL");
        logger.info(AUDIT_CRITICAL, logMessage);
    }
    
    /**
     * 记录登录成功
     */
    public static void logLoginSuccess(String username) {
        String logMessage = buildLogMessage(username, "LOGIN", username, "登录成功", "SUCCESS");
        logger.info(AUDIT_SUCCESS, logMessage);
    }
    
    /**
     * 记录登录失败
     */
    public static void logLoginFail(String username, String reason) {
        String logMessage = buildLogMessage(username, "LOGIN", username, "登录失败: " + reason, "FAIL");
        logger.info(AUDIT_FAIL, logMessage);
    }
    
    /**
     * 记录登出
     */
    public static void logLogout(String username) {
        String logMessage = buildLogMessage(username, "LOGOUT", username, "登出系统", "SUCCESS");
        logger.info(AUDIT_SUCCESS, logMessage);
    }
    
    /**
     * 获取当前操作者
     */
    private static String getCurrentOperator() {
        User currentUser = SessionContext.getCurrentUser();
        return currentUser != null ? currentUser.getUsername() : "SYSTEM";
    }
    
    /**
     * 构建日志消息
     * 格式: user=xxx action=xxx target=xxx msg=xxx result=xxx
     */
    private static String buildLogMessage(String operator, String action, String target, String message, String result) {
        return String.format("user=%s action=%s target=%s msg=%s result=%s",
                operator, action, target != null ? target : "", message, result);
    }
}
