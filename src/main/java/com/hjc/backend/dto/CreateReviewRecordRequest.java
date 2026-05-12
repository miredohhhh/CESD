package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Create review record request")
public class CreateReviewRecordRequest {

    @NotNull(message = "materialId must not be null")
    private Long materialId;

    @NotNull(message = "reviewerId must not be null")
    private Long reviewerId;

    @NotBlank(message = "beforeStatus must not be blank")
    @Pattern(regexp = "DRAFT|SUBMITTED|APPROVED|REJECTED|CANCELLED", message = "beforeStatus must be DRAFT, SUBMITTED, APPROVED, REJECTED, or CANCELLED")
    @Size(max = 30, message = "beforeStatus length must be less than or equal to 30")
    private String beforeStatus;

    @NotBlank(message = "afterStatus must not be blank")
    @Pattern(regexp = "DRAFT|SUBMITTED|APPROVED|REJECTED|CANCELLED", message = "afterStatus must be DRAFT, SUBMITTED, APPROVED, REJECTED, or CANCELLED")
    @Size(max = 30, message = "afterStatus length must be less than or equal to 30")
    private String afterStatus;

    @NotBlank(message = "reviewResult must not be blank")
    @Pattern(regexp = "APPROVED|REJECTED", message = "reviewResult must be APPROVED or REJECTED")
    @Size(max = 30, message = "reviewResult length must be less than or equal to 30")
    private String reviewResult;

    @DecimalMin(value = "0.00", message = "reviewScore must be greater than or equal to 0")
    private BigDecimal reviewScore;

    @Size(max = 500, message = "reviewComment length must be less than or equal to 500")
    private String reviewComment;

    @Schema(description = "Review time. If empty, backend sets current time.")
    private LocalDateTime reviewTime;
}
