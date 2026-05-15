package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Update major request")
public class UpdateMajorInfoRequest {

    @NotBlank(message = "majorName must not be blank")
    @Size(max = 100, message = "majorName length must be less than or equal to 100")
    private String majorName;

    @Size(max = 50, message = "majorCode length must be less than or equal to 50")
    private String majorCode;

    @NotBlank(message = "collegeName must not be blank")
    @Size(max = 100, message = "collegeName length must be less than or equal to 100")
    private String collegeName;

    @Size(max = 255, message = "description length must be less than or equal to 255")
    private String description;

    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;
}
