package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.ScoreSummaryPageRequest;
import com.hjc.backend.service.ScoreSummaryService;
import com.hjc.backend.vo.ScoreCategorySummaryVO;
import com.hjc.backend.vo.ScoreSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Score Management", description = "Score calculation and ranking")
@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreSummaryController {

    private final ScoreSummaryService scoreSummaryService;

    @Operation(summary = "Recalculate one student's score")
    @PostMapping("/students/{studentId}/recalculate")
    public ApiResponse<ScoreSummaryVO> recalculateStudent(@PathVariable Long studentId) {
        return ApiResponse.success(scoreSummaryService.recalculateStudentScore(studentId));
    }

    @Operation(summary = "Recalculate scores in one class")
    @PostMapping("/classes/{classId}/recalculate")
    public ApiResponse<List<ScoreSummaryVO>> recalculateClass(@PathVariable Long classId) {
        return ApiResponse.success(scoreSummaryService.recalculateClassScores(classId));
    }

    @Operation(summary = "Recalculate scores in one major")
    @PostMapping("/majors/{majorId}/recalculate")
    public ApiResponse<List<ScoreSummaryVO>> recalculateMajor(@PathVariable Long majorId) {
        return ApiResponse.success(scoreSummaryService.recalculateMajorScores(majorId));
    }

    @Operation(summary = "Recalculate all scores")
    @PostMapping("/recalculate-all")
    public ApiResponse<Void> recalculateAll() {
        scoreSummaryService.recalculateAllScores();
        return ApiResponse.success();
    }

    @Operation(summary = "Get one student's total score")
    @GetMapping("/students/{studentId}")
    public ApiResponse<ScoreSummaryVO> getStudentScore(@PathVariable Long studentId) {
        return ApiResponse.success(scoreSummaryService.getStudentScore(studentId));
    }

    @Operation(summary = "List one student's category scores")
    @GetMapping("/students/{studentId}/categories")
    public ApiResponse<List<ScoreCategorySummaryVO>> listStudentCategoryScores(@PathVariable Long studentId) {
        return ApiResponse.success(scoreSummaryService.listStudentCategoryScores(studentId));
    }

    @Operation(summary = "Page score summaries")
    @GetMapping("/page")
    public ApiResponse<PageResult<ScoreSummaryVO>> page(@Valid @ModelAttribute ScoreSummaryPageRequest request) {
        return ApiResponse.success(scoreSummaryService.pageScoreSummaries(request));
    }

    @Operation(summary = "List class ranking")
    @GetMapping("/classes/{classId}/ranking")
    public ApiResponse<List<ScoreSummaryVO>> listClassRanking(@PathVariable Long classId) {
        return ApiResponse.success(scoreSummaryService.listClassRanking(classId));
    }

    @Operation(summary = "List major ranking")
    @GetMapping("/majors/{majorId}/ranking")
    public ApiResponse<List<ScoreSummaryVO>> listMajorRanking(@PathVariable Long majorId) {
        return ApiResponse.success(scoreSummaryService.listMajorRanking(majorId));
    }
}
