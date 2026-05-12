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
@TableName("class_info")
@Schema(description = "班级表")
public class ClassInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("class_name")
    private String className;

    @TableField("class_code")
    private String classCode;

    @TableField("major_id")
    private Long majorId;

    private String grade;

    @TableField("counselor_name")
    private String counselorName;

    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
