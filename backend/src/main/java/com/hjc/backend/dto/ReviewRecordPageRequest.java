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
@Schema(description = "Review record page query request")
public class ReviewRecordPageRequest extends PageRequest {

    private Long materialId;

    private Long reviewerId;

    @Pattern(regexp = "APPROVED|REJECTED", message = "reviewResult must be APPROVED or REJECTED")
    private String reviewResult;

    @Schema(description = "Start review time, ISO date time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startReviewTime;

    @Schema(description = "End review time, ISO date time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endReviewTime;
}
