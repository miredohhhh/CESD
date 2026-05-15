package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.common.ResultCode;
import com.hjc.backend.dto.CreateReviewRecordRequest;
import com.hjc.backend.dto.ReviewRecordPageRequest;
import com.hjc.backend.entity.ReviewRecord;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.mapper.ReviewRecordMapper;
import com.hjc.backend.service.MaterialApplicationService;
import com.hjc.backend.service.ReviewRecordService;
import com.hjc.backend.service.SysUserService;
import com.hjc.backend.vo.ReviewRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReviewRecordServiceImpl extends ServiceImpl<ReviewRecordMapper, ReviewRecord> implements ReviewRecordService {

    private static final Set<String> MATERIAL_STATUSES = Set.of("DRAFT", "SUBMITTED", "APPROVED", "REJECTED", "CANCELLED");

    private static final Set<String> REVIEW_RESULTS = Set.of("APPROVED", "REJECTED");

    private final MaterialApplicationService materialApplicationService;

    private final SysUserService sysUserService;

    @Override
    public PageResult<ReviewRecordVO> pageReviewRecords(ReviewRecordPageRequest request) {
        if (StringUtils.hasText(request.getReviewResult())) {
            checkReviewResult(request.getReviewResult());
        }
        Page<ReviewRecord> page = new Page<>(normalizePageNum(request.getPageNum()), normalizePageSize(request.getPageSize()));
        LambdaQueryWrapper<ReviewRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(request.getMaterialId() != null, ReviewRecord::getMaterialId, request.getMaterialId())
                .eq(request.getReviewerId() != null, ReviewRecord::getReviewerId, request.getReviewerId())
                .eq(StringUtils.hasText(request.getReviewResult()), ReviewRecord::getReviewResult, request.getReviewResult())
                .ge(request.getStartReviewTime() != null, ReviewRecord::getReviewTime, request.getStartReviewTime())
                .le(request.getEndReviewTime() != null, ReviewRecord::getReviewTime, request.getEndReviewTime())
                .orderByDesc(ReviewRecord::getReviewTime)
                .orderByDesc(ReviewRecord::getId);
        Page<ReviewRecord> result = page(page, wrapper);
        List<ReviewRecordVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public ReviewRecordVO getReviewRecordDetail(Long id) {
        return toVO(getExisting(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewRecordVO createReviewRecord(CreateReviewRecordRequest request) {
        checkMaterialExists(request.getMaterialId());
        checkReviewerExists(request.getReviewerId());
        checkMaterialStatus(request.getBeforeStatus(), "beforeStatus");
        checkMaterialStatus(request.getAfterStatus(), "afterStatus");
        checkReviewResult(request.getReviewResult());
        checkReviewScore(request.getReviewScore());

        LocalDateTime now = LocalDateTime.now();
        ReviewRecord entity = new ReviewRecord();
        entity.setMaterialId(request.getMaterialId());
        entity.setReviewerId(request.getReviewerId());
        entity.setBeforeStatus(request.getBeforeStatus());
        entity.setAfterStatus(request.getAfterStatus());
        entity.setReviewResult(request.getReviewResult());
        entity.setReviewScore(request.getReviewScore());
        entity.setReviewComment(request.getReviewComment());
        entity.setReviewTime(request.getReviewTime() == null ? now : request.getReviewTime());
        entity.setCreateTime(now);
        save(entity);
        return getReviewRecordDetail(entity.getId());
    }

    @Override
    public List<ReviewRecordVO> listByMaterialId(Long materialId) {
        checkMaterialExists(materialId);
        return lambdaQuery()
                .eq(ReviewRecord::getMaterialId, materialId)
                .orderByDesc(ReviewRecord::getReviewTime)
                .orderByDesc(ReviewRecord::getId)
                .list()
                .stream()
                .map(this::toVO)
                .toList();
    }

    private ReviewRecord getExisting(Long id) {
        if (id == null) {
            throw new BusinessException("Review record id must not be null");
        }
        ReviewRecord entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Review record does not exist");
        }
        return entity;
    }

    private void checkMaterialExists(Long materialId) {
        if (materialId == null) {
            throw new BusinessException("materialId must not be null");
        }
        if (materialApplicationService.getById(materialId) == null) {
            throw new BusinessException("Material application does not exist or has been deleted");
        }
    }

    private void checkReviewerExists(Long reviewerId) {
        if (reviewerId == null) {
            throw new BusinessException("reviewerId must not be null");
        }
        if (sysUserService.getById(reviewerId) == null) {
            throw new BusinessException("Reviewer user does not exist or has been deleted");
        }
    }

    private void checkMaterialStatus(String status, String fieldName) {
        if (!StringUtils.hasText(status) || !MATERIAL_STATUSES.contains(status)) {
            throw new BusinessException(fieldName + " must be DRAFT, SUBMITTED, APPROVED, REJECTED, or CANCELLED");
        }
    }

    private void checkReviewResult(String reviewResult) {
        if (!StringUtils.hasText(reviewResult) || !REVIEW_RESULTS.contains(reviewResult)) {
            throw new BusinessException("reviewResult must be APPROVED or REJECTED");
        }
    }

    private void checkReviewScore(BigDecimal reviewScore) {
        if (reviewScore != null && reviewScore.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("reviewScore must be greater than or equal to 0");
        }
    }

    private ReviewRecordVO toVO(ReviewRecord entity) {
        ReviewRecordVO vo = new ReviewRecordVO();
        vo.setId(entity.getId());
        vo.setMaterialId(entity.getMaterialId());
        vo.setReviewerId(entity.getReviewerId());
        vo.setBeforeStatus(entity.getBeforeStatus());
        vo.setAfterStatus(entity.getAfterStatus());
        vo.setReviewResult(entity.getReviewResult());
        vo.setReviewScore(entity.getReviewScore());
        vo.setReviewComment(entity.getReviewComment());
        vo.setReviewTime(entity.getReviewTime());
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
