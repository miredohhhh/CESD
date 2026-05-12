package com.hjc.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateStudentRequest;
import com.hjc.backend.dto.UpdateStudentRequest;
import com.hjc.backend.entity.Student;
import com.hjc.backend.vo.StudentVO;

public interface StudentService extends IService<Student> {

    PageResult<StudentVO> pageQuery(Long pageNum, Long pageSize, String keyword, Integer status, String grade, Long majorId, Long classId);

    StudentVO getDetail(Long id);

    StudentVO create(CreateStudentRequest request);

    StudentVO update(Long id, UpdateStudentRequest request);

    void deleteById(Long id);
}
