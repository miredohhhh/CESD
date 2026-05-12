package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.common.ResultCode;
import com.hjc.backend.dto.CreateEvaluationItemRequest;
import com.hjc.backend.dto.UpdateEvaluationItemRequest;
import com.hjc.backend.entity.EvaluationItem;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.mapper.EvaluationItemMapper;
import com.hjc.backend.service.EvaluationCategoryService;
import com.hjc.backend.service.EvaluationItemService;
import com.hjc.backend.vo.EvaluationItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EvaluationItemServiceImpl extends ServiceImpl<EvaluationItemMapper, EvaluationItem> implements EvaluationItemService {

    private static final Set<String> SCORE_TYPES = Set.of("FIXED", "MANUAL", "RANGE");

    private final EvaluationCategoryService evaluationCategoryService;

    @Override
    public PageResult<EvaluationItemVO> pageQuery(Long pageNum, Long pageSize, String keyword, Integer status) {
        Page<EvaluationItem> page = new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize));
        LambdaQueryWrapper<EvaluationItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, EvaluationItem::getStatus, status)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(EvaluationItem::getItemName, keyword)
                        .or()
                        .like(EvaluationItem::getItemCode, keyword)
                        .or()
                        .like(EvaluationItem::getScoreType, keyword))
                .orderByAsc(EvaluationItem::getSortNo)
                .orderByDesc(EvaluationItem::getId);
        Page<EvaluationItem> result = page(page, wrapper);
        List<EvaluationItemVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public EvaluationItemVO getDetail(Long id) {
        return toVO(getExisting(id));
    }

    @Override
    public EvaluationItemVO create(CreateEvaluationItemRequest request) {
        checkCategoryExists(request.getCategoryId());
        checkItemCodeUnique(request.getItemCode(), null);
        checkScoreType(request.getScoreType());
        checkScoreRange(request.getScore(), request.getMaxScore());
        EvaluationItem entity = new EvaluationItem();
        entity.setCategoryId(request.getCategoryId());
        entity.setItemName(request.getItemName());
        entity.setItemCode(request.getItemCode());
        entity.setScoreType(request.getScoreType());
        entity.setScore(request.getScore());
        entity.setMaxScore(request.getMaxScore());
        entity.setNeedAttachment(defaultNeedAttachment(request.getNeedAttachment()));
        entity.setDescription(request.getDescription());
        entity.setSortNo(defaultSortNo(request.getSortNo()));
        entity.setStatus(defaultStatus(request.getStatus()));
        save(entity);
        return getDetail(entity.getId());
    }

    @Override
    public EvaluationItemVO update(Long id, UpdateEvaluationItemRequest request) {
        getExisting(id);
        checkCategoryExists(request.getCategoryId());
        checkItemCodeUnique(request.getItemCode(), id);
        checkScoreType(request.getScoreType());
        checkScoreRange(request.getScore(), request.getMaxScore());
        EvaluationItem entity = new EvaluationItem();
        entity.setId(id);
        entity.setCategoryId(request.getCategoryId());
        entity.setItemName(request.getItemName());
        entity.setItemCode(request.getItemCode());
        entity.setScoreType(request.getScoreType());
        entity.setScore(request.getScore());
        entity.setMaxScore(request.getMaxScore());
        entity.setNeedAttachment(request.getNeedAttachment());
        entity.setDescription(request.getDescription());
        entity.setSortNo(request.getSortNo());
        entity.setStatus(request.getStatus());
        updateById(entity);
        return getDetail(id);
    }

    @Override
    public void deleteById(Long id) {
        getExisting(id);
        // TODO Check whether material applications exist before deleting an evaluation item.
        removeById(id);
    }

    private EvaluationItem getExisting(Long id) {
        EvaluationItem entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "综测项目不存在");
        }
        return entity;
    }

    private void checkCategoryExists(Long categoryId) {
        if (evaluationCategoryService.getById(categoryId) == null) {
            throw new BusinessException("所属综测分类不存在或已删除");
        }
    }

    private void checkItemCodeUnique(String itemCode, Long excludeId) {
        if (!StringUtils.hasText(itemCode)) {
            return;
        }
        long count = lambdaQuery()
                .eq(EvaluationItem::getItemCode, itemCode)
                .ne(excludeId != null, EvaluationItem::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BusinessException("综测项目编码已存在");
        }
    }

    private void checkScoreType(String scoreType) {
        if (!SCORE_TYPES.contains(scoreType)) {
            throw new BusinessException("计分方式只允许 FIXED、MANUAL、RANGE");
        }
    }

    private void checkScoreRange(BigDecimal score, BigDecimal maxScore) {
        if (score != null && maxScore != null && score.compareTo(maxScore) > 0) {
            throw new BusinessException("默认分值不能大于项目最高分");
        }
    }

    private EvaluationItemVO toVO(EvaluationItem entity) {
        EvaluationItemVO vo = new EvaluationItemVO();
        vo.setId(entity.getId());
        vo.setCategoryId(entity.getCategoryId());
        vo.setItemName(entity.getItemName());
        vo.setItemCode(entity.getItemCode());
        vo.setScoreType(entity.getScoreType());
        vo.setScore(entity.getScore());
        vo.setMaxScore(entity.getMaxScore());
        vo.setNeedAttachment(entity.getNeedAttachment());
        vo.setDescription(entity.getDescription());
        vo.setSortNo(entity.getSortNo());
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

    private Integer defaultNeedAttachment(Integer needAttachment) {
        return needAttachment == null ? 1 : needAttachment;
    }
}
