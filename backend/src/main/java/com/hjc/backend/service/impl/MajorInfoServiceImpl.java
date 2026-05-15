package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.common.ResultCode;
import com.hjc.backend.dto.CreateMajorInfoRequest;
import com.hjc.backend.dto.UpdateMajorInfoRequest;
import com.hjc.backend.entity.MajorInfo;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.mapper.MajorInfoMapper;
import com.hjc.backend.service.MajorInfoService;
import com.hjc.backend.vo.MajorInfoVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MajorInfoServiceImpl extends ServiceImpl<MajorInfoMapper, MajorInfo> implements MajorInfoService {

    @Override
    public PageResult<MajorInfoVO> pageQuery(Long pageNum, Long pageSize, String keyword, Integer status) {
        Page<MajorInfo> page = new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize));
        LambdaQueryWrapper<MajorInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, MajorInfo::getStatus, status)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(MajorInfo::getMajorName, keyword)
                        .or()
                        .like(MajorInfo::getMajorCode, keyword)
                        .or()
                        .like(MajorInfo::getCollegeName, keyword))
                .orderByDesc(MajorInfo::getId);
        Page<MajorInfo> result = page(page, wrapper);
        List<MajorInfoVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public MajorInfoVO getDetail(Long id) {
        return toVO(getExisting(id));
    }

    @Override
    public MajorInfoVO create(CreateMajorInfoRequest request) {
        checkMajorCodeUnique(request.getMajorCode(), null);
        MajorInfo entity = new MajorInfo();
        entity.setMajorName(request.getMajorName());
        entity.setMajorCode(request.getMajorCode());
        entity.setCollegeName(request.getCollegeName());
        entity.setDescription(request.getDescription());
        entity.setStatus(defaultStatus(request.getStatus()));
        save(entity);
        return getDetail(entity.getId());
    }

    @Override
    public MajorInfoVO update(Long id, UpdateMajorInfoRequest request) {
        getExisting(id);
        checkMajorCodeUnique(request.getMajorCode(), id);
        MajorInfo entity = new MajorInfo();
        entity.setId(id);
        entity.setMajorName(request.getMajorName());
        entity.setMajorCode(request.getMajorCode());
        entity.setCollegeName(request.getCollegeName());
        entity.setDescription(request.getDescription());
        entity.setStatus(request.getStatus());
        updateById(entity);
        return getDetail(id);
    }

    @Override
    public void deleteById(Long id) {
        getExisting(id);
        // TODO Check whether classes or students exist before deleting a major.
        removeById(id);
    }

    private MajorInfo getExisting(Long id) {
        MajorInfo entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "专业不存在");
        }
        return entity;
    }

    private void checkMajorCodeUnique(String majorCode, Long excludeId) {
        if (!StringUtils.hasText(majorCode)) {
            return;
        }
        long count = lambdaQuery()
                .eq(MajorInfo::getMajorCode, majorCode)
                .ne(excludeId != null, MajorInfo::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BusinessException("专业编码已存在");
        }
    }

    private MajorInfoVO toVO(MajorInfo entity) {
        MajorInfoVO vo = new MajorInfoVO();
        vo.setId(entity.getId());
        vo.setMajorName(entity.getMajorName());
        vo.setMajorCode(entity.getMajorCode());
        vo.setCollegeName(entity.getCollegeName());
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
