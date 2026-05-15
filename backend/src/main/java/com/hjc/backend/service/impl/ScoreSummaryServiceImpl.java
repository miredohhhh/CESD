package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.common.PageRequest;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.common.ResultCode;
import com.hjc.backend.dto.ScoreSummaryPageRequest;
import com.hjc.backend.entity.ClassInfo;
import com.hjc.backend.entity.EvaluationCategory;
import com.hjc.backend.entity.EvaluationItem;
import com.hjc.backend.entity.MajorInfo;
import com.hjc.backend.entity.MaterialApplication;
import com.hjc.backend.entity.ScoreCategorySummary;
import com.hjc.backend.entity.ScoreSummary;
import com.hjc.backend.entity.Student;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.mapper.ScoreSummaryMapper;
import com.hjc.backend.security.CurrentUserUtils;
import com.hjc.backend.service.ClassInfoService;
import com.hjc.backend.service.EvaluationCategoryService;
import com.hjc.backend.service.EvaluationItemService;
import com.hjc.backend.service.MajorInfoService;
import com.hjc.backend.service.MaterialApplicationService;
import com.hjc.backend.service.ScoreCategorySummaryService;
import com.hjc.backend.service.ScoreSummaryService;
import com.hjc.backend.service.StudentService;
import com.hjc.backend.vo.FrontendScoreSummaryVO;
import com.hjc.backend.vo.ScoreCategorySummaryVO;
import com.hjc.backend.vo.ScoreSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreSummaryServiceImpl extends ServiceImpl<ScoreSummaryMapper, ScoreSummary> implements ScoreSummaryService {

    private static final String MATERIAL_STATUS_APPROVED = "APPROVED";

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private static final Integer ENABLED_STATUS = 1;

    private final ScoreCategorySummaryService scoreCategorySummaryService;

    private final MaterialApplicationService materialApplicationService;

    private final EvaluationItemService evaluationItemService;

    private final EvaluationCategoryService evaluationCategoryService;

    private final StudentService studentService;

    private final ClassInfoService classInfoService;

    private final MajorInfoService majorInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScoreSummaryVO recalculateStudentScore(Long studentId) {
        Student student = validateStudentExists(studentId);
        LocalDateTime now = LocalDateTime.now();
        recalculateStudentScoreInternal(student, now);
        updateClassRanking(student.getClassId(), now);
        updateMajorRanking(student.getMajorId(), now);
        return getStudentScore(studentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ScoreSummaryVO> recalculateClassScores(Long classId) {
        ClassInfo classInfo = validateClassExists(classId);
        LocalDateTime now = LocalDateTime.now();
        List<Student> students = listStudentsByClass(classId);
        for (Student student : students) {
            recalculateStudentScoreInternal(student, now);
        }
        updateClassRanking(classId, now);
        updateMajorRanking(classInfo.getMajorId(), now);
        return listClassRanking(classId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ScoreSummaryVO> recalculateMajorScores(Long majorId) {
        validateMajorExists(majorId);
        LocalDateTime now = LocalDateTime.now();
        List<Student> students = listStudentsByMajor(majorId);
        Set<Long> classIds = new HashSet<>();
        for (Student student : students) {
            recalculateStudentScoreInternal(student, now);
            classIds.add(student.getClassId());
        }
        for (Long classId : classIds) {
            updateClassRanking(classId, now);
        }
        updateMajorRanking(majorId, now);
        return listMajorRanking(majorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recalculateAllScores() {
        LocalDateTime now = LocalDateTime.now();
        List<Student> students = studentService.list();
        Set<Long> classIds = new HashSet<>();
        Set<Long> majorIds = new HashSet<>();
        for (Student student : students) {
            recalculateStudentScoreInternal(student, now);
            classIds.add(student.getClassId());
            majorIds.add(student.getMajorId());
        }
        for (Long classId : classIds) {
            updateClassRanking(classId, now);
        }
        for (Long majorId : majorIds) {
            updateMajorRanking(majorId, now);
        }
    }

    @Override
    public ScoreSummaryVO getStudentScore(Long studentId) {
        validateStudentExists(studentId);
        ScoreSummary entity = lambdaQuery().eq(ScoreSummary::getStudentId, studentId).one();
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Score has not been calculated");
        }
        return toScoreSummaryVO(entity);
    }

    @Override
    public List<ScoreCategorySummaryVO> listStudentCategoryScores(Long studentId) {
        validateStudentExists(studentId);
        return scoreCategorySummaryService.lambdaQuery()
                .eq(ScoreCategorySummary::getStudentId, studentId)
                .orderByAsc(ScoreCategorySummary::getCategoryId)
                .list()
                .stream()
                .map(this::toScoreCategorySummaryVO)
                .toList();
    }

    @Override
    public PageResult<ScoreSummaryVO> pageScoreSummaries(ScoreSummaryPageRequest request) {
        List<Long> filteredStudentIds = listFilteredStudentIds(request.getClassId(), request.getMajorId());
        if (filteredStudentIds != null && filteredStudentIds.isEmpty()) {
            return PageResult.of(List.of(), 0L, normalizePageNum(request.getPageNum()), normalizePageSize(request.getPageSize()));
        }

        Page<ScoreSummary> page = new Page<>(normalizePageNum(request.getPageNum()), normalizePageSize(request.getPageSize()));
        LambdaQueryWrapper<ScoreSummary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(request.getStudentId() != null, ScoreSummary::getStudentId, request.getStudentId())
                .in(filteredStudentIds != null, ScoreSummary::getStudentId, filteredStudentIds)
                .eq(request.getStatus() != null, ScoreSummary::getStatus, request.getStatus())
                .orderByDesc(ScoreSummary::getTotalScore)
                .orderByAsc(ScoreSummary::getClassRank)
                .orderByAsc(ScoreSummary::getMajorRank)
                .orderByAsc(ScoreSummary::getStudentId);
        Page<ScoreSummary> result = page(page, wrapper);
        List<ScoreSummaryVO> records = result.getRecords().stream().map(this::toScoreSummaryVO).toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<ScoreSummaryVO> listClassRanking(Long classId) {
        validateClassExists(classId);
        List<Student> students = listStudentsByClass(classId);
        ensureScoreSummaries(students, LocalDateTime.now());
        return listRankingByStudents(students).stream()
                .sorted(Comparator
                        .comparing(ScoreSummary::getClassRank, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ScoreSummary::getTotalScore, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ScoreSummary::getStudentId))
                .map(this::toScoreSummaryVO)
                .toList();
    }

    @Override
    public List<ScoreSummaryVO> listMajorRanking(Long majorId) {
        validateMajorExists(majorId);
        List<Student> students = listStudentsByMajor(majorId);
        ensureScoreSummaries(students, LocalDateTime.now());
        return listRankingByStudents(students).stream()
                .sorted(Comparator
                        .comparing(ScoreSummary::getMajorRank, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ScoreSummary::getTotalScore, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ScoreSummary::getStudentId))
                .map(this::toScoreSummaryVO)
                .toList();
    }

    @Override
    public FrontendScoreSummaryVO getMyScore() {
        Long effectiveStudentId = CurrentUserUtils.requireStudentId();
        validateStudentExists(effectiveStudentId);
        ScoreSummary entity = lambdaQuery().eq(ScoreSummary::getStudentId, effectiveStudentId).one();
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Score has not been calculated");
        }
        return toFrontendScoreSummaryVO(entity, true);
    }

    @Override
    public List<ScoreCategorySummaryVO> getMyCategoryScores() {
        return listStudentCategoryScores(CurrentUserUtils.requireStudentId());
    }

    @Override
    public PageResult<FrontendScoreSummaryVO> pageClassRankingForFrontend(Long classId, PageRequest request) {
        validateClassExists(classId);
        List<Student> students = listStudentsByClass(classId);
        return pageRankingForFrontend(students, true, request);
    }

    @Override
    public PageResult<FrontendScoreSummaryVO> pageMajorRankingForFrontend(Long majorId, PageRequest request) {
        validateMajorExists(majorId);
        List<Student> students = listStudentsByMajor(majorId);
        return pageRankingForFrontend(students, false, request);
    }

    private ScoreSummary recalculateStudentScoreInternal(Student student, LocalDateTime now) {
        Map<Long, BigDecimal> categoryScoreMap = calculateCategoryScores(student.getId());
        updateCategorySummaries(student.getId(), categoryScoreMap, now);
        BigDecimal totalScore = categoryScoreMap.values().stream().reduce(ZERO, BigDecimal::add);
        ScoreSummary scoreSummary = getOrCreateScoreSummary(student.getId(), now);
        scoreSummary.setTotalScore(totalScore);
        scoreSummary.setCalculateTime(now);
        scoreSummary.setStatus(ENABLED_STATUS);
        scoreSummary.setUpdateTime(now);
        if (scoreSummary.getId() == null) {
            save(scoreSummary);
        } else {
            updateById(scoreSummary);
        }
        return scoreSummary;
    }

    private Map<Long, BigDecimal> calculateCategoryScores(Long studentId) {
        List<MaterialApplication> approvedMaterials = materialApplicationService.lambdaQuery()
                .eq(MaterialApplication::getStudentId, studentId)
                .eq(MaterialApplication::getStatus, MATERIAL_STATUS_APPROVED)
                .list();

        Map<Long, BigDecimal> categoryScoreMap = new HashMap<>();
        for (MaterialApplication material : approvedMaterials) {
            if (material.getFinalScore() == null) {
                // TODO Use evaluation_item.score or configurable rules when final_score is empty.
                continue;
            }
            if (material.getFinalScore().compareTo(ZERO) < 0) {
                throw new BusinessException("Material final score must be greater than or equal to 0");
            }
            EvaluationItem item = evaluationItemService.getById(material.getItemId());
            if (item == null) {
                throw new BusinessException("Evaluation item does not exist or has been deleted");
            }
            EvaluationCategory category = evaluationCategoryService.getById(item.getCategoryId());
            if (category == null) {
                throw new BusinessException("Evaluation category does not exist or has been deleted");
            }
            BigDecimal currentScore = categoryScoreMap.getOrDefault(category.getId(), ZERO).add(material.getFinalScore());
            if (category.getMaxScore() != null && currentScore.compareTo(category.getMaxScore()) > 0) {
                currentScore = category.getMaxScore();
            }
            if (currentScore.compareTo(ZERO) < 0) {
                throw new BusinessException("Category score must be greater than or equal to 0");
            }
            categoryScoreMap.put(category.getId(), currentScore);
        }
        return categoryScoreMap;
    }

    private void updateCategorySummaries(Long studentId, Map<Long, BigDecimal> categoryScoreMap, LocalDateTime now) {
        List<ScoreCategorySummary> existingSummaries = scoreCategorySummaryService.lambdaQuery()
                .eq(ScoreCategorySummary::getStudentId, studentId)
                .list();
        Map<Long, ScoreCategorySummary> existingByCategoryId = existingSummaries.stream()
                .collect(Collectors.toMap(ScoreCategorySummary::getCategoryId, Function.identity()));

        for (Map.Entry<Long, BigDecimal> entry : categoryScoreMap.entrySet()) {
            ScoreCategorySummary summary = existingByCategoryId.remove(entry.getKey());
            if (summary == null) {
                summary = new ScoreCategorySummary();
                summary.setStudentId(studentId);
                summary.setCategoryId(entry.getKey());
                summary.setCategoryScore(entry.getValue());
                summary.setCalculateTime(now);
                summary.setCreateTime(now);
                summary.setUpdateTime(now);
                scoreCategorySummaryService.save(summary);
            } else {
                summary.setCategoryScore(entry.getValue());
                summary.setCalculateTime(now);
                summary.setUpdateTime(now);
                scoreCategorySummaryService.updateById(summary);
            }
        }

        for (ScoreCategorySummary staleSummary : existingByCategoryId.values()) {
            // TODO Consider logical deletion or cleanup when a category no longer has approved materials.
            staleSummary.setCategoryScore(ZERO);
            staleSummary.setCalculateTime(now);
            staleSummary.setUpdateTime(now);
            scoreCategorySummaryService.updateById(staleSummary);
        }
    }

    private void updateClassRanking(Long classId, LocalDateTime now) {
        if (classId == null) {
            return;
        }
        List<Student> students = listStudentsByClass(classId);
        updateRanking(students, now, true);
    }

    private void updateMajorRanking(Long majorId, LocalDateTime now) {
        if (majorId == null) {
            return;
        }
        List<Student> students = listStudentsByMajor(majorId);
        updateRanking(students, now, false);
    }

    private void updateRanking(List<Student> students, LocalDateTime now, boolean classRanking) {
        ensureScoreSummaries(students, now);
        List<ScoreSummary> summaries = listRankingByStudents(students);
        int previousRank = 0;
        BigDecimal previousScore = null;
        for (int i = 0; i < summaries.size(); i++) {
            ScoreSummary summary = summaries.get(i);
            BigDecimal score = defaultScore(summary.getTotalScore());
            int rank = previousScore != null && score.compareTo(previousScore) == 0 ? previousRank : i + 1;
            if (classRanking) {
                summary.setClassRank(rank);
            } else {
                summary.setMajorRank(rank);
            }
            summary.setUpdateTime(now);
            updateById(summary);
            previousRank = rank;
            previousScore = score;
        }
    }

    private void ensureScoreSummaries(List<Student> students, LocalDateTime now) {
        for (Student student : students) {
            ScoreSummary summary = lambdaQuery().eq(ScoreSummary::getStudentId, student.getId()).one();
            if (summary == null) {
                summary = new ScoreSummary();
                summary.setStudentId(student.getId());
                summary.setTotalScore(ZERO);
                summary.setCalculateTime(now);
                summary.setStatus(ENABLED_STATUS);
                summary.setCreateTime(now);
                summary.setUpdateTime(now);
                save(summary);
            }
        }
    }

    private List<ScoreSummary> listRankingByStudents(List<Student> students) {
        if (students.isEmpty()) {
            return List.of();
        }
        List<Long> studentIds = students.stream().map(Student::getId).toList();
        Map<Long, Student> studentMap = students.stream().collect(Collectors.toMap(Student::getId, Function.identity()));
        return lambdaQuery()
                .in(ScoreSummary::getStudentId, studentIds)
                .list()
                .stream()
                .sorted(Comparator
                        .comparing((ScoreSummary summary) -> defaultScore(summary.getTotalScore()), Comparator.reverseOrder())
                        .thenComparing(summary -> studentMap.get(summary.getStudentId()).getStudentNo(), Comparator.nullsLast(String::compareTo))
                        .thenComparing(ScoreSummary::getStudentId))
                .toList();
    }

    private ScoreSummary getOrCreateScoreSummary(Long studentId, LocalDateTime now) {
        ScoreSummary entity = lambdaQuery().eq(ScoreSummary::getStudentId, studentId).one();
        if (entity != null) {
            return entity;
        }
        entity = new ScoreSummary();
        entity.setStudentId(studentId);
        entity.setTotalScore(ZERO);
        entity.setCalculateTime(now);
        entity.setStatus(ENABLED_STATUS);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        return entity;
    }

    private Student validateStudentExists(Long studentId) {
        if (studentId == null) {
            throw new BusinessException("studentId must not be null");
        }
        Student student = studentService.getById(studentId);
        if (student == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Student does not exist or has been deleted");
        }
        return student;
    }

    private ClassInfo validateClassExists(Long classId) {
        if (classId == null) {
            throw new BusinessException("classId must not be null");
        }
        ClassInfo classInfo = classInfoService.getById(classId);
        if (classInfo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Class does not exist or has been deleted");
        }
        return classInfo;
    }

    private MajorInfo validateMajorExists(Long majorId) {
        if (majorId == null) {
            throw new BusinessException("majorId must not be null");
        }
        MajorInfo majorInfo = majorInfoService.getById(majorId);
        if (majorInfo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Major does not exist or has been deleted");
        }
        return majorInfo;
    }

    private List<Student> listStudentsByClass(Long classId) {
        return studentService.lambdaQuery()
                .eq(Student::getClassId, classId)
                .orderByAsc(Student::getStudentNo)
                .orderByAsc(Student::getId)
                .list();
    }

    private List<Student> listStudentsByMajor(Long majorId) {
        return studentService.lambdaQuery()
                .eq(Student::getMajorId, majorId)
                .orderByAsc(Student::getStudentNo)
                .orderByAsc(Student::getId)
                .list();
    }

    private List<Long> listFilteredStudentIds(Long classId, Long majorId) {
        if (classId == null && majorId == null) {
            return null;
        }
        if (classId != null) {
            validateClassExists(classId);
        }
        if (majorId != null) {
            validateMajorExists(majorId);
        }
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(classId != null, Student::getClassId, classId)
                .eq(majorId != null, Student::getMajorId, majorId);
        return studentService.list(wrapper).stream().map(Student::getId).toList();
    }

    private PageResult<FrontendScoreSummaryVO> pageRankingForFrontend(List<Student> students, boolean classRanking, PageRequest request) {
        PageRequest pageRequest = request == null ? new PageRequest() : request;
        if (students.isEmpty()) {
            return PageResult.of(List.of(), 0L, normalizePageNum(pageRequest.getPageNum()), normalizePageSize(pageRequest.getPageSize()));
        }
        List<Long> studentIds = students.stream().map(Student::getId).toList();
        Comparator<ScoreSummary> comparator = classRanking
                ? Comparator.comparing(ScoreSummary::getClassRank, Comparator.nullsLast(Integer::compareTo))
                : Comparator.comparing(ScoreSummary::getMajorRank, Comparator.nullsLast(Integer::compareTo));
        List<FrontendScoreSummaryVO> sorted = lambdaQuery()
                .in(ScoreSummary::getStudentId, studentIds)
                .list()
                .stream()
                .sorted(comparator
                        .thenComparing((ScoreSummary summary) -> defaultScore(summary.getTotalScore()), Comparator.reverseOrder())
                        .thenComparing(ScoreSummary::getStudentId))
                .map(entity -> toFrontendScoreSummaryVO(entity, false))
                .toList();
        long pageNum = normalizePageNum(pageRequest.getPageNum());
        long pageSize = normalizePageSize(pageRequest.getPageSize());
        int fromIndex = (int) Math.min((pageNum - 1) * pageSize, sorted.size());
        int toIndex = (int) Math.min(fromIndex + pageSize, sorted.size());
        return PageResult.of(sorted.subList(fromIndex, toIndex), (long) sorted.size(), pageNum, pageSize);
    }

    private ScoreSummaryVO toScoreSummaryVO(ScoreSummary entity) {
        ScoreSummaryVO vo = new ScoreSummaryVO();
        vo.setId(entity.getId());
        vo.setStudentId(entity.getStudentId());
        vo.setTotalScore(entity.getTotalScore());
        vo.setClassRank(entity.getClassRank());
        vo.setMajorRank(entity.getMajorRank());
        vo.setCalculateTime(entity.getCalculateTime());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        fillStudentFields(vo, entity.getStudentId());
        return vo;
    }

    private FrontendScoreSummaryVO toFrontendScoreSummaryVO(ScoreSummary entity, boolean includeCategoryScores) {
        ScoreSummaryVO source = toScoreSummaryVO(entity);
        FrontendScoreSummaryVO vo = new FrontendScoreSummaryVO();
        vo.setStudentId(source.getStudentId());
        vo.setStudentNo(source.getStudentNo());
        vo.setStudentName(source.getStudentName());
        vo.setClassId(source.getClassId());
        vo.setClassName(source.getClassName());
        vo.setMajorId(source.getMajorId());
        vo.setMajorName(source.getMajorName());
        vo.setTotalScore(source.getTotalScore());
        vo.setClassRank(source.getClassRank());
        vo.setMajorRank(source.getMajorRank());
        vo.setCalculateTime(source.getCalculateTime());
        vo.setStatus(source.getStatus());
        vo.setCategoryScores(includeCategoryScores ? listStudentCategoryScores(source.getStudentId()) : List.of());
        return vo;
    }

    private ScoreCategorySummaryVO toScoreCategorySummaryVO(ScoreCategorySummary entity) {
        ScoreCategorySummaryVO vo = new ScoreCategorySummaryVO();
        vo.setId(entity.getId());
        vo.setStudentId(entity.getStudentId());
        vo.setCategoryId(entity.getCategoryId());
        vo.setCategoryScore(entity.getCategoryScore());
        vo.setCalculateTime(entity.getCalculateTime());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        EvaluationCategory category = evaluationCategoryService.getById(entity.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getCategoryName());
            vo.setCategoryCode(category.getCategoryCode());
        }
        return vo;
    }

    private void fillStudentFields(ScoreSummaryVO vo, Long studentId) {
        Student student = studentService.getById(studentId);
        if (student == null) {
            return;
        }
        vo.setStudentNo(student.getStudentNo());
        vo.setStudentName(student.getName());
        vo.setClassId(student.getClassId());
        vo.setMajorId(student.getMajorId());
        ClassInfo classInfo = student.getClassId() == null ? null : classInfoService.getById(student.getClassId());
        if (classInfo != null) {
            vo.setClassName(classInfo.getClassName());
        }
        MajorInfo majorInfo = student.getMajorId() == null ? null : majorInfoService.getById(student.getMajorId());
        if (majorInfo != null) {
            vo.setMajorName(majorInfo.getMajorName());
        }
    }

    private BigDecimal defaultScore(BigDecimal score) {
        return Objects.requireNonNullElse(score, ZERO);
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
