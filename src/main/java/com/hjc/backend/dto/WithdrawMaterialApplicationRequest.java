package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Withdraw material application request")
public class WithdrawMaterialApplicationRequest {

    @Schema(description = "Reserved for future extension. It is not persisted in the current table.")
    @Size(max = 500, message = "reason length must be less than or equal to 500")
    private String reason;
}
