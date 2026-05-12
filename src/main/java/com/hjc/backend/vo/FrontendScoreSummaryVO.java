package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Frontend score summary response")
public class FrontendScoreSummaryVO {

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

    private List<ScoreCategorySummaryVO> categoryScores;
}
