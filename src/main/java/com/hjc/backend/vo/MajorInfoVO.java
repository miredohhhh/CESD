package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Major response")
public class MajorInfoVO {

    private Long id;

    private String majorName;

    private String majorCode;

    private String collegeName;

    private String description;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
