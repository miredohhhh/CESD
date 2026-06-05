package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.common.ResultCode;
import com.hjc.backend.dto.OperationLogPageRequest;
import com.hjc.backend.entity.OperationLog;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.mapper.OperationLogMapper;
import com.hjc.backend.security.CurrentUserUtils;
import com.hjc.backend.security.LoginUserContext;
import com.hjc.backend.service.OperationLogService;
import com.hjc.backend.utils.LogContextUtils;
import com.hjc.backend.vo.OperationLogVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public PageResult<OperationLogVO> pageQuery(OperationLogPageRequest request) {
        Page<OperationLog> page = new Page<>(normalizePageNum(request.getPageNum()), normalizePageSize(request.getPageSize()));
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(request.getUsername()), OperationLog::getUsername, request.getUsername())
                .eq(StringUtils.hasText(request.getOperationModule()), OperationLog::getOperationModule, request.getOperationModule())
                .eq(StringUtils.hasText(request.getOperationType()), OperationLog::getOperationType, request.getOperationType())
                .eq(StringUtils.hasText(request.getResult()), OperationLog::getResult, request.getResult())
                .ge(request.getStartTime() != null, OperationLog::getOperationTime, request.getStartTime())
                .le(request.getEndTime() != null, OperationLog::getOperationTime, request.getEndTime())
                .orderByDesc(OperationLog::getOperationTime)
                .orderByDesc(OperationLog::getId);
        Page<OperationLog> result = page(page, wrapper);
        List<OperationLogVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public OperationLogVO getDetail(Long id) {
        OperationLog entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Operation log does not exist");
        }
        return toVO(entity);
    }

    @Override
    public void recordSuccess(String module, String type, String description, String params) {
        saveLog(module, type, description, params, RESULT_SUCCESS, null);
    }

    @Override
    public void recordFailure(String module, String type, String description, String params, String errorMessage) {
        saveLog(module, type, description, params, RESULT_FAIL, errorMessage);
    }

    private void saveLog(String module, String type, String description, String params, String result, String errorMessage) {
        LocalDateTime now = LocalDateTime.now();
        OperationLog entity = new OperationLog();
        LoginUserContext context = CurrentUserUtils.getContext();
        if (context != null) {
            entity.setUserId(context.getUserId());
            entity.setUsername(context.getUsername());
            entity.setRoleCode(context.getRoleCode());
        }
        entity.setOperationModule(LogContextUtils.limit(module, 100));
        entity.setOperationType(LogContextUtils.limit(type, 50));
        entity.setOperationDesc(LogContextUtils.limit(description, 500));
        entity.setRequestMethod(LogContextUtils.limit(LogContextUtils.requestMethod(), 20));
        entity.setRequestUri(LogContextUtils.limit(LogContextUtils.requestUri(), 255));
        entity.setRequestParams(LogContextUtils.sanitize(params));
        entity.setIpAddress(LogContextUtils.ipAddress());
        entity.setUserAgent(LogContextUtils.userAgent());
        entity.setResult(result);
        entity.setErrorMessage(LogContextUtils.sanitize(errorMessage));
        entity.setOperationTime(now);
        entity.setCreateTime(now);
        save(entity);
    }

    private OperationLogVO toVO(OperationLog entity) {
        OperationLogVO vo = new OperationLogVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setUsername(entity.getUsername());
        vo.setRealName(entity.getRealName());
        vo.setRoleCode(entity.getRoleCode());
        vo.setOperationType(entity.getOperationType());
        vo.setOperationModule(entity.getOperationModule());
        vo.setOperationDesc(entity.getOperationDesc());
        vo.setRequestMethod(entity.getRequestMethod());
        vo.setRequestUri(entity.getRequestUri());
        vo.setRequestParams(entity.getRequestParams());
        vo.setIpAddress(entity.getIpAddress());
        vo.setUserAgent(entity.getUserAgent());
        vo.setResult(entity.getResult());
        vo.setErrorMessage(entity.getErrorMessage());
        vo.setOperationTime(entity.getOperationTime());
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
