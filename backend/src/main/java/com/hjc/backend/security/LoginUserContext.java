package com.hjc.backend.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserContext {

    private Long userId;

    private String username;

    private Long roleId;

    private String roleCode;

    private String roleName;

    private Long studentId;
}
