package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.dto.TestRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Test", description = "测试接口")
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Operation(summary = "Ping 测试", description = "返回 pong 用于验证接口连通性")
    @GetMapping("/ping")
    public ApiResponse<Map<String, String>> ping() {
        return ApiResponse.success(Map.of("message", "pong"));
    }

    @Operation(summary = "参数校验测试", description = "用于验证全局参数校验异常处理")
    @PostMapping("/validate")
    public ApiResponse<TestRequest> validate(@Valid @RequestBody TestRequest request) {
        return ApiResponse.success("Validation passed", request);
    }
}
