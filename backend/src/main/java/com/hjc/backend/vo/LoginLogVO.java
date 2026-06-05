package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Login log view object")
public class LoginLogVO {

    private Long id;

    private Long userId;

    private String username;

    private String realName;

    private String roleCode;

    private String loginType;

    private String result;

    private String ipAddress;

    private String userAgent;

    private String errorMessage;

    private LocalDateTime loginTime;

    private LocalDateTime createTime;
}
