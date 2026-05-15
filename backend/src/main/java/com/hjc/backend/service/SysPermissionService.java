package com.hjc.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateSysPermissionRequest;
import com.hjc.backend.dto.SysPermissionPageRequest;
import com.hjc.backend.dto.UpdateSysPermissionRequest;
import com.hjc.backend.entity.SysPermission;
import com.hjc.backend.vo.CurrentUserPermissionVO;
import com.hjc.backend.vo.SysPermissionVO;

import java.util.List;
import java.util.Set;

public interface SysPermissionService extends IService<SysPermission> {

    PageResult<SysPermissionVO> pageQuery(SysPermissionPageRequest request);

    List<SysPermissionVO> tree();

    SysPermissionVO getDetail(Long id);

    SysPermissionVO create(CreateSysPermissionRequest request);

    SysPermissionVO update(Long id, UpdateSysPermissionRequest request);

    void deleteById(Long id);

    Set<String> listPermissionCodesByRoleId(Long roleId);

    CurrentUserPermissionVO getCurrentUserPermissions();

    boolean hasPermission(Long roleId, String roleCode, String permissionCode);

    void requirePermission(String permissionCode);
}
