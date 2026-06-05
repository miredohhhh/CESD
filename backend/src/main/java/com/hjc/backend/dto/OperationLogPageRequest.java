package com.hjc.backend.dto;

import com.hjc.backend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Operation log page request")
public class OperationLogPageRequest extends PageRequest {

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Operation module")
    private String operationModule;

    @Schema(description = "Operation type")
    private String operationType;

    @Schema(description = "Result, SUCCESS or FAIL")
    private String result;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Start operation time")
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "End operation time")
    private LocalDateTime endTime;
}
