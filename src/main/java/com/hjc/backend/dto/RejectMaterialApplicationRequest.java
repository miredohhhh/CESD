package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Reject material application request")
public class RejectMaterialApplicationRequest {

    // TODO After authentication is added, reviewerId must come from the current user context.
    @NotNull(message = "reviewerId must not be null")
    private Long reviewerId;

    @NotBlank(message = "rejectReason must not be blank")
    @Size(max = 500, message = "rejectReason length must be less than or equal to 500")
    private String rejectReason;

    @Size(max = 500, message = "reviewComment length must be less than or equal to 500")
    private String reviewComment;
}
