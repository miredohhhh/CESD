package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.dto.AssignRolePermissionsRequest;
import com.hjc.backend.security.PermissionCode;
import com.hjc.backend.service.SysPermissionService;
import com.hjc.backend.service.SysRolePermissionService;
import com.hjc.backend.vo.RolePermissionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Role permission management")
@RestController
@RequestMapping("/api/roles/{roleId}/permissions")
@RequiredArgsConstructor
public class SysRolePermissionController {

    private final SysRolePermissionService sysRolePermissionService;

    private final SysPermissionService sysPermissionService;

    @Operation(summary = "Get role permissions")
    @GetMapping
    public ApiResponse<RolePermissionVO> getRolePermissions(@PathVariable Long roleId) {
        sysPermissionService.requirePermission(PermissionCode.ADMIN_ROLE_PERMISSION_ASSIGN);
        return ApiResponse.success(sysRolePermissionService.getRolePermissions(roleId));
    }

    @Operation(summary = "Assign role permissions")
    @PutMapping
    public ApiResponse<RolePermissionVO> assignRolePermissions(@PathVariable Long roleId,
                                                               @Valid @RequestBody AssignRolePermissionsRequest request) {
        sysPermissionService.requirePermission(PermissionCode.ADMIN_ROLE_PERMISSION_ASSIGN);
        return ApiResponse.success(sysRolePermissionService.assignRolePermissions(roleId, request));
    }
}
