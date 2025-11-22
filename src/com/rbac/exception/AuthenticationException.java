package com.rbac.exception;

/**
 * 认证异常 - 用户登录认证失败时抛出
 * 
 * <p>本异常继承自{@link BusinessException}，专门用于标识认证相关的错误。
 * 
 * <p><b>抛出场景：</b>
 * <ul>
 *   <li>用户名或密码为空</li>
 *   <li>用户不存在</li>
 *   <li>密码错误</li>
 *   <li>账户被冻结</li>
 *   <li>会话已过期或未登录</li>
 * </ul>
 * 
 * <p><b>安全考虑：</b>
 * <ul>
 *   <li>错误消息不应区分"用户不存在"和"密码错误"，统一返回"用户名或密码错误"</li>
 *   <li>避免泄露系统是否存在某个用户名</li>
 *   <li>登录失败应记录到审计日志</li>
 * </ul>
 * 
 * @author RBAC Team
 * @see com.rbac.service.impl.AuthServiceImpl#login(String, String)
 */
public class AuthenticationException extends BusinessException {
    
    /**
     * 构造认证异常
     * 
     * @param message 错误消息（应使用中文，不泄露敏感信息）
     */
    public AuthenticationException(String message) {
        super(message);
    }
}
