package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create permission request")
public class CreateSysPermissionRequest {

    @NotBlank(message = "permissionName must not be blank")
    @Size(max = 100)
    private String permissionName;

    @NotBlank(message = "permissionCode must not be blank")
    @Size(max = 100)
    private String permissionCode;

    @NotBlank(message = "permissionType must not be blank")
    @Pattern(regexp = "MENU|BUTTON|API", message = "permissionType must be MENU, BUTTON, or API")
    private String permissionType;

    private Long parentId;

    @Size(max = 255)
    private String routePath;

    @Size(max = 255)
    private String componentPath;

    @Size(max = 255)
    private String apiPath;

    @Size(max = 20)
    private String httpMethod;

    @Size(max = 100)
    private String icon;

    @Min(0)
    private Integer sortOrder;

    private Integer status;

    @Size(max = 500)
    private String remark;
}
