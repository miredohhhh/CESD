package com.hjc.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.OperationLogPageRequest;
import com.hjc.backend.entity.OperationLog;
import com.hjc.backend.vo.OperationLogVO;

public interface OperationLogService extends IService<OperationLog> {

    String RESULT_SUCCESS = "SUCCESS";

    String RESULT_FAIL = "FAIL";

    PageResult<OperationLogVO> pageQuery(OperationLogPageRequest request);

    OperationLogVO getDetail(Long id);

    void recordSuccess(String module, String type, String description, String params);

    void recordFailure(String module, String type, String description, String params, String errorMessage);
}
