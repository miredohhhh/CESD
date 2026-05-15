package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateMaterialAttachmentRequest;
import com.hjc.backend.dto.UpdateMaterialAttachmentRequest;
import com.hjc.backend.service.MaterialAttachmentService;
import com.hjc.backend.vo.MaterialAttachmentVO;
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

@Tag(name = "Material Attachment Management", description = "Material attachment metadata CRUD")
@RestController
@RequestMapping("/api/material-attachments")
@RequiredArgsConstructor
public class MaterialAttachmentController {

    private final MaterialAttachmentService materialAttachmentService;

    @Operation(summary = "Page material attachments")
    @GetMapping("/page")
    public ApiResponse<PageResult<MaterialAttachmentVO>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) Long materialId,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(materialAttachmentService.pageQuery(pageNum, pageSize, materialId, fileType, keyword));
    }

    @Operation(summary = "Get material attachment detail")
    @GetMapping("/{id}")
    public ApiResponse<MaterialAttachmentVO> detail(@PathVariable Long id) {
        return ApiResponse.success(materialAttachmentService.getDetail(id));
    }

    @Operation(summary = "Create material attachment metadata")
    @PostMapping
    public ApiResponse<MaterialAttachmentVO> create(@Valid @RequestBody CreateMaterialAttachmentRequest request) {
        return ApiResponse.success(materialAttachmentService.create(request));
    }

    @Operation(summary = "Update material attachment metadata")
    @PutMapping("/{id}")
    public ApiResponse<MaterialAttachmentVO> update(@PathVariable Long id, @Valid @RequestBody UpdateMaterialAttachmentRequest request) {
        return ApiResponse.success(materialAttachmentService.update(id, request));
    }

    @Operation(summary = "Delete material attachment")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        materialAttachmentService.deleteById(id);
        return ApiResponse.success();
    }
}
