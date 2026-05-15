package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.dto.LoginRequest;
import com.hjc.backend.service.AuthService;
import com.hjc.backend.vo.CurrentUserPermissionVO;
import com.hjc.backend.vo.LoginUserVO;
import com.hjc.backend.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login by username and password")
    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @Operation(summary = "Get current login user")
    @GetMapping("/me")
    public ApiResponse<LoginUserVO> me() {
        return ApiResponse.success(authService.getCurrentUser());
    }

    @Operation(summary = "Get current user permissions")
    @GetMapping("/permissions")
    public ApiResponse<CurrentUserPermissionVO> permissions() {
        return ApiResponse.success(authService.getCurrentUserPermissions());
    }
}
