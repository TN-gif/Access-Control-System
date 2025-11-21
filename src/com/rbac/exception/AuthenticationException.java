package com.rbac.exception;

/**
 * 认证异常
 */
public class AuthenticationException extends BusinessException {
    
    public AuthenticationException(String message) {
        super(message);
    }
}
