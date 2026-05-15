package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Score category summary response")
public class ScoreCategorySummaryVO {

    private Long id;

    private Long studentId;

    private Long categoryId;

    private String categoryName;

    private String categoryCode;

    private BigDecimal categoryScore;

    private LocalDateTime calculateTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
