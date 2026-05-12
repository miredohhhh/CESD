package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateSystemConfigRequest;
import com.hjc.backend.dto.UpdateSystemConfigRequest;
import com.hjc.backend.service.SystemConfigService;
import com.hjc.backend.vo.SystemConfigVO;
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

@Tag(name = "System Config Management", description = "System config basic data CRUD")
@RestController
@RequestMapping("/api/system-configs")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @Operation(summary = "Page system configs")
    @GetMapping("/page")
    public ApiResponse<PageResult<SystemConfigVO>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(systemConfigService.pageQuery(pageNum, pageSize, keyword, status));
    }

    @Operation(summary = "Get system config detail")
    @GetMapping("/{id}")
    public ApiResponse<SystemConfigVO> detail(@PathVariable Long id) {
        return ApiResponse.success(systemConfigService.getDetail(id));
    }

    @Operation(summary = "Create system config")
    @PostMapping
    public ApiResponse<SystemConfigVO> create(@Valid @RequestBody CreateSystemConfigRequest request) {
        return ApiResponse.success(systemConfigService.create(request));
    }

    @Operation(summary = "Update system config")
    @PutMapping("/{id}")
    public ApiResponse<SystemConfigVO> update(@PathVariable Long id, @Valid @RequestBody UpdateSystemConfigRequest request) {
        return ApiResponse.success(systemConfigService.update(id, request));
    }

    @Operation(summary = "Delete system config")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        systemConfigService.deleteById(id);
        return ApiResponse.success();
    }
}
