package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Schema(description = "Material application export query request")
public class MaterialApplicationExportRequest {

    @Pattern(regexp = "DRAFT|SUBMITTED|APPROVED|REJECTED|CANCELLED", message = "status must be DRAFT, SUBMITTED, APPROVED, REJECTED, or CANCELLED")
    @Schema(description = "Material status")
    private String status;

    @Schema(description = "Student ID")
    private Long studentId;

    @Schema(description = "Student number")
    private String studentNo;

    @Schema(description = "Student name")
    private String studentName;

    @Schema(description = "Class ID")
    private Long classId;

    @Schema(description = "Major ID")
    private Long majorId;

    @Schema(description = "Evaluation item ID")
    private Long itemId;

    @Schema(description = "Evaluation category ID")
    private Long categoryId;

    @Schema(description = "Keyword for title or description")
    private String keyword;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Start submit time")
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "End submit time")
    private LocalDateTime endTime;
}
