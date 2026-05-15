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
@TableName("evaluation_item")
@Schema(description = "综测项目表")
public class EvaluationItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("category_id")
    private Long categoryId;

    @TableField("item_name")
    private String itemName;

    @TableField("item_code")
    private String itemCode;

    @TableField("score_type")
    private String scoreType;

    private BigDecimal score;

    @TableField("max_score")
    private BigDecimal maxScore;

    @TableField("need_attachment")
    private Integer needAttachment;

    private String description;

    @TableField("sort_no")
    private Integer sortNo;

    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
