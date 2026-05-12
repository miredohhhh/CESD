package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Health", description = "健康检查接口")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Operation(summary = "健康检查", description = "检查后端 API 服务是否正常运行")
    @GetMapping
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success(Map.of(
                "status", "UP",
                "service", "backend"
        ));
    }
}
