package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create class request")
public class CreateClassInfoRequest {

    @NotBlank(message = "className must not be blank")
    @Size(max = 100, message = "className length must be less than or equal to 100")
    private String className;

    @Size(max = 50, message = "classCode length must be less than or equal to 50")
    private String classCode;

    @NotNull(message = "majorId must not be null")
    private Long majorId;

    @NotBlank(message = "grade must not be blank")
    @Size(max = 20, message = "grade length must be less than or equal to 20")
    private String grade;

    @Size(max = 50, message = "counselorName length must be less than or equal to 50")
    private String counselorName;

    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;
}
