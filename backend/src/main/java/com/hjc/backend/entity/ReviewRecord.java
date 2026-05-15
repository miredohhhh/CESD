package com.hjc.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("review_record")
@Schema(description = "审核记录表")
public class ReviewRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("material_id")
    private Long materialId;

    @TableField("reviewer_id")
    private Long reviewerId;

    @TableField("before_status")
    private String beforeStatus;

    @TableField("after_status")
    private String afterStatus;

    @TableField("review_result")
    private String reviewResult;

    @TableField("review_score")
    private BigDecimal reviewScore;

    @TableField("review_comment")
    private String reviewComment;

    @TableField("review_time")
    private LocalDateTime reviewTime;

    @TableField("create_time")
    private LocalDateTime createTime;
}
