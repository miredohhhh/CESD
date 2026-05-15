package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Class response")
public class ClassInfoVO {

    private Long id;

    private String className;

    private String classCode;

    private Long majorId;

    private String grade;

    private String counselorName;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
