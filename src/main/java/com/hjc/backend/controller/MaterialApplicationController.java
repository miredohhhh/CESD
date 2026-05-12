package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.ApproveMaterialApplicationRequest;
import com.hjc.backend.dto.CreateMaterialApplicationRequest;
import com.hjc.backend.dto.RejectMaterialApplicationRequest;
import com.hjc.backend.dto.UpdateMaterialApplicationRequest;
import com.hjc.backend.dto.WithdrawMaterialApplicationRequest;
import com.hjc.backend.service.MaterialApplicationService;
import com.hjc.backend.vo.MaterialApplicationVO;
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

@Tag(name = "Material Application Management", description = "Material application metadata CRUD")
@RestController
@RequestMapping("/api/material-applications")
@RequiredArgsConstructor
public class MaterialApplicationController {

    private final MaterialApplicationService materialApplicationService;

    @Operation(summary = "Page material applications")
    @GetMapping("/page")
    public ApiResponse<PageResult<MaterialApplicationVO>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long itemId) {
        return ApiResponse.success(materialApplicationService.pageQuery(pageNum, pageSize, keyword, status, studentId, itemId));
    }

    @Operation(summary = "Get material application detail")
    @GetMapping("/{id}")
    public ApiResponse<MaterialApplicationVO> detail(@PathVariable Long id) {
        return ApiResponse.success(materialApplicationService.getDetail(id));
    }

    @Operation(summary = "Create material application")
    @PostMapping
    public ApiResponse<MaterialApplicationVO> create(@Valid @RequestBody CreateMaterialApplicationRequest request) {
        return ApiResponse.success(materialApplicationService.create(request));
    }

    @Operation(summary = "Update material application")
    @PutMapping("/{id}")
    public ApiResponse<MaterialApplicationVO> update(@PathVariable Long id, @Valid @RequestBody UpdateMaterialApplicationRequest request) {
        return ApiResponse.success(materialApplicationService.update(id, request));
    }

    @Operation(summary = "Delete material application")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        materialApplicationService.deleteById(id);
        return ApiResponse.success();
    }

    @Operation(summary = "Submit material application")
    @PostMapping("/{id}/submit")
    public ApiResponse<MaterialApplicationVO> submit(@PathVariable Long id) {
        return ApiResponse.success(materialApplicationService.submitMaterialApplication(id));
    }

    @Operation(summary = "Withdraw material application")
    @PostMapping("/{id}/withdraw")
    public ApiResponse<MaterialApplicationVO> withdraw(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) WithdrawMaterialApplicationRequest request) {
        return ApiResponse.success(materialApplicationService.withdrawMaterialApplication(id, request));
    }

    @Operation(summary = "Approve material application")
    @PostMapping("/{id}/approve")
    public ApiResponse<MaterialApplicationVO> approve(@PathVariable Long id, @Valid @RequestBody ApproveMaterialApplicationRequest request) {
        return ApiResponse.success(materialApplicationService.approveMaterialApplication(id, request));
    }

    @Operation(summary = "Reject material application")
    @PostMapping("/{id}/reject")
    public ApiResponse<MaterialApplicationVO> reject(@PathVariable Long id, @Valid @RequestBody RejectMaterialApplicationRequest request) {
        return ApiResponse.success(materialApplicationService.rejectMaterialApplication(id, request));
    }
}
