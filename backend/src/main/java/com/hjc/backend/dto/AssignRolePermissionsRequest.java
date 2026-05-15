package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Assign role permissions request")
public class AssignRolePermissionsRequest {

    @NotNull(message = "permissionIds must not be null")
    private List<Long> permissionIds;
}
