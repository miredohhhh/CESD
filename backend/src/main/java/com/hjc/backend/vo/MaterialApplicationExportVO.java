package com.hjc.backend.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MaterialApplicationExportVO {

    private Long id;

    private String title;

    private Long studentId;

    private String studentNo;

    private String studentName;

    private Long majorId;

    private String majorName;

    private Long classId;

    private String className;

    private Long categoryId;

    private String categoryName;

    private Long itemId;

    private String itemName;

    private BigDecimal applyScore;

    private BigDecimal finalScore;

    private String status;

    private Integer attachmentCount;

    private LocalDateTime submitTime;

    private LocalDateTime reviewTime;

    private String rejectReason;
}
