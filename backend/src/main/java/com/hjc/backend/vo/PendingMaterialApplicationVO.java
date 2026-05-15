package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Frontend pending material application response")
public class PendingMaterialApplicationVO {

    private Long id;

    private Long studentId;

    private String studentNo;

    private String studentName;

    private Long classId;

    private String className;

    private Long majorId;

    private String majorName;

    private Long itemId;

    private String itemName;

    private Long categoryId;

    private String categoryName;

    private String title;

    private BigDecimal applyScore;

    private String status;

    private LocalDateTime submitTime;

    private Integer attachmentCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
