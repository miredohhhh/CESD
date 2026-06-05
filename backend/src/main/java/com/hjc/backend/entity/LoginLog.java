package com.hjc.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("login_log")
@Schema(description = "Login log")
public class LoginLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String username;

    @TableField("real_name")
    private String realName;

    @TableField("role_code")
    private String roleCode;

    @TableField("login_type")
    private String loginType;

    private String result;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField("error_message")
    private String errorMessage;

    @TableField("login_time")
    private LocalDateTime loginTime;

    @TableField("create_time")
    private LocalDateTime createTime;
}
