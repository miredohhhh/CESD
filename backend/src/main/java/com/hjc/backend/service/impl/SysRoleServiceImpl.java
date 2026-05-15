package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.common.ResultCode;
import com.hjc.backend.dto.CreateSysRoleRequest;
import com.hjc.backend.dto.UpdateSysRoleRequest;
import com.hjc.backend.entity.SysRole;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.mapper.SysRoleMapper;
import com.hjc.backend.service.SysRoleService;
import com.hjc.backend.vo.SysRoleVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Override
    public PageResult<SysRoleVO> pageQuery(Long pageNum, Long pageSize, String keyword, Integer status) {
        Page<SysRole> page = new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize));
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, SysRole::getStatus, status)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(SysRole::getRoleName, keyword)
                        .or()
                        .like(SysRole::getRoleCode, keyword))
                .orderByDesc(SysRole::getId);
        Page<SysRole> result = page(page, wrapper);
        List<SysRoleVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public SysRoleVO getDetail(Long id) {
        return toVO(getExisting(id));
    }

    @Override
    public SysRoleVO create(CreateSysRoleRequest request) {
        checkRoleCodeUnique(request.getRoleCode(), null);
        SysRole entity = new SysRole();
        entity.setRoleName(request.getRoleName());
        entity.setRoleCode(request.getRoleCode());
        entity.setDescription(request.getDescription());
        entity.setStatus(defaultStatus(request.getStatus()));
        save(entity);
        return getDetail(entity.getId());
    }

    @Override
    public SysRoleVO update(Long id, UpdateSysRoleRequest request) {
        getExisting(id);
        checkRoleCodeUnique(request.getRoleCode(), id);
        SysRole entity = new SysRole();
        entity.setId(id);
        entity.setRoleName(request.getRoleName());
        entity.setRoleCode(request.getRoleCode());
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

    private SysRole getExisting(Long id) {
        SysRole entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "角色不存在");
        }
        return entity;
    }

    private void checkRoleCodeUnique(String roleCode, Long excludeId) {
        long count = lambdaQuery()
                .eq(SysRole::getRoleCode, roleCode)
                .ne(excludeId != null, SysRole::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BusinessException("角色编码已存在");
        }
    }

    private SysRoleVO toVO(SysRole entity) {
        SysRoleVO vo = new SysRoleVO();
        vo.setId(entity.getId());
        vo.setRoleName(entity.getRoleName());
        vo.setRoleCode(entity.getRoleCode());
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
