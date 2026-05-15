package com.hjc.backend.security;

import com.hjc.backend.service.SysPermissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class PermissionAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final SysPermissionService sysPermissionService;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        Authentication auth = authentication.get();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof LoginUserContext loginUser)) {
            return new AuthorizationDecision(false);
        }
        if (CurrentUserUtils.ROLE_ADMIN.equalsIgnoreCase(loginUser.getRoleCode())) {
            return new AuthorizationDecision(true);
        }
        List<String> requiredCodes = resolvePermissionCodes(context.getRequest());
        if (requiredCodes.isEmpty()) {
            return new AuthorizationDecision(true);
        }
        boolean allowed = requiredCodes.stream()
                .anyMatch(code -> sysPermissionService.hasPermission(loginUser.getRoleId(), loginUser.getRoleCode(), code));
        return new AuthorizationDecision(allowed);
    }

    private List<String> resolvePermissionCodes(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        List<String> codes = new ArrayList<>();

        if (path.equals("/api/frontend/my-applications/page") || path.equals("/api/frontend/my-applications/statistics")) {
            codes.add(PermissionCode.STUDENT_APPLICATION_VIEW);
        } else if (path.equals("/api/frontend/my-score") || path.equals("/api/frontend/my-score/categories")) {
            codes.add(PermissionCode.STUDENT_SCORE_VIEW);
        } else if (path.equals("/api/frontend/audit/pending/page")) {
            codes.add(PermissionCode.AUDIT_PENDING_VIEW);
        } else if (path.equals("/api/material-applications") && "POST".equals(method)) {
            codes.add(PermissionCode.STUDENT_APPLICATION_CREATE);
        } else if (path.matches("/api/material-applications/\\d+/submit")) {
            codes.add(PermissionCode.STUDENT_APPLICATION_SUBMIT);
        } else if (path.matches("/api/material-applications/\\d+/withdraw")) {
            codes.add(PermissionCode.STUDENT_APPLICATION_WITHDRAW);
        } else if (path.matches("/api/material-applications/\\d+/approve")) {
            codes.add(PermissionCode.AUDIT_APPLICATION_APPROVE);
        } else if (path.matches("/api/material-applications/\\d+/reject")) {
            codes.add(PermissionCode.AUDIT_APPLICATION_REJECT);
        } else if (path.startsWith("/api/scores")) {
            codes.add(PermissionCode.ADMIN_SCORE_RECALCULATE);
        } else if (path.startsWith("/api/evaluation-categories")) {
            addCrudCode(codes, method, PermissionCode.ADMIN_EVALUATION_CATEGORY_VIEW,
                    PermissionCode.ADMIN_EVALUATION_CATEGORY_CREATE, PermissionCode.ADMIN_EVALUATION_CATEGORY_UPDATE,
                    PermissionCode.ADMIN_EVALUATION_CATEGORY_DELETE);
            if ("GET".equals(method)) {
                codes.add(PermissionCode.STUDENT_APPLICATION_CREATE);
            }
        } else if (path.startsWith("/api/evaluation-items")) {
            addCrudCode(codes, method, PermissionCode.ADMIN_EVALUATION_ITEM_VIEW,
                    PermissionCode.ADMIN_EVALUATION_ITEM_CREATE, PermissionCode.ADMIN_EVALUATION_ITEM_UPDATE,
                    PermissionCode.ADMIN_EVALUATION_ITEM_DELETE);
            if ("GET".equals(method)) {
                codes.add(PermissionCode.STUDENT_APPLICATION_CREATE);
            }
        } else if (path.startsWith("/api/majors")) {
            addCrudCode(codes, method, PermissionCode.ADMIN_MAJOR_VIEW, PermissionCode.ADMIN_MAJOR_CREATE,
                    PermissionCode.ADMIN_MAJOR_UPDATE, PermissionCode.ADMIN_MAJOR_DELETE);
        } else if (path.startsWith("/api/classes")) {
            addCrudCode(codes, method, PermissionCode.ADMIN_CLASS_VIEW, PermissionCode.ADMIN_CLASS_CREATE,
                    PermissionCode.ADMIN_CLASS_UPDATE, PermissionCode.ADMIN_CLASS_DELETE);
        } else if (path.startsWith("/api/students")) {
            addCrudCode(codes, method, PermissionCode.ADMIN_STUDENT_VIEW, PermissionCode.ADMIN_STUDENT_CREATE,
                    PermissionCode.ADMIN_STUDENT_UPDATE, PermissionCode.ADMIN_STUDENT_DELETE);
        } else if (path.startsWith("/api/users")) {
            addCrudCode(codes, method, PermissionCode.ADMIN_USER_VIEW, PermissionCode.ADMIN_USER_CREATE,
                    PermissionCode.ADMIN_USER_UPDATE, PermissionCode.ADMIN_USER_DELETE);
        } else if (path.matches("/api/roles/\\d+/permissions")) {
            codes.add(PermissionCode.ADMIN_ROLE_PERMISSION_ASSIGN);
        } else if (path.startsWith("/api/roles")) {
            addCrudCode(codes, method, PermissionCode.ADMIN_ROLE_VIEW, PermissionCode.ADMIN_ROLE_CREATE,
                    PermissionCode.ADMIN_ROLE_UPDATE, PermissionCode.ADMIN_ROLE_DELETE);
        } else if (path.startsWith("/api/permissions")) {
            addCrudCode(codes, method, PermissionCode.ADMIN_PERMISSION_VIEW, PermissionCode.ADMIN_PERMISSION_CREATE,
                    PermissionCode.ADMIN_PERMISSION_UPDATE, PermissionCode.ADMIN_PERMISSION_DELETE);
        } else if (path.startsWith("/api/system-configs")) {
            if ("GET".equals(method)) {
                codes.add(PermissionCode.ADMIN_SYSTEM_CONFIG_VIEW);
            } else {
                codes.add(PermissionCode.ADMIN_SYSTEM_CONFIG_UPDATE);
            }
        }

        return codes;
    }

    private void addCrudCode(List<String> codes, String method, String view, String create, String update, String delete) {
        if ("GET".equals(method)) {
            codes.add(view);
        } else if ("POST".equals(method)) {
            codes.add(create);
        } else if ("PUT".equals(method) || "PATCH".equals(method)) {
            codes.add(update);
        } else if ("DELETE".equals(method)) {
            codes.add(delete);
        }
    }
}
