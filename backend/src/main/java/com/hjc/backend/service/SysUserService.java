package com.hjc.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateSysUserRequest;
import com.hjc.backend.dto.ResetSysUserPasswordRequest;
import com.hjc.backend.dto.UpdateSysUserRequest;
import com.hjc.backend.entity.SysUser;
import com.hjc.backend.vo.SysUserVO;

public interface SysUserService extends IService<SysUser> {

    PageResult<SysUserVO> pageQuery(Long pageNum, Long pageSize, String keyword, Integer status, Long roleId);

    SysUserVO getDetail(Long id);

    SysUserVO create(CreateSysUserRequest request);

    SysUserVO update(Long id, UpdateSysUserRequest request);

    SysUserVO resetPassword(Long id, ResetSysUserPasswordRequest request);

    void deleteById(Long id);
}
