package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateReviewRecordRequest;
import com.hjc.backend.dto.ReviewRecordPageRequest;
import com.hjc.backend.service.ReviewRecordService;
import com.hjc.backend.vo.ReviewRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Review Record Management", description = "Review record query and create")
@RestController
@RequestMapping("/api/review-records")
@RequiredArgsConstructor
public class ReviewRecordController {

    private final ReviewRecordService reviewRecordService;

    @Operation(summary = "Page review records")
    @GetMapping("/page")
    public ApiResponse<PageResult<ReviewRecordVO>> page(@Valid @ModelAttribute ReviewRecordPageRequest request) {
        return ApiResponse.success(reviewRecordService.pageReviewRecords(request));
    }

    @Operation(summary = "Get review record detail")
    @GetMapping("/{id}")
    public ApiResponse<ReviewRecordVO> detail(@PathVariable Long id) {
        return ApiResponse.success(reviewRecordService.getReviewRecordDetail(id));
    }

    @Operation(summary = "Create review record")
    @PostMapping
    public ApiResponse<ReviewRecordVO> create(@Valid @RequestBody CreateReviewRecordRequest request) {
        return ApiResponse.success(reviewRecordService.createReviewRecord(request));
    }

    @Operation(summary = "List review history by material id")
    @GetMapping("/material/{materialId}")
    public ApiResponse<List<ReviewRecordVO>> listByMaterialId(@PathVariable Long materialId) {
        return ApiResponse.success(reviewRecordService.listByMaterialId(materialId));
    }
}
