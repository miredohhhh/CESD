package com.hjc.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateSystemConfigRequest;
import com.hjc.backend.dto.UpdateSystemConfigRequest;
import com.hjc.backend.entity.SystemConfig;
import com.hjc.backend.vo.SystemConfigVO;

public interface SystemConfigService extends IService<SystemConfig> {

    PageResult<SystemConfigVO> pageQuery(Long pageNum, Long pageSize, String keyword, Integer status);

    SystemConfigVO getDetail(Long id);

    SystemConfigVO create(CreateSystemConfigRequest request);

    SystemConfigVO update(Long id, UpdateSystemConfigRequest request);

    void deleteById(Long id);
}
