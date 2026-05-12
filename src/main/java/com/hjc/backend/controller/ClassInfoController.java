package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateClassInfoRequest;
import com.hjc.backend.dto.UpdateClassInfoRequest;
import com.hjc.backend.service.ClassInfoService;
import com.hjc.backend.vo.ClassInfoVO;
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

@Tag(name = "Class Management", description = "Class basic data CRUD")
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassInfoController {

    private final ClassInfoService classInfoService;

    @Operation(summary = "Page classes")
    @GetMapping("/page")
    public ApiResponse<PageResult<ClassInfoVO>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(classInfoService.pageQuery(pageNum, pageSize, keyword, status));
    }

    @Operation(summary = "Get class detail")
    @GetMapping("/{id}")
    public ApiResponse<ClassInfoVO> detail(@PathVariable Long id) {
        return ApiResponse.success(classInfoService.getDetail(id));
    }

    @Operation(summary = "Create class")
    @PostMapping
    public ApiResponse<ClassInfoVO> create(@Valid @RequestBody CreateClassInfoRequest request) {
        return ApiResponse.success(classInfoService.create(request));
    }

    @Operation(summary = "Update class")
    @PutMapping("/{id}")
    public ApiResponse<ClassInfoVO> update(@PathVariable Long id, @Valid @RequestBody UpdateClassInfoRequest request) {
        return ApiResponse.success(classInfoService.update(id, request));
    }

    @Operation(summary = "Delete class")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        classInfoService.deleteById(id);
        return ApiResponse.success();
    }
}
