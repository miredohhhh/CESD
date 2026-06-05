package com.hjc.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
@Schema(description = "Operation log")
public class OperationLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String username;

    @TableField("real_name")
    private String realName;

    @TableField("role_code")
    private String roleCode;

    @TableField("operation_type")
    private String operationType;

    @TableField("operation_module")
    private String operationModule;

    @TableField("operation_desc")
    private String operationDesc;

    @TableField("request_method")
    private String requestMethod;

    @TableField("request_uri")
    private String requestUri;

    @TableField("request_params")
    private String requestParams;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    private String result;

    @TableField("error_message")
    private String errorMessage;

    @TableField("operation_time")
    private LocalDateTime operationTime;

    @TableField("create_time")
    private LocalDateTime createTime;
}
