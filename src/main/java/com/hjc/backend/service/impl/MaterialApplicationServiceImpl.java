package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.common.ResultCode;
import com.hjc.backend.dto.ApproveMaterialApplicationRequest;
import com.hjc.backend.dto.CreateReviewRecordRequest;
import com.hjc.backend.dto.CreateMaterialApplicationRequest;
import com.hjc.backend.dto.MyMaterialApplicationPageRequest;
import com.hjc.backend.dto.PendingMaterialApplicationPageRequest;
import com.hjc.backend.dto.RejectMaterialApplicationRequest;
import com.hjc.backend.dto.UpdateMaterialApplicationRequest;
import com.hjc.backend.dto.WithdrawMaterialApplicationRequest;
import com.hjc.backend.entity.ClassInfo;
import com.hjc.backend.entity.EvaluationCategory;
import com.hjc.backend.entity.EvaluationItem;
import com.hjc.backend.entity.MajorInfo;
import com.hjc.backend.entity.MaterialApplication;
import com.hjc.backend.entity.MaterialAttachment;
import com.hjc.backend.entity.Student;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.mapper.MaterialApplicationMapper;
import com.hjc.backend.service.ClassInfoService;
import com.hjc.backend.service.EvaluationCategoryService;
import com.hjc.backend.service.EvaluationItemService;
import com.hjc.backend.service.MajorInfoService;
import com.hjc.backend.service.MaterialApplicationService;
import com.hjc.backend.service.MaterialAttachmentService;
import com.hjc.backend.service.ReviewRecordService;
import com.hjc.backend.service.StudentService;
import com.hjc.backend.service.SysUserService;
import com.hjc.backend.vo.MaterialApplicationDetailVO;
import com.hjc.backend.vo.MaterialApplicationVO;
import com.hjc.backend.vo.MaterialAttachmentVO;
import com.hjc.backend.vo.MyApplicationStatisticsVO;
import com.hjc.backend.vo.MyMaterialApplicationVO;
import com.hjc.backend.vo.PendingMaterialApplicationVO;
import com.hjc.backend.vo.ReviewRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialApplicationServiceImpl extends ServiceImpl<MaterialApplicationMapper, MaterialApplication> implements MaterialApplicationService {

    private static final String DEFAULT_STATUS = "DRAFT";

    private static final String STATUS_DRAFT = "DRAFT";

    private static final String STATUS_SUBMITTED = "SUBMITTED";

    private static final String STATUS_APPROVED = "APPROVED";

    private static final String STATUS_REJECTED = "REJECTED";

    private static final String STATUS_CANCELLED = "CANCELLED";

    private static final Set<String> STATUSES = Set.of("DRAFT", "SUBMITTED", "APPROVED", "REJECTED", "CANCELLED");

    private final StudentService studentService;

    private final EvaluationItemService evaluationItemService;

    private final EvaluationCategoryService evaluationCategoryService;

    private final ClassInfoService classInfoService;

    private final MajorInfoService majorInfoService;

    private final SysUserService sysUserService;

    private final ObjectProvider<MaterialAttachmentService> materialAttachmentServiceProvider;

    private final ObjectProvider<ReviewRecordService> reviewRecordServiceProvider;

    @Override
    public PageResult<MaterialApplicationVO> pageQuery(Long pageNum, Long pageSize, String keyword, String status, Long studentId, Long itemId) {
        if (StringUtils.hasText(status)) {
            checkStatus(status);
        }
        Page<MaterialApplication> page = new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize));
        LambdaQueryWrapper<MaterialApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(status), MaterialApplication::getStatus, status)
                .eq(studentId != null, MaterialApplication::getStudentId, studentId)
                .eq(itemId != null, MaterialApplication::getItemId, itemId)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(MaterialApplication::getTitle, keyword)
                        .or()
                        .like(MaterialApplication::getDescription, keyword)
                        .or()
                        .like(MaterialApplication::getRejectReason, keyword))
                .orderByDesc(MaterialApplication::getId);
        Page<MaterialApplication> result = page(page, wrapper);
        List<MaterialApplicationVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public MaterialApplicationVO getDetail(Long id) {
        return toVO(getExisting(id));
    }

    @Override
    public MaterialApplicationVO create(CreateMaterialApplicationRequest request) {
        checkStudentExists(request.getStudentId());
        checkItemExists(request.getItemId());
        String status = StringUtils.hasText(request.getStatus()) ? request.getStatus() : DEFAULT_STATUS;
        checkStatus(status);
        MaterialApplication entity = new MaterialApplication();
        entity.setStudentId(request.getStudentId());
        entity.setItemId(request.getItemId());
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setApplyScore(request.getApplyScore());
        entity.setFinalScore(request.getFinalScore());
        entity.setStatus(status);
        entity.setRejectReason(request.getRejectReason());
        entity.setSubmitCount(defaultSubmitCount(request.getSubmitCount()));
        save(entity);
        return getDetail(entity.getId());
    }

    @Override
    public MaterialApplicationVO update(Long id, UpdateMaterialApplicationRequest request) {
        MaterialApplication entity = getExisting(id);
        Long finalStudentId = request.getStudentId() == null ? entity.getStudentId() : request.getStudentId();
        Long finalItemId = request.getItemId() == null ? entity.getItemId() : request.getItemId();
        checkStudentExists(finalStudentId);
        checkItemExists(finalItemId);
        entity.setStudentId(finalStudentId);
        entity.setItemId(finalItemId);
        if (StringUtils.hasText(request.getTitle())) {
            entity.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getApplyScore() != null) {
            entity.setApplyScore(request.getApplyScore());
        }
        if (request.getFinalScore() != null) {
            entity.setFinalScore(request.getFinalScore());
        }
        if (StringUtils.hasText(request.getStatus())) {
            checkStatus(request.getStatus());
            entity.setStatus(request.getStatus());
        }
        if (request.getRejectReason() != null) {
            entity.setRejectReason(request.getRejectReason());
        }
        if (request.getSubmitCount() != null) {
            entity.setSubmitCount(request.getSubmitCount());
        }
        updateById(entity);
        return getDetail(id);
    }

    @Override
    public void deleteById(Long id) {
        getExisting(id);
        // TODO Check material attachments and review records before deleting a material application.
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialApplicationVO submitMaterialApplication(Long id) {
        MaterialApplication entity = getExisting(id);
        if (!Set.of(STATUS_DRAFT, STATUS_REJECTED, STATUS_CANCELLED).contains(entity.getStatus())) {
            throw new BusinessException("Current status does not allow submit");
        }
        LocalDateTime now = LocalDateTime.now();
        entity.setStatus(STATUS_SUBMITTED);
        entity.setSubmitTime(now);
        entity.setSubmitCount(defaultSubmitCount(entity.getSubmitCount()) + 1);
        entity.setRejectReason(null);
        entity.setUpdateTime(now);
        // TODO For high concurrency, use conditional update by id and status or optimistic locking.
        updateById(entity);
        return getDetail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialApplicationVO withdrawMaterialApplication(Long id, WithdrawMaterialApplicationRequest request) {
        MaterialApplication entity = getExisting(id);
        if (!STATUS_SUBMITTED.equals(entity.getStatus())) {
            throw new BusinessException("Current status does not allow withdraw");
        }
        LocalDateTime now = LocalDateTime.now();
        entity.setStatus(STATUS_CANCELLED);
        entity.setUpdateTime(now);
        // TODO Withdraw reason is accepted for future extension but is not persisted in the current table.
        // TODO For high concurrency, use conditional update by id and status or optimistic locking.
        updateById(entity);
        return getDetail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialApplicationVO approveMaterialApplication(Long id, ApproveMaterialApplicationRequest request) {
        if (request == null) {
            throw new BusinessException("Approve request must not be null");
        }
        checkReviewerExists(request.getReviewerId());
        checkReviewScoreRequired(request.getReviewScore());
        checkReviewCommentLength(request.getReviewComment());

        MaterialApplication entity = getExisting(id);
        String beforeStatus = entity.getStatus();
        if (!STATUS_SUBMITTED.equals(beforeStatus)) {
            throw new BusinessException("Current status does not allow approve");
        }

        LocalDateTime now = LocalDateTime.now();
        entity.setStatus(STATUS_APPROVED);
        entity.setFinalScore(request.getReviewScore());
        entity.setRejectReason(null);
        entity.setReviewTime(now);
        entity.setUpdateTime(now);
        // TODO For high concurrency, use conditional update by id and status or optimistic locking.
        updateById(entity);

        createReviewRecord(id, request.getReviewerId(), beforeStatus, STATUS_APPROVED, STATUS_APPROVED, request.getReviewScore(), request.getReviewComment(), now);
        // TODO After authentication and score modules mature, trigger ScoreSummaryService.recalculateStudentScore(studentId) here or asynchronously.
        return getDetail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialApplicationVO rejectMaterialApplication(Long id, RejectMaterialApplicationRequest request) {
        if (request == null) {
            throw new BusinessException("Reject request must not be null");
        }
        checkReviewerExists(request.getReviewerId());
        if (!StringUtils.hasText(request.getRejectReason())) {
            throw new BusinessException("Reject reason must not be blank");
        }
        checkTextLength(request.getRejectReason(), "rejectReason");
        checkReviewCommentLength(request.getReviewComment());

        MaterialApplication entity = getExisting(id);
        String beforeStatus = entity.getStatus();
        if (!STATUS_SUBMITTED.equals(beforeStatus)) {
            throw new BusinessException("Current status does not allow reject");
        }

        LocalDateTime now = LocalDateTime.now();
        entity.setStatus(STATUS_REJECTED);
        entity.setFinalScore(null);
        entity.setRejectReason(request.getRejectReason());
        entity.setReviewTime(now);
        entity.setUpdateTime(now);
        // TODO For high concurrency, use conditional update by id and status or optimistic locking.
        updateById(entity);

        String reviewComment = StringUtils.hasText(request.getReviewComment()) ? request.getReviewComment() : request.getRejectReason();
        createReviewRecord(id, request.getReviewerId(), beforeStatus, STATUS_REJECTED, STATUS_REJECTED, null, reviewComment, now);
        return getDetail(id);
    }

    @Override
    public PageResult<MyMaterialApplicationVO> pageMyApplications(MyMaterialApplicationPageRequest request) {
        // TODO After authentication is introduced, studentId must come from current user context instead of request parameters.
        checkStudentExists(request.getStudentId());
        if (StringUtils.hasText(request.getStatus())) {
            checkStatus(request.getStatus());
        }
        List<Long> categoryItemIds = listItemIdsByCategory(request.getCategoryId());
        if (categoryItemIds != null && categoryItemIds.isEmpty()) {
            return PageResult.of(List.of(), 0L, resolvePageNum(request.getPageNo(), request.getPageNum()), normalizePageSize(request.getPageSize()));
        }

        Page<MaterialApplication> page = new Page<>(resolvePageNum(request.getPageNo(), request.getPageNum()), normalizePageSize(request.getPageSize()));
        LambdaQueryWrapper<MaterialApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialApplication::getStudentId, request.getStudentId())
                .eq(StringUtils.hasText(request.getStatus()), MaterialApplication::getStatus, request.getStatus())
                .eq(request.getItemId() != null, MaterialApplication::getItemId, request.getItemId())
                .in(categoryItemIds != null, MaterialApplication::getItemId, categoryItemIds)
                .and(StringUtils.hasText(request.getKeyword()), w -> w
                        .like(MaterialApplication::getTitle, request.getKeyword())
                        .or()
                        .like(MaterialApplication::getDescription, request.getKeyword()))
                .ge(request.getStartTime() != null, MaterialApplication::getCreateTime, request.getStartTime())
                .le(request.getEndTime() != null, MaterialApplication::getCreateTime, request.getEndTime())
                .orderByDesc(MaterialApplication::getUpdateTime)
                .orderByDesc(MaterialApplication::getCreateTime)
                .orderByDesc(MaterialApplication::getId);
        Page<MaterialApplication> result = page(page, wrapper);
        ApplicationContext context = loadApplicationContext(result.getRecords());
        List<MyMaterialApplicationVO> records = result.getRecords().stream()
                .map(entity -> toMyMaterialApplicationVO(entity, context))
                .toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public MaterialApplicationDetailVO getFrontendApplicationDetail(Long id) {
        MaterialApplication entity = getExisting(id);
        ApplicationContext context = loadApplicationContext(List.of(entity));
        List<MaterialAttachmentVO> attachments = listAttachmentsByMaterialId(id).stream()
                .map(this::toAttachmentVO)
                .toList();
        List<ReviewRecordVO> reviewRecords = reviewRecordServiceProvider.getObject().listByMaterialId(id);
        return toMaterialApplicationDetailVO(entity, context, attachments, reviewRecords);
    }

    @Override
    public MyApplicationStatisticsVO getMyApplicationStatistics(Long studentId) {
        // TODO After authentication is introduced, studentId must come from current user context instead of request parameters.
        checkStudentExists(studentId);
        List<MaterialApplication> applications = lambdaQuery()
                .eq(MaterialApplication::getStudentId, studentId)
                .list();
        MyApplicationStatisticsVO vo = new MyApplicationStatisticsVO();
        vo.setStudentId(studentId);
        vo.setTotalCount((long) applications.size());
        vo.setDraftCount(countByStatus(applications, STATUS_DRAFT));
        vo.setSubmittedCount(countByStatus(applications, STATUS_SUBMITTED));
        vo.setApprovedCount(countByStatus(applications, STATUS_APPROVED));
        vo.setRejectedCount(countByStatus(applications, STATUS_REJECTED));
        vo.setCancelledCount(countByStatus(applications, STATUS_CANCELLED));
        return vo;
    }

    @Override
    public PageResult<PendingMaterialApplicationVO> pagePendingApplications(PendingMaterialApplicationPageRequest request) {
        // TODO After permission support is introduced, filter pending data by reviewer_scope or current reviewer permissions.
        String status = StringUtils.hasText(request.getStatus()) ? request.getStatus() : STATUS_SUBMITTED;
        checkStatus(status);
        if (request.getClassId() != null && classInfoService.getById(request.getClassId()) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Class does not exist or has been deleted");
        }
        if (request.getMajorId() != null && majorInfoService.getById(request.getMajorId()) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Major does not exist or has been deleted");
        }

        List<Long> studentIds = listFilteredStudentIds(request);
        if (studentIds != null && studentIds.isEmpty()) {
            return PageResult.of(List.of(), 0L, resolvePageNum(request.getPageNo(), request.getPageNum()), normalizePageSize(request.getPageSize()));
        }
        List<Long> categoryItemIds = listItemIdsByCategory(request.getCategoryId());
        if (categoryItemIds != null && categoryItemIds.isEmpty()) {
            return PageResult.of(List.of(), 0L, resolvePageNum(request.getPageNo(), request.getPageNum()), normalizePageSize(request.getPageSize()));
        }

        Page<MaterialApplication> page = new Page<>(resolvePageNum(request.getPageNo(), request.getPageNum()), normalizePageSize(request.getPageSize()));
        LambdaQueryWrapper<MaterialApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialApplication::getStatus, status)
                .eq(request.getStudentId() != null, MaterialApplication::getStudentId, request.getStudentId())
                .in(studentIds != null, MaterialApplication::getStudentId, studentIds)
                .eq(request.getItemId() != null, MaterialApplication::getItemId, request.getItemId())
                .in(categoryItemIds != null, MaterialApplication::getItemId, categoryItemIds)
                .and(StringUtils.hasText(request.getKeyword()), w -> w
                        .like(MaterialApplication::getTitle, request.getKeyword())
                        .or()
                        .like(MaterialApplication::getDescription, request.getKeyword()))
                .ge(request.getStartTime() != null, MaterialApplication::getSubmitTime, request.getStartTime())
                .le(request.getEndTime() != null, MaterialApplication::getSubmitTime, request.getEndTime())
                .orderByDesc(MaterialApplication::getSubmitTime)
                .orderByDesc(MaterialApplication::getCreateTime)
                .orderByDesc(MaterialApplication::getId);
        Page<MaterialApplication> result = page(page, wrapper);
        ApplicationContext context = loadApplicationContext(result.getRecords());
        List<PendingMaterialApplicationVO> records = result.getRecords().stream()
                .map(entity -> toPendingMaterialApplicationVO(entity, context))
                .toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    private MaterialApplication getExisting(Long id) {
        MaterialApplication entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Material application does not exist");
        }
        return entity;
    }

    private void checkStudentExists(Long studentId) {
        if (studentService.getById(studentId) == null) {
            throw new BusinessException("Student does not exist or has been deleted");
        }
    }

    private void checkItemExists(Long itemId) {
        if (evaluationItemService.getById(itemId) == null) {
            throw new BusinessException("Evaluation item does not exist or has been deleted");
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

    private void checkStatus(String status) {
        if (!STATUSES.contains(status)) {
            throw new BusinessException("Material status must be DRAFT, SUBMITTED, APPROVED, REJECTED, or CANCELLED");
        }
    }

    private void checkReviewScoreRequired(BigDecimal reviewScore) {
        if (reviewScore == null) {
            throw new BusinessException("reviewScore must not be null");
        }
        if (reviewScore.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("reviewScore must be greater than or equal to 0");
        }
    }

    private void checkReviewCommentLength(String reviewComment) {
        checkTextLength(reviewComment, "reviewComment");
    }

    private void checkTextLength(String text, String fieldName) {
        if (text != null && text.length() > 500) {
            throw new BusinessException(fieldName + " length must be less than or equal to 500");
        }
    }

    private void createReviewRecord(Long materialId, Long reviewerId, String beforeStatus, String afterStatus, String reviewResult,
                                    BigDecimal reviewScore, String reviewComment, LocalDateTime reviewTime) {
        CreateReviewRecordRequest reviewRecordRequest = new CreateReviewRecordRequest();
        reviewRecordRequest.setMaterialId(materialId);
        reviewRecordRequest.setReviewerId(reviewerId);
        reviewRecordRequest.setBeforeStatus(beforeStatus);
        reviewRecordRequest.setAfterStatus(afterStatus);
        reviewRecordRequest.setReviewResult(reviewResult);
        reviewRecordRequest.setReviewScore(reviewScore);
        reviewRecordRequest.setReviewComment(reviewComment);
        reviewRecordRequest.setReviewTime(reviewTime);
        reviewRecordServiceProvider.getObject().createReviewRecord(reviewRecordRequest);
    }

    private List<Long> listItemIdsByCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        if (evaluationCategoryService.getById(categoryId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Evaluation category does not exist or has been deleted");
        }
        return evaluationItemService.lambdaQuery()
                .eq(EvaluationItem::getCategoryId, categoryId)
                .list()
                .stream()
                .map(EvaluationItem::getId)
                .toList();
    }

    private List<Long> listFilteredStudentIds(PendingMaterialApplicationPageRequest request) {
        boolean hasStudentFilter = request.getStudentId() != null
                || StringUtils.hasText(request.getStudentNo())
                || StringUtils.hasText(request.getStudentName())
                || request.getClassId() != null
                || request.getMajorId() != null;
        if (!hasStudentFilter) {
            return null;
        }
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(request.getStudentId() != null, Student::getId, request.getStudentId())
                .like(StringUtils.hasText(request.getStudentNo()), Student::getStudentNo, request.getStudentNo())
                .like(StringUtils.hasText(request.getStudentName()), Student::getName, request.getStudentName())
                .eq(request.getClassId() != null, Student::getClassId, request.getClassId())
                .eq(request.getMajorId() != null, Student::getMajorId, request.getMajorId());
        return studentService.list(wrapper).stream().map(Student::getId).toList();
    }

    private ApplicationContext loadApplicationContext(List<MaterialApplication> applications) {
        ApplicationContext context = new ApplicationContext();
        if (applications.isEmpty()) {
            return context;
        }
        Set<Long> materialIds = applications.stream().map(MaterialApplication::getId).collect(Collectors.toSet());
        Set<Long> studentIds = applications.stream().map(MaterialApplication::getStudentId).collect(Collectors.toSet());
        Set<Long> itemIds = applications.stream().map(MaterialApplication::getItemId).collect(Collectors.toSet());

        context.students = mapById(studentService.listByIds(studentIds), Student::getId);
        context.items = mapById(evaluationItemService.listByIds(itemIds), EvaluationItem::getId);
        Set<Long> categoryIds = context.items.values().stream().map(EvaluationItem::getCategoryId).collect(Collectors.toSet());
        Set<Long> classIds = context.students.values().stream().map(Student::getClassId).collect(Collectors.toSet());
        Set<Long> majorIds = context.students.values().stream().map(Student::getMajorId).collect(Collectors.toSet());
        context.categories = categoryIds.isEmpty() ? Collections.emptyMap() : mapById(evaluationCategoryService.listByIds(categoryIds), EvaluationCategory::getId);
        context.classes = classIds.isEmpty() ? Collections.emptyMap() : mapById(classInfoService.listByIds(classIds), ClassInfo::getId);
        context.majors = majorIds.isEmpty() ? Collections.emptyMap() : mapById(majorInfoService.listByIds(majorIds), MajorInfo::getId);
        context.attachmentCounts = loadAttachmentCounts(materialIds);
        return context;
    }

    private Map<Long, Integer> loadAttachmentCounts(Collection<Long> materialIds) {
        if (materialIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return listAttachmentsByMaterialIds(materialIds).stream()
                .collect(Collectors.groupingBy(MaterialAttachment::getMaterialId, Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }

    private List<MaterialAttachment> listAttachmentsByMaterialId(Long materialId) {
        return materialAttachmentServiceProvider.getObject().lambdaQuery()
                .eq(MaterialAttachment::getMaterialId, materialId)
                .orderByDesc(MaterialAttachment::getId)
                .list();
    }

    private List<MaterialAttachment> listAttachmentsByMaterialIds(Collection<Long> materialIds) {
        if (materialIds.isEmpty()) {
            return List.of();
        }
        return materialAttachmentServiceProvider.getObject().lambdaQuery()
                .in(MaterialAttachment::getMaterialId, materialIds)
                .list();
    }

    private <T> Map<Long, T> mapById(Collection<T> entities, Function<T, Long> idFunction) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyMap();
        }
        return entities.stream().collect(Collectors.toMap(idFunction, Function.identity(), (left, right) -> left));
    }

    private MyMaterialApplicationVO toMyMaterialApplicationVO(MaterialApplication entity, ApplicationContext context) {
        MyMaterialApplicationVO vo = new MyMaterialApplicationVO();
        vo.setId(entity.getId());
        vo.setStudentId(entity.getStudentId());
        vo.setItemId(entity.getItemId());
        fillItemFields(vo, entity.getItemId(), context);
        vo.setTitle(entity.getTitle());
        vo.setDescription(entity.getDescription());
        vo.setApplyScore(entity.getApplyScore());
        vo.setFinalScore(entity.getFinalScore());
        vo.setStatus(entity.getStatus());
        vo.setSubmitTime(entity.getSubmitTime());
        vo.setReviewTime(entity.getReviewTime());
        vo.setRejectReason(entity.getRejectReason());
        vo.setSubmitCount(entity.getSubmitCount());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        vo.setAttachmentCount(context.attachmentCounts.getOrDefault(entity.getId(), 0));
        return vo;
    }

    private PendingMaterialApplicationVO toPendingMaterialApplicationVO(MaterialApplication entity, ApplicationContext context) {
        PendingMaterialApplicationVO vo = new PendingMaterialApplicationVO();
        vo.setId(entity.getId());
        vo.setStudentId(entity.getStudentId());
        fillStudentFields(vo, entity.getStudentId(), context);
        vo.setItemId(entity.getItemId());
        fillItemFields(vo, entity.getItemId(), context);
        vo.setTitle(entity.getTitle());
        vo.setApplyScore(entity.getApplyScore());
        vo.setStatus(entity.getStatus());
        vo.setSubmitTime(entity.getSubmitTime());
        vo.setAttachmentCount(context.attachmentCounts.getOrDefault(entity.getId(), 0));
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private MaterialApplicationDetailVO toMaterialApplicationDetailVO(MaterialApplication entity, ApplicationContext context,
                                                                     List<MaterialAttachmentVO> attachments,
                                                                     List<ReviewRecordVO> reviewRecords) {
        MaterialApplicationDetailVO vo = new MaterialApplicationDetailVO();
        vo.setId(entity.getId());
        vo.setStudentId(entity.getStudentId());
        fillStudentFields(vo, entity.getStudentId(), context);
        vo.setItemId(entity.getItemId());
        fillItemFields(vo, entity.getItemId(), context);
        vo.setTitle(entity.getTitle());
        vo.setDescription(entity.getDescription());
        vo.setApplyScore(entity.getApplyScore());
        vo.setFinalScore(entity.getFinalScore());
        vo.setStatus(entity.getStatus());
        vo.setSubmitTime(entity.getSubmitTime());
        vo.setReviewTime(entity.getReviewTime());
        vo.setRejectReason(entity.getRejectReason());
        vo.setSubmitCount(entity.getSubmitCount());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        vo.setAttachments(attachments);
        vo.setReviewRecords(reviewRecords);
        return vo;
    }

    private void fillItemFields(MyMaterialApplicationVO vo, Long itemId, ApplicationContext context) {
        EvaluationItem item = context.items.get(itemId);
        if (item == null) {
            return;
        }
        vo.setItemName(item.getItemName());
        vo.setCategoryId(item.getCategoryId());
        EvaluationCategory category = context.categories.get(item.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getCategoryName());
        }
    }

    private void fillItemFields(PendingMaterialApplicationVO vo, Long itemId, ApplicationContext context) {
        EvaluationItem item = context.items.get(itemId);
        if (item == null) {
            return;
        }
        vo.setItemName(item.getItemName());
        vo.setCategoryId(item.getCategoryId());
        EvaluationCategory category = context.categories.get(item.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getCategoryName());
        }
    }

    private void fillItemFields(MaterialApplicationDetailVO vo, Long itemId, ApplicationContext context) {
        EvaluationItem item = context.items.get(itemId);
        if (item == null) {
            return;
        }
        vo.setItemName(item.getItemName());
        vo.setCategoryId(item.getCategoryId());
        EvaluationCategory category = context.categories.get(item.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getCategoryName());
        }
    }

    private void fillStudentFields(PendingMaterialApplicationVO vo, Long studentId, ApplicationContext context) {
        Student student = context.students.get(studentId);
        if (student == null) {
            return;
        }
        vo.setStudentNo(student.getStudentNo());
        vo.setStudentName(student.getName());
        vo.setClassId(student.getClassId());
        vo.setMajorId(student.getMajorId());
        ClassInfo classInfo = context.classes.get(student.getClassId());
        if (classInfo != null) {
            vo.setClassName(classInfo.getClassName());
        }
        MajorInfo majorInfo = context.majors.get(student.getMajorId());
        if (majorInfo != null) {
            vo.setMajorName(majorInfo.getMajorName());
        }
    }

    private void fillStudentFields(MaterialApplicationDetailVO vo, Long studentId, ApplicationContext context) {
        Student student = context.students.get(studentId);
        if (student == null) {
            return;
        }
        vo.setStudentNo(student.getStudentNo());
        vo.setStudentName(student.getName());
        vo.setClassId(student.getClassId());
        vo.setMajorId(student.getMajorId());
        ClassInfo classInfo = context.classes.get(student.getClassId());
        if (classInfo != null) {
            vo.setClassName(classInfo.getClassName());
        }
        MajorInfo majorInfo = context.majors.get(student.getMajorId());
        if (majorInfo != null) {
            vo.setMajorName(majorInfo.getMajorName());
        }
    }

    private MaterialAttachmentVO toAttachmentVO(MaterialAttachment entity) {
        MaterialAttachmentVO vo = new MaterialAttachmentVO();
        vo.setId(entity.getId());
        vo.setMaterialId(entity.getMaterialId());
        vo.setOriginalName(entity.getOriginalName());
        vo.setStoredName(entity.getStoredName());
        vo.setFilePath(entity.getFilePath());
        vo.setFileUrl(entity.getFileUrl());
        vo.setFileType(entity.getFileType());
        vo.setFileSize(entity.getFileSize());
        vo.setUploadTime(entity.getUploadTime());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private long countByStatus(List<MaterialApplication> applications, String status) {
        return applications.stream().filter(application -> status.equals(application.getStatus())).count();
    }

    private MaterialApplicationVO toVO(MaterialApplication entity) {
        MaterialApplicationVO vo = new MaterialApplicationVO();
        vo.setId(entity.getId());
        vo.setStudentId(entity.getStudentId());
        vo.setItemId(entity.getItemId());
        vo.setTitle(entity.getTitle());
        vo.setDescription(entity.getDescription());
        vo.setApplyScore(entity.getApplyScore());
        vo.setFinalScore(entity.getFinalScore());
        vo.setStatus(entity.getStatus());
        vo.setRejectReason(entity.getRejectReason());
        vo.setSubmitCount(entity.getSubmitCount());
        vo.setSubmitTime(entity.getSubmitTime());
        vo.setReviewTime(entity.getReviewTime());
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

    private long resolvePageNum(Long pageNo, Long pageNum) {
        return normalizePageNum(pageNo == null ? pageNum : pageNo);
    }

    private Integer defaultSubmitCount(Integer submitCount) {
        return submitCount == null ? 0 : submitCount;
    }

    private static class ApplicationContext {

        private Map<Long, Student> students = new HashMap<>();

        private Map<Long, EvaluationItem> items = new HashMap<>();

        private Map<Long, EvaluationCategory> categories = new HashMap<>();

        private Map<Long, ClassInfo> classes = new HashMap<>();

        private Map<Long, MajorInfo> majors = new HashMap<>();

        private Map<Long, Integer> attachmentCounts = new HashMap<>();
    }
}
