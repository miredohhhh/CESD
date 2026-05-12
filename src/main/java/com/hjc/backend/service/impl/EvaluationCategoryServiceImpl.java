package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.common.ResultCode;
import com.hjc.backend.dto.CreateEvaluationCategoryRequest;
import com.hjc.backend.dto.UpdateEvaluationCategoryRequest;
import com.hjc.backend.entity.EvaluationCategory;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.mapper.EvaluationCategoryMapper;
import com.hjc.backend.service.EvaluationCategoryService;
import com.hjc.backend.vo.EvaluationCategoryVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class EvaluationCategoryServiceImpl extends ServiceImpl<EvaluationCategoryMapper, EvaluationCategory> implements EvaluationCategoryService {

    @Override
    public PageResult<EvaluationCategoryVO> pageQuery(Long pageNum, Long pageSize, String keyword, Integer status) {
        Page<EvaluationCategory> page = new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize));
        LambdaQueryWrapper<EvaluationCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, EvaluationCategory::getStatus, status)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(EvaluationCategory::getCategoryName, keyword)
                        .or()
                        .like(EvaluationCategory::getCategoryCode, keyword))
                .orderByAsc(EvaluationCategory::getSortNo)
                .orderByDesc(EvaluationCategory::getId);
        Page<EvaluationCategory> result = page(page, wrapper);
        List<EvaluationCategoryVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public EvaluationCategoryVO getDetail(Long id) {
        return toVO(getExisting(id));
    }

    @Override
    public EvaluationCategoryVO create(CreateEvaluationCategoryRequest request) {
        checkCategoryCodeUnique(request.getCategoryCode(), null);
        EvaluationCategory entity = new EvaluationCategory();
        entity.setCategoryName(request.getCategoryName());
        entity.setCategoryCode(request.getCategoryCode());
        entity.setMaxScore(request.getMaxScore());
        entity.setSortNo(defaultSortNo(request.getSortNo()));
        entity.setDescription(request.getDescription());
        entity.setStatus(defaultStatus(request.getStatus()));
        save(entity);
        return getDetail(entity.getId());
    }

    @Override
    public EvaluationCategoryVO update(Long id, UpdateEvaluationCategoryRequest request) {
        getExisting(id);
        checkCategoryCodeUnique(request.getCategoryCode(), id);
        EvaluationCategory entity = new EvaluationCategory();
        entity.setId(id);
        entity.setCategoryName(request.getCategoryName());
        entity.setCategoryCode(request.getCategoryCode());
        entity.setMaxScore(request.getMaxScore());
        entity.setSortNo(request.getSortNo());
        entity.setDescription(request.getDescription());
        entity.setStatus(request.getStatus());
        updateById(entity);
        return getDetail(id);
    }

    @Override
    public void deleteById(Long id) {
        getExisting(id);
        // TODO Check whether evaluation items exist before deleting a category.
        removeById(id);
    }

    private EvaluationCategory getExisting(Long id) {
        EvaluationCategory entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "综测分类不存在");
        }
        return entity;
    }

    private void checkCategoryCodeUnique(String categoryCode, Long excludeId) {
        if (!StringUtils.hasText(categoryCode)) {
            return;
        }
        long count = lambdaQuery()
                .eq(EvaluationCategory::getCategoryCode, categoryCode)
                .ne(excludeId != null, EvaluationCategory::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BusinessException("综测分类编码已存在");
        }
    }

    private EvaluationCategoryVO toVO(EvaluationCategory entity) {
        EvaluationCategoryVO vo = new EvaluationCategoryVO();
        vo.setId(entity.getId());
        vo.setCategoryName(entity.getCategoryName());
        vo.setCategoryCode(entity.getCategoryCode());
        vo.setMaxScore(entity.getMaxScore());
        vo.setSortNo(entity.getSortNo());
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

    private Integer defaultSortNo(Integer sortNo) {
        return sortNo == null ? 0 : sortNo;
    }
}
