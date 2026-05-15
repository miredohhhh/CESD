package com.hjc.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Student response")
public class StudentVO {

    // TODO Add username, majorName, and className in later list/detail queries.

    private Long id;

    private Long userId;

    private String studentNo;

    private String name;

    private String gender;

    private String grade;

    private Long majorId;

    private Long classId;

    private String phone;

    private String email;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
