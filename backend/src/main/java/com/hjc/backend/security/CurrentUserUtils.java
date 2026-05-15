package com.hjc.backend.security;

import com.hjc.backend.common.ResultCode;
import com.hjc.backend.exception.BusinessException;
import org.springframework.util.StringUtils;

public final class CurrentUserUtils {

    public static final String ROLE_STUDENT = "STUDENT";

    public static final String ROLE_AUDITOR = "AUDITOR";

    public static final String ROLE_REVIEWER = "REVIEWER";

    public static final String ROLE_ADMIN = "ADMIN";

    private CurrentUserUtils() {
    }

    public static LoginUserContext getContext() {
        return LoginUserContextHolder.get();
    }

    public static boolean isLoggedIn() {
        LoginUserContext context = getContext();
        return context != null && context.getUserId() != null;
    }

    public static Long getUserId() {
        LoginUserContext context = getContext();
        return context == null ? null : context.getUserId();
    }

    public static String getUsername() {
        LoginUserContext context = getContext();
        return context == null ? null : context.getUsername();
    }

    public static String getRoleCode() {
        LoginUserContext context = getContext();
        return context == null ? null : context.getRoleCode();
    }

    public static Long getStudentId() {
        LoginUserContext context = getContext();
        return context == null ? null : context.getStudentId();
    }

    public static Long requireUserId() {
        Long userId = getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Unauthorized");
        }
        return userId;
    }

    public static Long requireStudentId() {
        requireUserId();
        Long studentId = getStudentId();
        if (studentId == null) {
            throw new BusinessException("Current account is not bound to student information");
        }
        return studentId;
    }

    public static boolean isAdmin() {
        return hasRole(ROLE_ADMIN);
    }

    public static boolean isStudent() {
        return hasRole(ROLE_STUDENT);
    }

    public static boolean isAuditor() {
        return hasRole(ROLE_AUDITOR) || hasRole(ROLE_REVIEWER);
    }

    public static boolean hasRole(String roleCode) {
        String currentRoleCode = getRoleCode();
        return StringUtils.hasText(currentRoleCode) && currentRoleCode.equalsIgnoreCase(roleCode);
    }

    public static void requireAuditorOrAdmin() {
        requireUserId();
        if (!isAuditor() && !isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Current account has no audit permission");
        }
    }
}
