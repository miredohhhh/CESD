package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "JWT")
    private String token;

    @Schema(description = "Token 类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "过期秒数")
    private Long expiresIn;

    @Schema(description = "当前登录用户信息")
    private LoginUserVO user;
}
