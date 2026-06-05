package com.hjc.backend.service.impl;

import com.hjc.backend.common.ResultCode;
import com.hjc.backend.dto.ChangePasswordRequest;
import com.hjc.backend.dto.LoginRequest;
import com.hjc.backend.entity.Student;
import com.hjc.backend.entity.SysRole;
import com.hjc.backend.entity.SysUser;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.security.CurrentUserUtils;
import com.hjc.backend.security.JwtTokenProvider;
import com.hjc.backend.security.LoginUserContext;
import com.hjc.backend.security.LoginUserContextHolder;
import com.hjc.backend.service.AuthService;
import com.hjc.backend.service.LoginLogService;
import com.hjc.backend.service.StudentService;
import com.hjc.backend.service.SysPermissionService;
import com.hjc.backend.service.SysRoleService;
import com.hjc.backend.service.SysUserService;
import com.hjc.backend.vo.CurrentUserPermissionVO;
import com.hjc.backend.vo.LoginUserVO;
import com.hjc.backend.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Integer ENABLED = 1;

    private final SysUserService sysUserService;

    private final SysRoleService sysRoleService;

    private final StudentService studentService;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    private final SysPermissionService sysPermissionService;

    private final LoginLogService loginLogService;

    @Override
    @Transactional
    public LoginVO login(LoginRequest request) {
        SysUser user = sysUserService.lambdaQuery()
                .eq(SysUser::getUsername, request.getUsername())
                .one();
        if (user == null || !ENABLED.equals(user.getStatus())) {
            recordLoginFailureQuietly(request.getUsername(), "Username or password is incorrect");
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Username or password is incorrect");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            recordLoginFailureQuietly(request.getUsername(), "Username or password is incorrect");
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Username or password is incorrect");
        }

        LoginUserVO loginUser = buildLoginUser(user);
        user.setLastLoginTime(LocalDateTime.now());
        sysUserService.updateById(user);

        LoginVO vo = new LoginVO();
        vo.setToken(jwtTokenProvider.generateToken(loginUser));
        vo.setTokenType("Bearer");
        vo.setExpiresIn(jwtTokenProvider.getExpirationSeconds());
        vo.setUser(loginUser);
        loginLogService.recordLoginSuccess(loginUser);
        return vo;
    }

    @Override
    public LoginUserVO getCurrentUser() {
        LoginUserContext context = LoginUserContextHolder.get();
        if (context == null || context.getUserId() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Unauthorized");
        }
        SysUser user = sysUserService.getById(context.getUserId());
        if (user == null || !ENABLED.equals(user.getStatus())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Unauthorized");
        }
        return buildLoginUser(user);
    }

    @Override
    public CurrentUserPermissionVO getCurrentUserPermissions() {
        return sysPermissionService.getCurrentUserPermissions();
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Long userId = CurrentUserUtils.requireUserId();
        SysUser user = sysUserService.getById(userId);
        if (user == null || !ENABLED.equals(user.getStatus())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Unauthorized");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "New password and confirm password do not match");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Old password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "New password cannot be the same as old password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        sysUserService.updateById(user);
    }

    @Override
    public void logout() {
        CurrentUserUtils.requireUserId();
        loginLogService.recordLogoutSuccess();
        // TODO: Add the current token to a Redis blacklist after token invalidation is introduced.
    }

    private LoginUserVO buildLoginUser(SysUser user) {
        SysRole role = sysRoleService.getById(user.getRoleId());
        if (role == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "User role does not exist");
        }
        LoginUserVO vo = new LoginUserVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRoleId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setStudentId(findStudentId(user.getId()));
        return vo;
    }

    private Long findStudentId(Long userId) {
        Student student = studentService.lambdaQuery()
                .eq(Student::getUserId, userId)
                .eq(Student::getStatus, ENABLED)
                .one();
        return student == null ? null : student.getId();
    }

    private void recordLoginFailureQuietly(String username, String errorMessage) {
        try {
            loginLogService.recordLoginFailure(username, errorMessage);
        } catch (RuntimeException ignored) {
            // Failed login logging must not mask the original authentication failure response.
        }
    }
}
