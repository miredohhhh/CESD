package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "测试参数校验请求")
public class TestRequest {

    @Schema(description = "名称", example = "test")
    @NotBlank(message = "name must not be blank")
    private String name;

    @Schema(description = "邮箱", example = "test@example.com")
    @Email(message = "email format is invalid")
    private String email;
}
