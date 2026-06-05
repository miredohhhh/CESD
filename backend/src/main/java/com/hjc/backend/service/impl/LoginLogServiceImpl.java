package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.common.ResultCode;
import com.hjc.backend.dto.LoginLogPageRequest;
import com.hjc.backend.entity.LoginLog;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.mapper.LoginLogMapper;
import com.hjc.backend.security.CurrentUserUtils;
import com.hjc.backend.security.LoginUserContext;
import com.hjc.backend.service.LoginLogService;
import com.hjc.backend.utils.LogContextUtils;
import com.hjc.backend.vo.LoginLogVO;
import com.hjc.backend.vo.LoginUserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements LoginLogService {

    @Override
    public PageResult<LoginLogVO> pageQuery(LoginLogPageRequest request) {
        Page<LoginLog> page = new Page<>(normalizePageNum(request.getPageNum()), normalizePageSize(request.getPageSize()));
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(request.getUsername()), LoginLog::getUsername, request.getUsername())
                .eq(StringUtils.hasText(request.getLoginType()), LoginLog::getLoginType, request.getLoginType())
                .eq(StringUtils.hasText(request.getResult()), LoginLog::getResult, request.getResult())
                .ge(request.getStartTime() != null, LoginLog::getLoginTime, request.getStartTime())
                .le(request.getEndTime() != null, LoginLog::getLoginTime, request.getEndTime())
                .orderByDesc(LoginLog::getLoginTime)
                .orderByDesc(LoginLog::getId);
        Page<LoginLog> result = page(page, wrapper);
        List<LoginLogVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public LoginLogVO getDetail(Long id) {
        LoginLog entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Login log does not exist");
        }
        return toVO(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLoginSuccess(LoginUserVO user) {
        LoginLog entity = baseLog(TYPE_LOGIN, RESULT_SUCCESS, null);
        if (user != null) {
            entity.setUserId(user.getUserId());
            entity.setUsername(user.getUsername());
            entity.setRealName(user.getRealName());
            entity.setRoleCode(user.getRoleCode());
        }
        save(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLoginFailure(String username, String errorMessage) {
        LoginLog entity = baseLog(TYPE_LOGIN, RESULT_FAIL, errorMessage);
        entity.setUsername(LogContextUtils.limit(username, 50));
        save(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLogoutSuccess() {
        LoginLog entity = baseLog(TYPE_LOGOUT, RESULT_SUCCESS, null);
        LoginUserContext context = CurrentUserUtils.getContext();
        if (context != null) {
            entity.setUserId(context.getUserId());
            entity.setUsername(context.getUsername());
            entity.setRoleCode(context.getRoleCode());
        }
        save(entity);
    }

    private LoginLog baseLog(String loginType, String result, String errorMessage) {
        LocalDateTime now = LocalDateTime.now();
        LoginLog entity = new LoginLog();
        entity.setLoginType(loginType);
        entity.setResult(result);
        entity.setIpAddress(LogContextUtils.ipAddress());
        entity.setUserAgent(LogContextUtils.userAgent());
        entity.setErrorMessage(LogContextUtils.sanitize(errorMessage));
        entity.setLoginTime(now);
        entity.setCreateTime(now);
        return entity;
    }

    private LoginLogVO toVO(LoginLog entity) {
        LoginLogVO vo = new LoginLogVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setUsername(entity.getUsername());
        vo.setRealName(entity.getRealName());
        vo.setRoleCode(entity.getRoleCode());
        vo.setLoginType(entity.getLoginType());
        vo.setResult(entity.getResult());
        vo.setIpAddress(entity.getIpAddress());
        vo.setUserAgent(entity.getUserAgent());
        vo.setErrorMessage(entity.getErrorMessage());
        vo.setLoginTime(entity.getLoginTime());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private long normalizePageNum(Long pageNum) {
        return pageNum == null || pageNum < 1 ? 1L : pageNum;
    }

    private long normalizePageSize(Long pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10L;
        }
        return Math.min(pageSize, 100L);
    }
}
