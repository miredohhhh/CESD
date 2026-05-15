package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create role request")
public class CreateSysRoleRequest {

    @NotBlank(message = "roleName must not be blank")
    @Size(max = 50, message = "roleName length must be less than or equal to 50")
    @Schema(description = "Role name", example = "管理员")
    private String roleName;

    @NotBlank(message = "roleCode must not be blank")
    @Size(max = 50, message = "roleCode length must be less than or equal to 50")
    @Schema(description = "Role code", example = "ADMIN")
    private String roleCode;

    @Size(max = 255, message = "description length must be less than or equal to 255")
    @Schema(description = "Description")
    private String description;

    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    @Schema(description = "Status, 1 enabled, 0 disabled", example = "1")
    private Integer status;
}
