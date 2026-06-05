package com.hjc.backend.dto;

import com.hjc.backend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Login log page request")
public class LoginLogPageRequest extends PageRequest {

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Login type, LOGIN or LOGOUT")
    private String loginType;

    @Schema(description = "Result, SUCCESS or FAIL")
    private String result;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Start login time")
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "End login time")
    private LocalDateTime endTime;
}
