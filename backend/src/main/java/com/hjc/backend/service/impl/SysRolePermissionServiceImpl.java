package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.common.ResultCode;
import com.hjc.backend.dto.AssignRolePermissionsRequest;
import com.hjc.backend.entity.SysPermission;
import com.hjc.backend.entity.SysRole;
import com.hjc.backend.entity.SysRolePermission;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.mapper.SysRolePermissionMapper;
import com.hjc.backend.service.SysPermissionService;
import com.hjc.backend.service.SysRolePermissionService;
import com.hjc.backend.service.SysRoleService;
import com.hjc.backend.vo.RolePermissionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> implements SysRolePermissionService {

    private final SysRoleService sysRoleService;

    private final SysPermissionService sysPermissionService;

    @Override
    public RolePermissionVO getRolePermissions(Long roleId) {
        checkRoleExists(roleId);
        List<SysRolePermission> relations = lambdaQuery()
                .eq(SysRolePermission::getRoleId, roleId)
                .list();
        List<Long> permissionIds = relations.stream()
                .map(SysRolePermission::getPermissionId)
                .distinct()
                .toList();
        RolePermissionVO vo = new RolePermissionVO();
        vo.setRoleId(roleId);
        vo.setPermissionIds(permissionIds);
        if (!permissionIds.isEmpty()) {
            vo.setPermissionCodes(sysPermissionService.listByIds(permissionIds).stream()
                    .map(SysPermission::getPermissionCode)
                    .filter(code -> code != null && !code.isBlank())
                    .toList());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RolePermissionVO assignRolePermissions(Long roleId, AssignRolePermissionsRequest request) {
        checkRoleExists(roleId);
        List<Long> permissionIds = normalizePermissionIds(request.getPermissionIds());
        checkPermissionsExist(permissionIds);
        lambdaUpdate().eq(SysRolePermission::getRoleId, roleId).remove();
        if (!permissionIds.isEmpty()) {
            List<SysRolePermission> relations = permissionIds.stream().map(permissionId -> {
                SysRolePermission relation = new SysRolePermission();
                relation.setRoleId(roleId);
                relation.setPermissionId(permissionId);
                return relation;
            }).toList();
            saveBatch(relations);
        }
        return getRolePermissions(roleId);
    }

    private void checkRoleExists(Long roleId) {
        if (roleId == null) {
            throw new BusinessException("roleId must not be null");
        }
        SysRole role = sysRoleService.getById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Role does not exist");
        }
    }

    private List<Long> normalizePermissionIds(List<Long> permissionIds) {
        if (permissionIds == null) {
            return List.of();
        }
        Set<Long> distinct = new LinkedHashSet<>();
        for (Long permissionId : permissionIds) {
            if (permissionId != null) {
                distinct.add(permissionId);
            }
        }
        return new ArrayList<>(distinct);
    }

    private void checkPermissionsExist(List<Long> permissionIds) {
        if (permissionIds.isEmpty()) {
            return;
        }
        long count = sysPermissionService.lambdaQuery()
                .in(SysPermission::getId, permissionIds)
                .count();
        if (count != permissionIds.size()) {
            throw new BusinessException("Permission contains non-existing id");
        }
    }
}
