package com.hjc.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateMajorInfoRequest;
import com.hjc.backend.dto.UpdateMajorInfoRequest;
import com.hjc.backend.entity.MajorInfo;
import com.hjc.backend.vo.MajorInfoVO;

public interface MajorInfoService extends IService<MajorInfo> {

    PageResult<MajorInfoVO> pageQuery(Long pageNum, Long pageSize, String keyword, Integer status);

    MajorInfoVO getDetail(Long id);

    MajorInfoVO create(CreateMajorInfoRequest request);

    MajorInfoVO update(Long id, UpdateMajorInfoRequest request);

    void deleteById(Long id);
}
