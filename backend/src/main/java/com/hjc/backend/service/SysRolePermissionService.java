package com.hjc.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjc.backend.dto.AssignRolePermissionsRequest;
import com.hjc.backend.entity.SysRolePermission;
import com.hjc.backend.vo.RolePermissionVO;

public interface SysRolePermissionService extends IService<SysRolePermission> {

    RolePermissionVO getRolePermissions(Long roleId);

    RolePermissionVO assignRolePermissions(Long roleId, AssignRolePermissionsRequest request);
}
