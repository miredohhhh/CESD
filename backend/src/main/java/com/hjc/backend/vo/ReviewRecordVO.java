package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Review record response")
public class ReviewRecordVO {

    private Long id;

    private Long materialId;

    private Long reviewerId;

    private String beforeStatus;

    private String afterStatus;

    private String reviewResult;

    private BigDecimal reviewScore;

    private String reviewComment;

    private LocalDateTime reviewTime;

    private LocalDateTime createTime;
}
