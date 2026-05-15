package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Update evaluation item request")
public class UpdateEvaluationItemRequest {

    @NotNull(message = "categoryId must not be null")
    private Long categoryId;

    @NotBlank(message = "itemName must not be blank")
    @Size(max = 100, message = "itemName length must be less than or equal to 100")
    private String itemName;

    @Size(max = 50, message = "itemCode length must be less than or equal to 50")
    private String itemCode;

    @NotBlank(message = "scoreType must not be blank")
    @Pattern(regexp = "FIXED|MANUAL|RANGE", message = "scoreType must be FIXED, MANUAL, or RANGE")
    @Size(max = 30, message = "scoreType length must be less than or equal to 30")
    private String scoreType;

    @DecimalMin(value = "0.00", message = "score must be greater than or equal to 0")
    private BigDecimal score;

    @DecimalMin(value = "0.00", message = "maxScore must be greater than or equal to 0")
    private BigDecimal maxScore;

    @Min(value = 0, message = "needAttachment must be 0 or 1")
    @Max(value = 1, message = "needAttachment must be 0 or 1")
    private Integer needAttachment;

    @Size(max = 500, message = "description length must be less than or equal to 500")
    private String description;

    @Min(value = 0, message = "sortNo must be greater than or equal to 0")
    private Integer sortNo;

    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;
}
