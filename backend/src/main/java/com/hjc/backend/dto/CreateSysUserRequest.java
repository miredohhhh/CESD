package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create user request")
public class CreateSysUserRequest {

    @NotBlank(message = "username must not be blank")
    @Size(max = 50, message = "username length must be less than or equal to 50")
    private String username;

    // The field name is kept for compatibility; SysUserService stores it as a BCrypt hash.
    @NotBlank(message = "passwordHash must not be blank")
    @Size(max = 255, message = "passwordHash length must be less than or equal to 255")
    private String passwordHash;

    @NotBlank(message = "realName must not be blank")
    @Size(max = 50, message = "realName length must be less than or equal to 50")
    private String realName;

    @NotNull(message = "roleId must not be null")
    private Long roleId;

    @Size(max = 20, message = "phone length must be less than or equal to 20")
    private String phone;

    @Email(message = "email format is invalid")
    @Size(max = 100, message = "email length must be less than or equal to 100")
    private String email;

    @Size(max = 500, message = "avatar length must be less than or equal to 500")
    private String avatar;

    @NotNull(message = "status must not be null")
    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;
}
