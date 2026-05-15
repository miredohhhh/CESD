package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Update user request")
public class UpdateSysUserRequest {

    @Size(max = 50, message = "username length must be less than or equal to 50")
    private String username;

    // TODO Generate this value with BCrypt in backend when login/register is introduced.
    @Size(max = 255, message = "passwordHash length must be less than or equal to 255")
    private String passwordHash;

    @Size(max = 50, message = "realName length must be less than or equal to 50")
    private String realName;

    private Long roleId;

    @Size(max = 20, message = "phone length must be less than or equal to 20")
    private String phone;

    @Email(message = "email format is invalid")
    @Size(max = 100, message = "email length must be less than or equal to 100")
    private String email;

    @Size(max = 500, message = "avatar length must be less than or equal to 500")
    private String avatar;

    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;
}
