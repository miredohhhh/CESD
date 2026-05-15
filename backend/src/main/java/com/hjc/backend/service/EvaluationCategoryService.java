package com.hjc.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateEvaluationCategoryRequest;
import com.hjc.backend.dto.UpdateEvaluationCategoryRequest;
import com.hjc.backend.entity.EvaluationCategory;
import com.hjc.backend.vo.EvaluationCategoryVO;

public interface EvaluationCategoryService extends IService<EvaluationCategory> {

    PageResult<EvaluationCategoryVO> pageQuery(Long pageNum, Long pageSize, String keyword, Integer status);

    EvaluationCategoryVO getDetail(Long id);

    EvaluationCategoryVO create(CreateEvaluationCategoryRequest request);

    EvaluationCategoryVO update(Long id, UpdateEvaluationCategoryRequest request);

    void deleteById(Long id);
}
