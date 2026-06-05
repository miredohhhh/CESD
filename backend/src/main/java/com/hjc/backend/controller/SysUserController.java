package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateSysUserRequest;
import com.hjc.backend.dto.ResetSysUserPasswordRequest;
import com.hjc.backend.dto.UpdateSysUserRequest;
import com.hjc.backend.service.SysUserService;
import com.hjc.backend.vo.SysUserVO;
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

@Tag(name = "用户管理", description = "用户基础 CRUD")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public ApiResponse<PageResult<SysUserVO>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long roleId) {
        return ApiResponse.success(sysUserService.pageQuery(pageNum, pageSize, keyword, status, roleId));
    }

    @Operation(summary = "查询用户详情")
    @GetMapping("/{id}")
    public ApiResponse<SysUserVO> detail(@PathVariable Long id) {
        return ApiResponse.success(sysUserService.getDetail(id));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public ApiResponse<SysUserVO> create(@Valid @RequestBody CreateSysUserRequest request) {
        return ApiResponse.success(sysUserService.create(request));
    }

    @Operation(summary = "修改用户")
    @PutMapping("/{id}")
    public ApiResponse<SysUserVO> update(@PathVariable Long id, @Valid @RequestBody UpdateSysUserRequest request) {
        return ApiResponse.success(sysUserService.update(id, request));
    }

    @Operation(summary = "Reset user password")
    @PutMapping("/{id}/password/reset")
    public ApiResponse<SysUserVO> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetSysUserPasswordRequest request) {
        return ApiResponse.success(sysUserService.resetPassword(id, request));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sysUserService.deleteById(id);
        return ApiResponse.success();
    }
}
