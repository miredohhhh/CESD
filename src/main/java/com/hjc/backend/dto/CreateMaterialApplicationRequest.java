package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Create material application request")
public class CreateMaterialApplicationRequest {

    @NotNull(message = "studentId must not be null")
    private Long studentId;

    @NotNull(message = "itemId must not be null")
    private Long itemId;

    @NotBlank(message = "title must not be blank")
    @Size(max = 200, message = "title length must be less than or equal to 200")
    private String title;

    private String description;

    @DecimalMin(value = "0.00", message = "applyScore must be greater than or equal to 0")
    private BigDecimal applyScore;

    @DecimalMin(value = "0.00", message = "finalScore must be greater than or equal to 0")
    private BigDecimal finalScore;

    @Pattern(regexp = "DRAFT|SUBMITTED|APPROVED|REJECTED|CANCELLED", message = "status must be DRAFT, SUBMITTED, APPROVED, REJECTED, or CANCELLED")
    @Size(max = 30, message = "status length must be less than or equal to 30")
    private String status;

    @Size(max = 500, message = "rejectReason length must be less than or equal to 500")
    private String rejectReason;

    @Min(value = 0, message = "submitCount must be greater than or equal to 0")
    private Integer submitCount;
}
