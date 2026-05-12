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
@TableName("evaluation_category")
@Schema(description = "综测分类表")
public class EvaluationCategory {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("category_name")
    private String categoryName;

    @TableField("category_code")
    private String categoryCode;

    @TableField("max_score")
    private BigDecimal maxScore;

    @TableField("sort_no")
    private Integer sortNo;

    private String description;

    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
