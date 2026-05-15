package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateMajorInfoRequest;
import com.hjc.backend.dto.UpdateMajorInfoRequest;
import com.hjc.backend.service.MajorInfoService;
import com.hjc.backend.vo.MajorInfoVO;
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

@Tag(name = "Major Management", description = "Major basic data CRUD")
@RestController
@RequestMapping("/api/majors")
@RequiredArgsConstructor
public class MajorInfoController {

    private final MajorInfoService majorInfoService;

    @Operation(summary = "Page majors")
    @GetMapping("/page")
    public ApiResponse<PageResult<MajorInfoVO>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(majorInfoService.pageQuery(pageNum, pageSize, keyword, status));
    }

    @Operation(summary = "Get major detail")
    @GetMapping("/{id}")
    public ApiResponse<MajorInfoVO> detail(@PathVariable Long id) {
        return ApiResponse.success(majorInfoService.getDetail(id));
    }

    @Operation(summary = "Create major")
    @PostMapping
    public ApiResponse<MajorInfoVO> create(@Valid @RequestBody CreateMajorInfoRequest request) {
        return ApiResponse.success(majorInfoService.create(request));
    }

    @Operation(summary = "Update major")
    @PutMapping("/{id}")
    public ApiResponse<MajorInfoVO> update(@PathVariable Long id, @Valid @RequestBody UpdateMajorInfoRequest request) {
        return ApiResponse.success(majorInfoService.update(id, request));
    }

    @Operation(summary = "Delete major")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        majorInfoService.deleteById(id);
        return ApiResponse.success();
    }
}
