package com.hjc.backend.service;

import com.hjc.backend.dto.LoginRequest;
import com.hjc.backend.vo.CurrentUserPermissionVO;
import com.hjc.backend.vo.LoginUserVO;
import com.hjc.backend.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginRequest request);

    LoginUserVO getCurrentUser();

    CurrentUserPermissionVO getCurrentUserPermissions();
}
