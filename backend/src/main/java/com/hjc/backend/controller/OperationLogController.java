package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.OperationLogPageRequest;
import com.hjc.backend.service.OperationLogService;
import com.hjc.backend.vo.OperationLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Operation Logs", description = "Operation log query")
@RestController
@RequestMapping("/api/operation-logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @Operation(summary = "Page operation logs")
    @GetMapping("/page")
    public ApiResponse<PageResult<OperationLogVO>> page(@Valid @ModelAttribute OperationLogPageRequest request) {
        return ApiResponse.success(operationLogService.pageQuery(request));
    }

    @Operation(summary = "Get operation log detail")
    @GetMapping("/{id}")
    public ApiResponse<OperationLogVO> detail(@PathVariable Long id) {
        return ApiResponse.success(operationLogService.getDetail(id));
    }
}
