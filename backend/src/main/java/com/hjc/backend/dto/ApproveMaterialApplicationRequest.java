package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Approve material application request")
public class ApproveMaterialApplicationRequest {

    @NotNull(message = "reviewScore must not be null")
    @DecimalMin(value = "0.00", message = "reviewScore must be greater than or equal to 0")
    private BigDecimal reviewScore;

    @Size(max = 500, message = "reviewComment length must be less than or equal to 500")
    private String reviewComment;
}
