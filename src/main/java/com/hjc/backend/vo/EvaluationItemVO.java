package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Evaluation item response")
public class EvaluationItemVO {

    private Long id;

    private Long categoryId;

    private String itemName;

    private String itemCode;

    private String scoreType;

    private BigDecimal score;

    private BigDecimal maxScore;

    private Integer needAttachment;

    private String description;

    private Integer sortNo;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
