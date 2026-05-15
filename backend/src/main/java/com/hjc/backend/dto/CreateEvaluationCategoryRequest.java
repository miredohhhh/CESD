package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Create evaluation category request")
public class CreateEvaluationCategoryRequest {

    @NotBlank(message = "categoryName must not be blank")
    @Size(max = 100, message = "categoryName length must be less than or equal to 100")
    private String categoryName;

    @Size(max = 50, message = "categoryCode length must be less than or equal to 50")
    private String categoryCode;

    @DecimalMin(value = "0.00", message = "maxScore must be greater than or equal to 0")
    private BigDecimal maxScore;

    @Min(value = 0, message = "sortNo must be greater than or equal to 0")
    private Integer sortNo;

    @Size(max = 255, message = "description length must be less than or equal to 255")
    private String description;

    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;
}
