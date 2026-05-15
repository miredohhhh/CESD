package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Frontend my material application statistics response")
public class MyApplicationStatisticsVO {

    private Long studentId;

    private Long totalCount;

    private Long draftCount;

    private Long submittedCount;

    private Long approvedCount;

    private Long rejectedCount;

    private Long cancelledCount;
}
