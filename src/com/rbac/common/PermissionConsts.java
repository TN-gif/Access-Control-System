package com.rbac.common;

/**
 * 权限常量类
 */
public class PermissionConsts {
    
    // 用户管理权限
    public static final String USER_CREATE = "USER:CREATE";
    public static final String USER_DELETE = "USER:DELETE";
    public static final String USER_FREEZE = "USER:FREEZE";
    public static final String USER_UNFREEZE = "USER:UNFREEZE";
    public static final String USER_LIST = "USER:LIST";
    
    // 角色管理权限
    public static final String ROLE_CREATE = "ROLE:CREATE";
    public static final String ROLE_DELETE = "ROLE:DELETE";
    public static final String ROLE_ASSIGN = "ROLE:ASSIGN";
    public static final String ROLE_REVOKE = "ROLE:REVOKE";
    public static final String ROLE_LIST = "ROLE:LIST";
    
    // 权限管理权限
    public static final String PERM_LIST = "PERMISSION:LIST";
    public static final String PERM_CREATE = "PERMISSION:CREATE";
    public static final String PERM_DELETE = "PERMISSION:DELETE";
    public static final String PERM_ASSIGN = "PERMISSION:ASSIGN";
    public static final String PERM_REVOKE = "PERMISSION:REVOKE";
    
    // 私有构造函数，防止实例化
    private PermissionConsts() {}
}
