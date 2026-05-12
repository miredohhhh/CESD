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
@TableName("material_attachment")
@Schema(description = "材料附件表")
public class MaterialAttachment {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("material_id")
    private Long materialId;

    @TableField("original_name")
    private String originalName;

    @TableField("stored_name")
    private String storedName;

    @TableField("file_path")
    private String filePath;

    @TableField("file_url")
    private String fileUrl;

    @TableField("file_type")
    private String fileType;

    @TableField("file_size")
    private Long fileSize;

    @TableField("upload_time")
    private LocalDateTime uploadTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
