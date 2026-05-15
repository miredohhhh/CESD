package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateSysPermissionRequest;
import com.hjc.backend.dto.SysPermissionPageRequest;
import com.hjc.backend.dto.UpdateSysPermissionRequest;
import com.hjc.backend.security.PermissionCode;
import com.hjc.backend.service.SysPermissionService;
import com.hjc.backend.vo.SysPermissionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Permission management")
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class SysPermissionController {

    private final SysPermissionService sysPermissionService;

    @Operation(summary = "Page permissions")
    @GetMapping("/page")
    public ApiResponse<PageResult<SysPermissionVO>> page(@ModelAttribute SysPermissionPageRequest request) {
        sysPermissionService.requirePermission(PermissionCode.ADMIN_PERMISSION_VIEW);
        return ApiResponse.success(sysPermissionService.pageQuery(request));
    }

    @Operation(summary = "Permission tree")
    @GetMapping("/tree")
    public ApiResponse<List<SysPermissionVO>> tree() {
        sysPermissionService.requirePermission(PermissionCode.ADMIN_PERMISSION_VIEW);
        return ApiResponse.success(sysPermissionService.tree());
    }

    @Operation(summary = "Permission detail")
    @GetMapping("/{id}")
    public ApiResponse<SysPermissionVO> detail(@PathVariable Long id) {
        sysPermissionService.requirePermission(PermissionCode.ADMIN_PERMISSION_VIEW);
        return ApiResponse.success(sysPermissionService.getDetail(id));
    }

    @Operation(summary = "Create permission")
    @PostMapping
    public ApiResponse<SysPermissionVO> create(@Valid @RequestBody CreateSysPermissionRequest request) {
        sysPermissionService.requirePermission(PermissionCode.ADMIN_PERMISSION_CREATE);
        return ApiResponse.success(sysPermissionService.create(request));
    }

    @Operation(summary = "Update permission")
    @PutMapping("/{id}")
    public ApiResponse<SysPermissionVO> update(@PathVariable Long id, @Valid @RequestBody UpdateSysPermissionRequest request) {
        sysPermissionService.requirePermission(PermissionCode.ADMIN_PERMISSION_UPDATE);
        return ApiResponse.success(sysPermissionService.update(id, request));
    }

    @Operation(summary = "Delete permission")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sysPermissionService.requirePermission(PermissionCode.ADMIN_PERMISSION_DELETE);
        sysPermissionService.deleteById(id);
        return ApiResponse.success();
    }
}
