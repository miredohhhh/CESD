package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.common.ResultCode;
import com.hjc.backend.dto.CreateStudentRequest;
import com.hjc.backend.dto.UpdateStudentRequest;
import com.hjc.backend.entity.ClassInfo;
import com.hjc.backend.entity.Student;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.mapper.StudentMapper;
import com.hjc.backend.service.ClassInfoService;
import com.hjc.backend.service.MajorInfoService;
import com.hjc.backend.service.StudentService;
import com.hjc.backend.service.SysUserService;
import com.hjc.backend.vo.StudentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    private final SysUserService sysUserService;

    private final MajorInfoService majorInfoService;

    private final ClassInfoService classInfoService;

    @Override
    public PageResult<StudentVO> pageQuery(Long pageNum, Long pageSize, String keyword, Integer status, String grade, Long majorId, Long classId) {
        Page<Student> page = new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize));
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Student::getStatus, status)
                .eq(StringUtils.hasText(grade), Student::getGrade, grade)
                .eq(majorId != null, Student::getMajorId, majorId)
                .eq(classId != null, Student::getClassId, classId)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Student::getStudentNo, keyword)
                        .or()
                        .like(Student::getName, keyword)
                        .or()
                        .like(Student::getPhone, keyword)
                        .or()
                        .like(Student::getEmail, keyword))
                .orderByDesc(Student::getId);
        Page<Student> result = page(page, wrapper);
        List<StudentVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public StudentVO getDetail(Long id) {
        return toVO(getExisting(id));
    }

    @Override
    public StudentVO create(CreateStudentRequest request) {
        checkUserExists(request.getUserId());
        checkMajorExists(request.getMajorId());
        ClassInfo classInfo = getExistingClass(request.getClassId());
        checkMajorAndClassMatch(request.getMajorId(), classInfo);
        checkUserIdUnique(request.getUserId(), null);
        checkStudentNoUnique(request.getStudentNo(), null);
        Student entity = new Student();
        entity.setUserId(request.getUserId());
        entity.setStudentNo(request.getStudentNo());
        entity.setName(request.getName());
        if (request.getGender() != null) {
            entity.setGender(request.getGender());
        }
        entity.setGrade(request.getGrade());
        entity.setMajorId(request.getMajorId());
        entity.setClassId(request.getClassId());
        if (request.getPhone() != null) {
            entity.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            entity.setEmail(request.getEmail());
        }
        entity.setStatus(request.getStatus());
        save(entity);
        return getDetail(entity.getId());
    }

    @Override
    public StudentVO update(Long id, UpdateStudentRequest request) {
        Student entity = getExisting(id);
        Long finalUserId = request.getUserId() == null ? entity.getUserId() : request.getUserId();
        Long finalMajorId = request.getMajorId() == null ? entity.getMajorId() : request.getMajorId();
        Long finalClassId = request.getClassId() == null ? entity.getClassId() : request.getClassId();

        checkUserExists(finalUserId);
        checkMajorExists(finalMajorId);
        ClassInfo classInfo = getExistingClass(finalClassId);
        checkMajorAndClassMatch(finalMajorId, classInfo);
        checkUserIdUnique(finalUserId, id);

        if (StringUtils.hasText(request.getStudentNo())) {
            checkStudentNoUnique(request.getStudentNo(), id);
            entity.setStudentNo(request.getStudentNo());
        }
        entity.setUserId(finalUserId);
        if (StringUtils.hasText(request.getName())) {
            entity.setName(request.getName());
        }
        entity.setGender(request.getGender());
        if (StringUtils.hasText(request.getGrade())) {
            entity.setGrade(request.getGrade());
        }
        entity.setMajorId(finalMajorId);
        entity.setClassId(finalClassId);
        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        updateById(entity);
        return getDetail(id);
    }

    @Override
    public void deleteById(Long id) {
        getExisting(id);
        // TODO Check material applications, score summary, and score category summary before deleting a student.
        removeById(id);
    }

    private Student getExisting(Long id) {
        Student entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "学生不存在");
        }
        return entity;
    }

    private void checkUserExists(Long userId) {
        if (sysUserService.getById(userId) == null) {
            throw new BusinessException("用户不存在或已删除");
        }
    }

    private void checkMajorExists(Long majorId) {
        if (majorInfoService.getById(majorId) == null) {
            throw new BusinessException("专业不存在或已删除");
        }
    }

    private ClassInfo getExistingClass(Long classId) {
        ClassInfo classInfo = classInfoService.getById(classId);
        if (classInfo == null) {
            throw new BusinessException("班级不存在或已删除");
        }
        return classInfo;
    }

    private void checkMajorAndClassMatch(Long majorId, ClassInfo classInfo) {
        if (!majorId.equals(classInfo.getMajorId())) {
            throw new BusinessException("学生对应专业与班级所属专业不一致");
        }
    }

    private void checkUserIdUnique(Long userId, Long excludeId) {
        long count = lambdaQuery()
                .eq(Student::getUserId, userId)
                .ne(excludeId != null, Student::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BusinessException("用户已绑定学生信息");
        }
    }

    private void checkStudentNoUnique(String studentNo, Long excludeId) {
        long count = lambdaQuery()
                .eq(Student::getStudentNo, studentNo)
                .ne(excludeId != null, Student::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BusinessException("学号已存在");
        }
    }

    private StudentVO toVO(Student entity) {
        StudentVO vo = new StudentVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setStudentNo(entity.getStudentNo());
        vo.setName(entity.getName());
        vo.setGender(entity.getGender());
        vo.setGrade(entity.getGrade());
        vo.setMajorId(entity.getMajorId());
        vo.setClassId(entity.getClassId());
        vo.setPhone(entity.getPhone());
        vo.setEmail(entity.getEmail());
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
}
