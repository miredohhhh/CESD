package com.hjc.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("score_summary")
@Schema(description = "总成绩统计表")
public class ScoreSummary {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("student_id")
    private Long studentId;

    @TableField("total_score")
    private BigDecimal totalScore;

    @TableField("class_rank")
    private Integer classRank;

    @TableField("major_rank")
    private Integer majorRank;

    @TableField("calculate_time")
    private LocalDateTime calculateTime;

    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
