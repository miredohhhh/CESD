package com.hjc.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reviewer_scope")
@Schema(description = "审核范围表")
public class ReviewerScope {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("reviewer_id")
    private Long reviewerId;

    @TableField("major_id")
    private Long majorId;

    @TableField("class_id")
    private Long classId;

    private String grade;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
