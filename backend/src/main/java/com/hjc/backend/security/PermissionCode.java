package com.hjc.backend.security;

public final class PermissionCode {

    public static final String STUDENT_APPLICATION_VIEW = "student:application:view";
    public static final String STUDENT_APPLICATION_CREATE = "student:application:create";
    public static final String STUDENT_APPLICATION_SUBMIT = "student:application:submit";
    public static final String STUDENT_APPLICATION_WITHDRAW = "student:application:withdraw";
    public static final String STUDENT_SCORE_VIEW = "student:score:view";
    public static final String AUDIT_PENDING_VIEW = "audit:pending:view";
    public static final String AUDIT_APPLICATION_APPROVE = "audit:application:approve";
    public static final String AUDIT_APPLICATION_REJECT = "audit:application:reject";
    public static final String ADMIN_SCORE_RECALCULATE = "admin:score:recalculate";
    public static final String ADMIN_SCORE_EXPORT = "admin:score:export";
    public static final String ADMIN_MATERIAL_EXPORT = "admin:material:export";
    public static final String ADMIN_EVALUATION_CATEGORY_VIEW = "admin:evaluation-category:view";
    public static final String ADMIN_EVALUATION_CATEGORY_CREATE = "admin:evaluation-category:create";
    public static final String ADMIN_EVALUATION_CATEGORY_UPDATE = "admin:evaluation-category:update";
    public static final String ADMIN_EVALUATION_CATEGORY_DELETE = "admin:evaluation-category:delete";
    public static final String ADMIN_EVALUATION_ITEM_VIEW = "admin:evaluation-item:view";
    public static final String ADMIN_EVALUATION_ITEM_CREATE = "admin:evaluation-item:create";
    public static final String ADMIN_EVALUATION_ITEM_UPDATE = "admin:evaluation-item:update";
    public static final String ADMIN_EVALUATION_ITEM_DELETE = "admin:evaluation-item:delete";
    public static final String ADMIN_MAJOR_VIEW = "admin:major:view";
    public static final String ADMIN_MAJOR_CREATE = "admin:major:create";
    public static final String ADMIN_MAJOR_UPDATE = "admin:major:update";
    public static final String ADMIN_MAJOR_DELETE = "admin:major:delete";
    public static final String ADMIN_CLASS_VIEW = "admin:class:view";
    public static final String ADMIN_CLASS_CREATE = "admin:class:create";
    public static final String ADMIN_CLASS_UPDATE = "admin:class:update";
    public static final String ADMIN_CLASS_DELETE = "admin:class:delete";
    public static final String ADMIN_STUDENT_VIEW = "admin:student:view";
    public static final String ADMIN_STUDENT_CREATE = "admin:student:create";
    public static final String ADMIN_STUDENT_UPDATE = "admin:student:update";
    public static final String ADMIN_STUDENT_DELETE = "admin:student:delete";
    public static final String ADMIN_USER_VIEW = "admin:user:view";
    public static final String ADMIN_USER_CREATE = "admin:user:create";
    public static final String ADMIN_USER_UPDATE = "admin:user:update";
    public static final String ADMIN_USER_DELETE = "admin:user:delete";
    public static final String ADMIN_USER_RESET_PASSWORD = "admin:user:reset-password";
    public static final String ADMIN_ROLE_VIEW = "admin:role:view";
    public static final String ADMIN_ROLE_CREATE = "admin:role:create";
    public static final String ADMIN_ROLE_UPDATE = "admin:role:update";
    public static final String ADMIN_ROLE_DELETE = "admin:role:delete";
    public static final String ADMIN_PERMISSION_VIEW = "admin:permission:view";
    public static final String ADMIN_PERMISSION_CREATE = "admin:permission:create";
    public static final String ADMIN_PERMISSION_UPDATE = "admin:permission:update";
    public static final String ADMIN_PERMISSION_DELETE = "admin:permission:delete";
    public static final String ADMIN_ROLE_PERMISSION_ASSIGN = "admin:role-permission:assign";
    public static final String ADMIN_SYSTEM_CONFIG_VIEW = "admin:system-config:view";
    public static final String ADMIN_SYSTEM_CONFIG_CREATE = "admin:system-config:create";
    public static final String ADMIN_SYSTEM_CONFIG_UPDATE = "admin:system-config:update";
    public static final String ADMIN_SYSTEM_CONFIG_DELETE = "admin:system-config:delete";
    public static final String ADMIN_OPERATION_LOG_VIEW = "admin:operation-log:view";
    public static final String ADMIN_LOGIN_LOG_VIEW = "admin:login-log:view";

    private PermissionCode() {
    }
}
