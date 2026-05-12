package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.common.ResultCode;
import com.hjc.backend.dto.CreateSystemConfigRequest;
import com.hjc.backend.dto.UpdateSystemConfigRequest;
import com.hjc.backend.entity.SystemConfig;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.mapper.SystemConfigMapper;
import com.hjc.backend.service.SystemConfigService;
import com.hjc.backend.vo.SystemConfigVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    @Override
    public PageResult<SystemConfigVO> pageQuery(Long pageNum, Long pageSize, String keyword, Integer status) {
        Page<SystemConfig> page = new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize));
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, SystemConfig::getStatus, status)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(SystemConfig::getConfigKey, keyword)
                        .or()
                        .like(SystemConfig::getDescription, keyword))
                .orderByDesc(SystemConfig::getId);
        Page<SystemConfig> result = page(page, wrapper);
        List<SystemConfigVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public SystemConfigVO getDetail(Long id) {
        return toVO(getExisting(id));
    }

    @Override
    public SystemConfigVO create(CreateSystemConfigRequest request) {
        checkConfigKeyUnique(request.getConfigKey(), null);
        SystemConfig entity = new SystemConfig();
        entity.setConfigKey(request.getConfigKey());
        entity.setConfigValue(request.getConfigValue());
        entity.setDescription(request.getDescription());
        entity.setStatus(defaultStatus(request.getStatus()));
        save(entity);
        return getDetail(entity.getId());
    }

    @Override
    public SystemConfigVO update(Long id, UpdateSystemConfigRequest request) {
        getExisting(id);
        checkConfigKeyUnique(request.getConfigKey(), id);
        SystemConfig entity = new SystemConfig();
        entity.setId(id);
        entity.setConfigKey(request.getConfigKey());
        entity.setConfigValue(request.getConfigValue());
        entity.setDescription(request.getDescription());
        entity.setStatus(request.getStatus());
        updateById(entity);
        return getDetail(id);
    }

    @Override
    public void deleteById(Long id) {
        getExisting(id);
        removeById(id);
    }

    private SystemConfig getExisting(Long id) {
        SystemConfig entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "系统配置不存在");
        }
        return entity;
    }

    private void checkConfigKeyUnique(String configKey, Long excludeId) {
        long count = lambdaQuery()
                .eq(SystemConfig::getConfigKey, configKey)
                .ne(excludeId != null, SystemConfig::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BusinessException("配置键已存在");
        }
    }

    private SystemConfigVO toVO(SystemConfig entity) {
        SystemConfigVO vo = new SystemConfigVO();
        vo.setId(entity.getId());
        vo.setConfigKey(entity.getConfigKey());
        vo.setConfigValue(entity.getConfigValue());
        vo.setDescription(entity.getDescription());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
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

    private Integer defaultStatus(Integer status) {
        return status == null ? 1 : status;
    }
}
