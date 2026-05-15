package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Update system config request")
public class UpdateSystemConfigRequest {

    @NotBlank(message = "configKey must not be blank")
    @Size(max = 100, message = "configKey length must be less than or equal to 100")
    private String configKey;

    @NotBlank(message = "configValue must not be blank")
    @Size(max = 500, message = "configValue length must be less than or equal to 500")
    private String configValue;

    @Size(max = 255, message = "description length must be less than or equal to 255")
    private String description;

    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;
}
