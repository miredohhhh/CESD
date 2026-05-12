package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "User response")
public class SysUserVO {

    // TODO Add roleName from sys_role in later list/detail queries.

    private Long id;

    private String username;

    private String realName;

    private Long roleId;

    private String phone;

    private String email;

    private String avatar;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
