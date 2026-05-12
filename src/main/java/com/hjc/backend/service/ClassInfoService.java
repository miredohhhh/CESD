package com.hjc.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateClassInfoRequest;
import com.hjc.backend.dto.UpdateClassInfoRequest;
import com.hjc.backend.entity.ClassInfo;
import com.hjc.backend.vo.ClassInfoVO;

public interface ClassInfoService extends IService<ClassInfo> {

    PageResult<ClassInfoVO> pageQuery(Long pageNum, Long pageSize, String keyword, Integer status);

    ClassInfoVO getDetail(Long id);

    ClassInfoVO create(CreateClassInfoRequest request);

    ClassInfoVO update(Long id, UpdateClassInfoRequest request);

    void deleteById(Long id);
}
