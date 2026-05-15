package com.hjc.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateSysRoleRequest;
import com.hjc.backend.dto.UpdateSysRoleRequest;
import com.hjc.backend.entity.SysRole;
import com.hjc.backend.vo.SysRoleVO;

public interface SysRoleService extends IService<SysRole> {

    PageResult<SysRoleVO> pageQuery(Long pageNum, Long pageSize, String keyword, Integer status);

    SysRoleVO getDetail(Long id);

    SysRoleVO create(CreateSysRoleRequest request);

    SysRoleVO update(Long id, UpdateSysRoleRequest request);

    void deleteById(Long id);
}
