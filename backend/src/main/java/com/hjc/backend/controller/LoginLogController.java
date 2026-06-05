package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.LoginLogPageRequest;
import com.hjc.backend.service.LoginLogService;
import com.hjc.backend.vo.LoginLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Login Logs", description = "Login log query")
@RestController
@RequestMapping("/api/login-logs")
@RequiredArgsConstructor
public class LoginLogController {

    private final LoginLogService loginLogService;

    @Operation(summary = "Page login logs")
    @GetMapping("/page")
    public ApiResponse<PageResult<LoginLogVO>> page(@Valid @ModelAttribute LoginLogPageRequest request) {
        return ApiResponse.success(loginLogService.pageQuery(request));
    }

    @Operation(summary = "Get login log detail")
    @GetMapping("/{id}")
    public ApiResponse<LoginLogVO> detail(@PathVariable Long id) {
        return ApiResponse.success(loginLogService.getDetail(id));
    }
}
