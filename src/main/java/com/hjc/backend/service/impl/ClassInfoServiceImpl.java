package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.common.ResultCode;
import com.hjc.backend.dto.CreateClassInfoRequest;
import com.hjc.backend.dto.UpdateClassInfoRequest;
import com.hjc.backend.entity.ClassInfo;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.mapper.ClassInfoMapper;
import com.hjc.backend.service.ClassInfoService;
import com.hjc.backend.service.MajorInfoService;
import com.hjc.backend.vo.ClassInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassInfoServiceImpl extends ServiceImpl<ClassInfoMapper, ClassInfo> implements ClassInfoService {

    private final MajorInfoService majorInfoService;

    @Override
    public PageResult<ClassInfoVO> pageQuery(Long pageNum, Long pageSize, String keyword, Integer status) {
        Page<ClassInfo> page = new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize));
        LambdaQueryWrapper<ClassInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, ClassInfo::getStatus, status)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(ClassInfo::getClassName, keyword)
                        .or()
                        .like(ClassInfo::getClassCode, keyword)
                        .or()
                        .like(ClassInfo::getGrade, keyword)
                        .or()
                        .like(ClassInfo::getCounselorName, keyword))
                .orderByDesc(ClassInfo::getId);
        Page<ClassInfo> result = page(page, wrapper);
        List<ClassInfoVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public ClassInfoVO getDetail(Long id) {
        return toVO(getExisting(id));
    }

    @Override
    public ClassInfoVO create(CreateClassInfoRequest request) {
        checkMajorExists(request.getMajorId());
        checkClassCodeUnique(request.getClassCode(), null);
        ClassInfo entity = new ClassInfo();
        entity.setClassName(request.getClassName());
        entity.setClassCode(request.getClassCode());
        entity.setMajorId(request.getMajorId());
        entity.setGrade(request.getGrade());
        entity.setCounselorName(request.getCounselorName());
        entity.setStatus(defaultStatus(request.getStatus()));
        save(entity);
        return getDetail(entity.getId());
    }

    @Override
    public ClassInfoVO update(Long id, UpdateClassInfoRequest request) {
        getExisting(id);
        checkMajorExists(request.getMajorId());
        checkClassCodeUnique(request.getClassCode(), id);
        ClassInfo entity = new ClassInfo();
        entity.setId(id);
        entity.setClassName(request.getClassName());
        entity.setClassCode(request.getClassCode());
        entity.setMajorId(request.getMajorId());
        entity.setGrade(request.getGrade());
        entity.setCounselorName(request.getCounselorName());
        entity.setStatus(request.getStatus());
        updateById(entity);
        return getDetail(id);
    }

    @Override
    public void deleteById(Long id) {
        getExisting(id);
        // TODO Check whether students exist before deleting a class.
        removeById(id);
    }

    private ClassInfo getExisting(Long id) {
        ClassInfo entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "班级不存在");
        }
        return entity;
    }

    private void checkMajorExists(Long majorId) {
        if (majorInfoService.getById(majorId) == null) {
            throw new BusinessException("所属专业不存在或已删除");
        }
    }

    private void checkClassCodeUnique(String classCode, Long excludeId) {
        if (!StringUtils.hasText(classCode)) {
            return;
        }
        long count = lambdaQuery()
                .eq(ClassInfo::getClassCode, classCode)
                .ne(excludeId != null, ClassInfo::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BusinessException("班级编码已存在");
        }
    }

    private ClassInfoVO toVO(ClassInfo entity) {
        ClassInfoVO vo = new ClassInfoVO();
        vo.setId(entity.getId());
        vo.setClassName(entity.getClassName());
        vo.setClassCode(entity.getClassCode());
        vo.setMajorId(entity.getMajorId());
        vo.setGrade(entity.getGrade());
        vo.setCounselorName(entity.getCounselorName());
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
