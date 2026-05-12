package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateEvaluationItemRequest;
import com.hjc.backend.dto.UpdateEvaluationItemRequest;
import com.hjc.backend.service.EvaluationItemService;
import com.hjc.backend.vo.EvaluationItemVO;
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

@Tag(name = "Evaluation Item Management", description = "Evaluation item basic data CRUD")
@RestController
@RequestMapping("/api/evaluation-items")
@RequiredArgsConstructor
public class EvaluationItemController {

    private final EvaluationItemService evaluationItemService;

    @Operation(summary = "Page evaluation items")
    @GetMapping("/page")
    public ApiResponse<PageResult<EvaluationItemVO>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(evaluationItemService.pageQuery(pageNum, pageSize, keyword, status));
    }

    @Operation(summary = "Get evaluation item detail")
    @GetMapping("/{id}")
    public ApiResponse<EvaluationItemVO> detail(@PathVariable Long id) {
        return ApiResponse.success(evaluationItemService.getDetail(id));
    }

    @Operation(summary = "Create evaluation item")
    @PostMapping
    public ApiResponse<EvaluationItemVO> create(@Valid @RequestBody CreateEvaluationItemRequest request) {
        return ApiResponse.success(evaluationItemService.create(request));
    }

    @Operation(summary = "Update evaluation item")
    @PutMapping("/{id}")
    public ApiResponse<EvaluationItemVO> update(@PathVariable Long id, @Valid @RequestBody UpdateEvaluationItemRequest request) {
        return ApiResponse.success(evaluationItemService.update(id, request));
    }

    @Operation(summary = "Delete evaluation item")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        evaluationItemService.deleteById(id);
        return ApiResponse.success();
    }
}
