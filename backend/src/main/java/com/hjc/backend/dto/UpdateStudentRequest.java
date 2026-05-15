package com.hjc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Update student request")
public class UpdateStudentRequest {

    private Long userId;

    @Size(max = 30, message = "studentNo length must be less than or equal to 30")
    private String studentNo;

    @Size(max = 50, message = "name length must be less than or equal to 50")
    private String name;

    @Pattern(regexp = "M|F", message = "gender must be M or F")
    private String gender;

    @Size(max = 20, message = "grade length must be less than or equal to 20")
    private String grade;

    private Long majorId;

    private Long classId;

    @Size(max = 20, message = "phone length must be less than or equal to 20")
    private String phone;

    @Email(message = "email format is invalid")
    @Size(max = 100, message = "email length must be less than or equal to 100")
    private String email;

    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;
}
