package com.hjc.backend.dto;

import com.hjc.backend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Score summary page query request")
public class ScoreSummaryPageRequest extends PageRequest {

    private Long studentId;

    @Schema(description = "Student number")
    private String studentNo;

    @Schema(description = "Student name")
    private String studentName;

    private Long classId;

    private Long majorId;

    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;
}
