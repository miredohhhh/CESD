package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Frontend material application detail response")
public class MaterialApplicationDetailVO {

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

    private List<MaterialAttachmentVO> attachments;

    private List<ReviewRecordVO> reviewRecords;
}
