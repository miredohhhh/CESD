package com.hjc.backend.dto;

import com.hjc.backend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Frontend my material application page request")
public class MyMaterialApplicationPageRequest extends PageRequest {

    @Schema(description = "Frontend page number alias")
    private Long pageNo;

    @Pattern(regexp = "DRAFT|SUBMITTED|APPROVED|REJECTED|CANCELLED", message = "status must be DRAFT, SUBMITTED, APPROVED, REJECTED, or CANCELLED")
    @Schema(description = "Material status")
    private String status;

    @Schema(description = "Evaluation item ID")
    private Long itemId;

    @Schema(description = "Evaluation category ID")
    private Long categoryId;

    @Schema(description = "Keyword for title or description")
    private String keyword;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Start create time")
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "End create time")
    private LocalDateTime endTime;
}
