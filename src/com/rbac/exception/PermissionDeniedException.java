package com.rbac.exception;

/**
 * 权限拒绝异常 - 用户权限不足时抛出
 * 
 * <p>本异常继承自{@link BusinessException}，专门用于标识权限验证失败的错误。
 * 
 * <p><b>抛出场景：</b>
 * <ul>
 *   <li>用户未登录就尝试执行需要权限的操作</li>
 *   <li>用户已登录但缺少必要的权限</li>
 *   <li>权限检查失败（通过{@link com.rbac.service.AuthService#checkPermission(String)}）</li>
 * </ul>
 * 
 * <p><b>处理建议：</b>
 * <ul>
 *   <li>在CLI层捕获后，向用户展示缺少的权限编码</li>
 *   <li>记录到审计日志，便于安全分析和权限调整</li>
 *   <li>错误消息应明确指出需要哪个权限</li>
 * </ul>
 * 
 * <p><b>示例错误消息：</b>
 * <pre>
 * 权限不足：需要权限 [USER:CREATE]
 * 未登录，无法执行此操作
 * </pre>
 * 
 * @author RBAC Team
 * @see com.rbac.service.impl.AuthServiceImpl#checkPermission(String)
 */
public class PermissionDeniedException extends BusinessException {
    
    /**
     * 构造权限拒绝异常
     * 
     * @param message 错误消息（应明确指出缺少的权限）
     */
    public PermissionDeniedException(String message) {
        super(message);
    }
}
