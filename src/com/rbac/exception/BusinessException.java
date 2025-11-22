package com.rbac.exception;

/**
 * 业务异常基类 - RBAC系统的顶层业务异常
 * 
 * <p>本异常类是所有业务逻辑异常的基类，继承自RuntimeException，
 * 因此不需要在方法签名中声明throws。
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>业务规则验证失败（如用户名已存在、密码复杂度不足）</li>
 *   <li>数据完整性约束违反（如删除不存在的记录）</li>
 *   <li>状态转换不合法（如解冻已经是正常状态的用户）</li>
 * </ul>
 * 
 * <p><b>异常处理建议：</b>
 * <ul>
 *   <li>在Service层抛出，携带清晰的中文错误消息</li>
 *   <li>在CLI层捕获，向用户展示友好的错误提示</li>
 *   <li>错误消息应该具体说明失败原因，便于用户理解和纠正</li>
 * </ul>
 * 
 * @author RBAC Team
 * @see AuthenticationException
 * @see PermissionDeniedException
 */
public class BusinessException extends RuntimeException {
    
    /**
     * 构造业务异常
     * 
     * @param message 错误消息（应使用中文，清晰描述问题）
     */
    public BusinessException(String message) {
        super(message);
    }
    
    /**
     * 构造业务异常（带原因）
     * 
     * <p>当业务异常由底层异常（如SQLException）引起时使用
     * 
     * @param message 错误消息（应使用中文，清晰描述问题）
     * @param cause 原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
