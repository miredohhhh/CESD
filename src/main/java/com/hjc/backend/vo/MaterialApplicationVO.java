package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Material application response")
public class MaterialApplicationVO {

    private Long id;

    private Long studentId;

    private Long itemId;

    private String title;

    private String description;

    private BigDecimal applyScore;

    private BigDecimal finalScore;

    private String status;

    private String rejectReason;

    private Integer submitCount;

    private LocalDateTime submitTime;

    private LocalDateTime reviewTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
