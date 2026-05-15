package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Evaluation category response")
public class EvaluationCategoryVO {

    private Long id;

    private String categoryName;

    private String categoryCode;

    private BigDecimal maxScore;

    private Integer sortNo;

    private String description;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
