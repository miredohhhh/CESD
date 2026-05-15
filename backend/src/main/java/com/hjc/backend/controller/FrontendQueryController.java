package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.common.PageRequest;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.MyMaterialApplicationPageRequest;
import com.hjc.backend.dto.PendingMaterialApplicationPageRequest;
import com.hjc.backend.service.MaterialApplicationService;
import com.hjc.backend.service.ScoreSummaryService;
import com.hjc.backend.vo.FrontendScoreSummaryVO;
import com.hjc.backend.vo.MaterialApplicationDetailVO;
import com.hjc.backend.vo.MyApplicationStatisticsVO;
import com.hjc.backend.vo.MyMaterialApplicationVO;
import com.hjc.backend.vo.PendingMaterialApplicationVO;
import com.hjc.backend.vo.ScoreCategorySummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Frontend Query", description = "Frontend friendly query APIs")
@RestController
@RequestMapping("/api/frontend")
@RequiredArgsConstructor
public class FrontendQueryController {

    private final MaterialApplicationService materialApplicationService;

    private final ScoreSummaryService scoreSummaryService;

    @Operation(summary = "Page my material applications")
    @GetMapping("/my-applications/page")
    public ApiResponse<PageResult<MyMaterialApplicationVO>> pageMyApplications(@Valid @ModelAttribute MyMaterialApplicationPageRequest request) {
        return ApiResponse.success(materialApplicationService.pageMyApplications(request));
    }

    @Operation(summary = "Get my material application statistics")
    @GetMapping("/my-applications/statistics")
    public ApiResponse<MyApplicationStatisticsVO> getMyApplicationStatistics() {
        return ApiResponse.success(materialApplicationService.getMyApplicationStatistics());
    }

    @Operation(summary = "Get frontend material application detail")
    @GetMapping("/applications/{id}/detail")
    public ApiResponse<MaterialApplicationDetailVO> getApplicationDetail(@PathVariable Long id) {
        return ApiResponse.success(materialApplicationService.getFrontendApplicationDetail(id));
    }

    @Operation(summary = "Page pending material applications")
    @GetMapping("/audit/pending/page")
    public ApiResponse<PageResult<PendingMaterialApplicationVO>> pagePendingApplications(@Valid @ModelAttribute PendingMaterialApplicationPageRequest request) {
        return ApiResponse.success(materialApplicationService.pagePendingApplications(request));
    }

    @Operation(summary = "Get my score")
    @GetMapping("/my-score")
    public ApiResponse<FrontendScoreSummaryVO> getMyScore() {
        return ApiResponse.success(scoreSummaryService.getMyScore());
    }

    @Operation(summary = "Get my category scores")
    @GetMapping("/my-score/categories")
    public ApiResponse<List<ScoreCategorySummaryVO>> getMyCategoryScores() {
        return ApiResponse.success(scoreSummaryService.getMyCategoryScores());
    }

    @Operation(summary = "Page class ranking for frontend")
    @GetMapping("/classes/{classId}/ranking/page")
    public ApiResponse<PageResult<FrontendScoreSummaryVO>> pageClassRanking(
            @PathVariable Long classId,
            @RequestParam(defaultValue = "1") Long pageNo,
            @RequestParam(defaultValue = "10") Long pageSize) {
        PageRequest request = new PageRequest();
        request.setPageNum(pageNo);
        request.setPageSize(pageSize);
        return ApiResponse.success(scoreSummaryService.pageClassRankingForFrontend(classId, request));
    }

    @Operation(summary = "Page major ranking for frontend")
    @GetMapping("/majors/{majorId}/ranking/page")
    public ApiResponse<PageResult<FrontendScoreSummaryVO>> pageMajorRanking(
            @PathVariable Long majorId,
            @RequestParam(defaultValue = "1") Long pageNo,
            @RequestParam(defaultValue = "10") Long pageSize) {
        PageRequest request = new PageRequest();
        request.setPageNum(pageNo);
        request.setPageSize(pageSize);
        return ApiResponse.success(scoreSummaryService.pageMajorRankingForFrontend(majorId, request));
    }
}
