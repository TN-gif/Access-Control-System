USE rbac_system;

-- 初始化权限数据
INSERT INTO permissions (permission_code, description) VALUES
('USER:CREATE', '创建用户'),
('USER:DELETE', '删除用户'),
('USER:UPDATE', '更新用户'),
('USER:LIST', '查看用户列表'),
('USER:FREEZE', '冻结用户'),
('USER:UNFREEZE', '解冻用户'),
('ROLE:CREATE', '创建角色'),
('ROLE:DELETE', '删除角色'),
('ROLE:UPDATE', '更新角色'),
('ROLE:LIST', '查看角色列表'),
('ROLE:ASSIGN', '分配角色'),
('ROLE:REVOKE', '移除角色'),
('PERMISSION:CREATE', '创建权限'),
('PERMISSION:DELETE', '删除权限'),
('PERMISSION:LIST', '查看权限列表'),
('PERMISSION:ASSIGN', '分配权限'),
('PERMISSION:REVOKE', '移除权限'),
('AUDIT:VIEW', '查看审计日志'),
('AUDIT:ANALYZE', '执行审计分析');

-- 初始化角色数据
INSERT INTO roles (role_code, role_name, description) VALUES
('ADMIN', '系统管理员', '拥有所有权限'),
('USER_MANAGER', '用户管理员', '负责用户管理'),
('AUDITOR', '审计员', '负责审计和日志查看'),
('GUEST', '访客', '只有查看权限');

-- 为ADMIN角色分配所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.role_code = 'ADMIN';

-- 为USER_MANAGER角色分配用户管理权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.role_code = 'USER_MANAGER'
AND p.permission_code IN ('USER:CREATE', 'USER:DELETE', 'USER:UPDATE', 'USER:LIST', 'USER:FREEZE', 'USER:UNFREEZE', 'ROLE:LIST', 'ROLE:ASSIGN');

-- 为AUDITOR角色分配审计权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.role_code = 'AUDITOR'
AND p.permission_code IN ('AUDIT:VIEW', 'AUDIT:ANALYZE', 'USER:LIST', 'ROLE:LIST', 'PERMISSION:LIST');

-- 为GUEST角色分配查看权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.role_code = 'GUEST'
AND p.permission_code IN ('USER:LIST', 'ROLE:LIST', 'PERMISSION:LIST');

-- =====================================================
-- 创建默认管理员账户
-- =====================================================
-- 用户名: admin
-- 密码: admin123
-- 
-- ⚠️ 重要提示：
-- 下面的盐值和哈希值是测试用的，无法直接使用！
-- 执行此脚本后，必须运行以下工具之一来设置正确的密码：
-- 
-- 方法1（推荐）：运行 FixAdminPassword.java
--   这会自动生成正确的密码哈希并更新数据库
-- 
-- 方法2：运行 GeneratePasswordHash.java
--   这会生成 SQL 语句，手动复制到 MySQL Shell 中执行
-- 
-- 方法3：手动在 Java 程序中创建管理员
--   使用 UserService.createUser() 创建用户
-- =====================================================

-- 临时插入（使用测试数据，之后需要用工具修复）
INSERT INTO users (username, password_hash, salt, status) VALUES
('admin', 'K7gNU3sdo+OL0wNhqoVWhr3g6s1xYv72ol/pe/Unols=', 'test_salt_value', 0);

-- 为默认管理员分配ADMIN角色
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.role_code = 'ADMIN';
