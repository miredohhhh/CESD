package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateSysRoleRequest;
import com.hjc.backend.dto.UpdateSysRoleRequest;
import com.hjc.backend.service.SysRoleService;
import com.hjc.backend.vo.SysRoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Role Management", description = "Role basic data CRUD")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;

    @Operation(summary = "Page roles")
    @GetMapping("/page")
    public ApiResponse<PageResult<SysRoleVO>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(sysRoleService.pageQuery(pageNum, pageSize, keyword, status));
    }

    @Operation(summary = "Get role detail")
    @GetMapping("/{id}")
    public ApiResponse<SysRoleVO> detail(@PathVariable Long id) {
        return ApiResponse.success(sysRoleService.getDetail(id));
    }

    @Operation(summary = "Create role")
    @PostMapping
    public ApiResponse<SysRoleVO> create(@Valid @RequestBody CreateSysRoleRequest request) {
        return ApiResponse.success(sysRoleService.create(request));
    }

    @Operation(summary = "Update role")
    @PutMapping("/{id}")
    public ApiResponse<SysRoleVO> update(@PathVariable Long id, @Valid @RequestBody UpdateSysRoleRequest request) {
        return ApiResponse.success(sysRoleService.update(id, request));
    }

    @Operation(summary = "Delete role")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sysRoleService.deleteById(id);
        return ApiResponse.success();
    }
}
