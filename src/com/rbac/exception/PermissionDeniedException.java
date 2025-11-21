package com.rbac.exception;

/**
 * 权限不足异常
 */
public class PermissionDeniedException extends BusinessException {
    
    public PermissionDeniedException(String message) {
        super(message);
    }
}
