package com.hjc.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateEvaluationItemRequest;
import com.hjc.backend.dto.UpdateEvaluationItemRequest;
import com.hjc.backend.entity.EvaluationItem;
import com.hjc.backend.vo.EvaluationItemVO;

public interface EvaluationItemService extends IService<EvaluationItem> {

    PageResult<EvaluationItemVO> pageQuery(Long pageNum, Long pageSize, String keyword, Integer status);

    EvaluationItemVO getDetail(Long id);

    EvaluationItemVO create(CreateEvaluationItemRequest request);

    EvaluationItemVO update(Long id, UpdateEvaluationItemRequest request);

    void deleteById(Long id);
}
