package com.hjc.backend.controller;

import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateStudentRequest;
import com.hjc.backend.dto.UpdateStudentRequest;
import com.hjc.backend.service.StudentService;
import com.hjc.backend.vo.StudentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "学生管理", description = "学生基础 CRUD")
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "分页查询学生")
    @GetMapping("/page")
    public ApiResponse<PageResult<StudentVO>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) Long classId) {
        return ApiResponse.success(studentService.pageQuery(pageNum, pageSize, keyword, status, grade, majorId, classId));
    }

    @Operation(summary = "查询学生详情")
    @GetMapping("/{id}")
    public ApiResponse<StudentVO> detail(@PathVariable Long id) {
        return ApiResponse.success(studentService.getDetail(id));
    }

    @Operation(summary = "新增学生")
    @PostMapping
    public ApiResponse<StudentVO> create(@Valid @RequestBody CreateStudentRequest request) {
        return ApiResponse.success(studentService.create(request));
    }

    @Operation(summary = "修改学生")
    @PutMapping("/{id}")
    public ApiResponse<StudentVO> update(@PathVariable Long id, @Valid @RequestBody UpdateStudentRequest request) {
        return ApiResponse.success(studentService.update(id, request));
    }

    @Operation(summary = "删除学生")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        studentService.deleteById(id);
        return ApiResponse.success();
    }
}
