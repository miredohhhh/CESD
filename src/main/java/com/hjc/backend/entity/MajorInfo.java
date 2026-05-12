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
@TableName("major_info")
@Schema(description = "专业表")
public class MajorInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("major_name")
    private String majorName;

    @TableField("major_code")
    private String majorCode;

    @TableField("college_name")
    private String collegeName;

    private String description;

    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
