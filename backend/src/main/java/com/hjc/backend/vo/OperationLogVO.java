package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Operation log view object")
public class OperationLogVO {

    private Long id;

    private Long userId;

    private String username;

    private String realName;

    private String roleCode;

    private String operationType;

    private String operationModule;

    private String operationDesc;

    private String requestMethod;

    private String requestUri;

    private String requestParams;

    private String ipAddress;

    private String userAgent;

    private String result;

    private String errorMessage;

    private LocalDateTime operationTime;

    private LocalDateTime createTime;
}
