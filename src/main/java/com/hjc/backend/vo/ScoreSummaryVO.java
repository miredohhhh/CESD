package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Score summary response")
public class ScoreSummaryVO {

    private Long id;

    private Long studentId;

    private String studentNo;

    private String studentName;

    private Long classId;

    private String className;

    private Long majorId;

    private String majorName;

    private BigDecimal totalScore;

    private Integer classRank;

    private Integer majorRank;

    private LocalDateTime calculateTime;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
