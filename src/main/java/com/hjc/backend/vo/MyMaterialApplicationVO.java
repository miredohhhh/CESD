package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Frontend my material application response")
public class MyMaterialApplicationVO {

    private Long id;

    private Long studentId;

    private Long itemId;

    private String itemName;

    private Long categoryId;

    private String categoryName;

    private String title;

    private String description;

    private BigDecimal applyScore;

    private BigDecimal finalScore;

    private String status;

    private LocalDateTime submitTime;

    private LocalDateTime reviewTime;

    private String rejectReason;

    private Integer submitCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer attachmentCount;
}
