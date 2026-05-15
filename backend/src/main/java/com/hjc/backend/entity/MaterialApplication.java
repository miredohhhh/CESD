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
@TableName("material_application")
@Schema(description = "材料申报表")
public class MaterialApplication {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("student_id")
    private Long studentId;

    @TableField("item_id")
    private Long itemId;

    private String title;

    private String description;

    @TableField("apply_score")
    private BigDecimal applyScore;

    @TableField("final_score")
    private BigDecimal finalScore;

    private String status;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("submit_count")
    private Integer submitCount;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("review_time")
    private LocalDateTime reviewTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
