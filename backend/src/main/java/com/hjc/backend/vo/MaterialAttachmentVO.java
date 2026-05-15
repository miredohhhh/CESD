package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Material attachment metadata response")
public class MaterialAttachmentVO {

    private Long id;

    private Long materialId;

    private String originalName;

    private String storedName;

    private String filePath;

    private String fileUrl;

    private String fileType;

    private Long fileSize;

    private LocalDateTime uploadTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
