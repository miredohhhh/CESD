package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Reset user password request")
public class ResetSysUserPasswordRequest {

    @NotBlank(message = "newPassword must not be blank")
    @Size(min = 6, max = 100, message = "newPassword length must be between 6 and 100")
    private String newPassword;
}
