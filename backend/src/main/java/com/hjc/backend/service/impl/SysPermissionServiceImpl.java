package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.common.ResultCode;
import com.hjc.backend.dto.CreateSysPermissionRequest;
import com.hjc.backend.dto.SysPermissionPageRequest;
import com.hjc.backend.dto.UpdateSysPermissionRequest;
import com.hjc.backend.entity.SysPermission;
import com.hjc.backend.entity.SysRolePermission;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.mapper.SysPermissionMapper;
import com.hjc.backend.mapper.SysRolePermissionMapper;
import com.hjc.backend.security.CurrentUserUtils;
import com.hjc.backend.security.PermissionCode;
import com.hjc.backend.service.SysPermissionService;
import com.hjc.backend.vo.CurrentUserPermissionVO;
import com.hjc.backend.vo.MenuPermissionVO;
import com.hjc.backend.vo.SysPermissionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    private static final Integer ENABLED = 1;

    private final SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public PageResult<SysPermissionVO> pageQuery(SysPermissionPageRequest request) {
        Page<SysPermission> page = new Page<>(normalizePageNum(request.getPageNo() != null ? request.getPageNo() : request.getPageNum()), normalizePageSize(request.getPageSize()));
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(request.getPermissionType()), SysPermission::getPermissionType, request.getPermissionType())
                .eq(request.getStatus() != null, SysPermission::getStatus, request.getStatus())
                .eq(request.getParentId() != null, SysPermission::getParentId, request.getParentId())
                .and(StringUtils.hasText(request.getKeyword()), w -> w
                        .like(SysPermission::getPermissionName, request.getKeyword())
                        .or()
                        .like(SysPermission::getPermissionCode, request.getKeyword()))
                .orderByAsc(SysPermission::getSortOrder)
                .orderByAsc(SysPermission::getId);
        Page<SysPermission> result = page(page, wrapper);
        List<SysPermissionVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<SysPermissionVO> tree() {
        List<SysPermissionVO> nodes = lambdaQuery()
                .orderByAsc(SysPermission::getSortOrder)
                .orderByAsc(SysPermission::getId)
                .list()
                .stream()
                .map(this::toVO)
                .toList();
        return buildTree(nodes);
    }

    @Override
    public SysPermissionVO getDetail(Long id) {
        return toVO(getExisting(id));
    }

    @Override
    public SysPermissionVO create(CreateSysPermissionRequest request) {
        checkPermissionCodeUnique(request.getPermissionCode(), null);
        checkParentExists(request.getParentId());
        SysPermission entity = new SysPermission();
        fillEntity(entity, request.getPermissionName(), request.getPermissionCode(), request.getPermissionType(),
                request.getParentId(), request.getRoutePath(), request.getComponentPath(), request.getApiPath(),
                request.getHttpMethod(), request.getIcon(), request.getSortOrder(), request.getStatus(), request.getRemark());
        save(entity);
        return getDetail(entity.getId());
    }

    @Override
    public SysPermissionVO update(Long id, UpdateSysPermissionRequest request) {
        getExisting(id);
        checkPermissionCodeUnique(request.getPermissionCode(), id);
        if (id.equals(request.getParentId())) {
            throw new BusinessException("parentId cannot be itself");
        }
        checkParentExists(request.getParentId());
        SysPermission entity = new SysPermission();
        entity.setId(id);
        fillEntity(entity, request.getPermissionName(), request.getPermissionCode(), request.getPermissionType(),
                request.getParentId(), request.getRoutePath(), request.getComponentPath(), request.getApiPath(),
                request.getHttpMethod(), request.getIcon(), request.getSortOrder(), request.getStatus(), request.getRemark());
        updateById(entity);
        return getDetail(id);
    }

    @Override
    public void deleteById(Long id) {
        getExisting(id);
        long childCount = lambdaQuery().eq(SysPermission::getParentId, id).count();
        if (childCount > 0) {
            throw new BusinessException("Permission has child nodes and cannot be deleted");
        }
        long bindCount = sysRolePermissionMapper.selectCount(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getPermissionId, id));
        if (bindCount > 0) {
            throw new BusinessException("Permission is assigned to roles and cannot be deleted");
        }
        removeById(id);
    }

    @Override
    public Set<String> listPermissionCodesByRoleId(Long roleId) {
        if (roleId == null) {
            return Set.of();
        }
        List<SysRolePermission> relations = sysRolePermissionMapper.selectList(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId)
        );
        if (relations.isEmpty()) {
            return Set.of();
        }
        List<Long> permissionIds = relations.stream().map(SysRolePermission::getPermissionId).distinct().toList();
        return listByIds(permissionIds).stream()
                .filter(permission -> ENABLED.equals(permission.getStatus()))
                .map(SysPermission::getPermissionCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public CurrentUserPermissionVO getCurrentUserPermissions() {
        CurrentUserUtils.requireUserId();
        String roleCode = CurrentUserUtils.getRoleCode();
        Long roleId = CurrentUserUtils.getContext() == null ? null : CurrentUserUtils.getContext().getRoleId();
        Set<String> codes = resolvePermissionCodes(roleId, roleCode);
        CurrentUserPermissionVO vo = new CurrentUserPermissionVO();
        vo.setUserId(CurrentUserUtils.requireUserId());
        vo.setRoleCode(roleCode);
        vo.setPermissionCodes(new ArrayList<>(codes));
        List<SysPermission> permissions = listEnabledPermissionsForRole(roleId, roleCode);
        vo.setMenus(buildMenuTree(permissions.stream()
                .filter(permission -> "MENU".equals(permission.getPermissionType()))
                .toList()));
        vo.setButtons(permissions.stream()
                .filter(permission -> "BUTTON".equals(permission.getPermissionType()))
                .map(SysPermission::getPermissionCode)
                .filter(StringUtils::hasText)
                .toList());
        return vo;
    }

    @Override
    public boolean hasPermission(Long roleId, String roleCode, String permissionCode) {
        if (!StringUtils.hasText(permissionCode)) {
            return true;
        }
        if (CurrentUserUtils.ROLE_ADMIN.equalsIgnoreCase(roleCode)) {
            return true;
        }
        Set<String> codes = listPermissionCodesByRoleId(roleId);
        if (!codes.isEmpty()) {
            return codes.contains(permissionCode);
        }
        return fallbackCodes(roleCode).contains(permissionCode);
    }

    @Override
    public void requirePermission(String permissionCode) {
        CurrentUserUtils.requireUserId();
        Long roleId = CurrentUserUtils.getContext() == null ? null : CurrentUserUtils.getContext().getRoleId();
        if (!hasPermission(roleId, CurrentUserUtils.getRoleCode(), permissionCode)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "No permission: " + permissionCode);
        }
    }

    private Set<String> resolvePermissionCodes(Long roleId, String roleCode) {
        if (CurrentUserUtils.ROLE_ADMIN.equalsIgnoreCase(roleCode)) {
            Set<String> allCodes = lambdaQuery()
                    .eq(SysPermission::getStatus, ENABLED)
                    .list()
                    .stream()
                    .map(SysPermission::getPermissionCode)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            return allCodes.isEmpty() ? fallbackCodes(roleCode) : allCodes;
        }
        Set<String> codes = listPermissionCodesByRoleId(roleId);
        return codes.isEmpty() ? fallbackCodes(roleCode) : codes;
    }

    private List<SysPermission> listEnabledPermissionsForRole(Long roleId, String roleCode) {
        if (CurrentUserUtils.ROLE_ADMIN.equalsIgnoreCase(roleCode)) {
            return lambdaQuery().eq(SysPermission::getStatus, ENABLED).list();
        }
        Set<String> codes = listPermissionCodesByRoleId(roleId);
        if (codes.isEmpty()) {
            codes = fallbackCodes(roleCode);
        }
        if (codes.isEmpty()) {
            return List.of();
        }
        return lambdaQuery()
                .eq(SysPermission::getStatus, ENABLED)
                .in(SysPermission::getPermissionCode, codes)
                .list();
    }

    private Set<String> fallbackCodes(String roleCode) {
        Set<String> codes = new LinkedHashSet<>();
        if (CurrentUserUtils.ROLE_STUDENT.equalsIgnoreCase(roleCode)) {
            codes.add(PermissionCode.STUDENT_APPLICATION_VIEW);
            codes.add(PermissionCode.STUDENT_APPLICATION_CREATE);
            codes.add(PermissionCode.STUDENT_APPLICATION_SUBMIT);
            codes.add(PermissionCode.STUDENT_APPLICATION_WITHDRAW);
            codes.add(PermissionCode.STUDENT_SCORE_VIEW);
        } else if (CurrentUserUtils.ROLE_AUDITOR.equalsIgnoreCase(roleCode) || CurrentUserUtils.ROLE_REVIEWER.equalsIgnoreCase(roleCode)) {
            codes.add(PermissionCode.AUDIT_PENDING_VIEW);
            codes.add(PermissionCode.AUDIT_APPLICATION_APPROVE);
            codes.add(PermissionCode.AUDIT_APPLICATION_REJECT);
        } else if (CurrentUserUtils.ROLE_ADMIN.equalsIgnoreCase(roleCode)) {
            codes.add("*");
        }
        return codes;
    }

    private List<SysPermissionVO> buildTree(List<SysPermissionVO> nodes) {
        Map<Long, SysPermissionVO> map = nodes.stream().collect(Collectors.toMap(SysPermissionVO::getId, Function.identity()));
        List<SysPermissionVO> roots = new ArrayList<>();
        for (SysPermissionVO node : nodes) {
            if (node.getParentId() != null && map.containsKey(node.getParentId())) {
                map.get(node.getParentId()).getChildren().add(node);
            } else {
                roots.add(node);
            }
        }
        sortTree(roots);
        return roots;
    }

    private List<MenuPermissionVO> buildMenuTree(List<SysPermission> permissions) {
        Map<Long, MenuPermissionVO> map = permissions.stream()
                .map(this::toMenuVO)
                .collect(Collectors.toMap(MenuPermissionVO::getId, Function.identity()));
        List<MenuPermissionVO> roots = new ArrayList<>();
        for (MenuPermissionVO node : map.values()) {
            SysPermission source = permissions.stream().filter(item -> item.getId().equals(node.getId())).findFirst().orElse(null);
            Long parentId = source == null ? null : source.getParentId();
            if (parentId != null && map.containsKey(parentId)) {
                map.get(parentId).getChildren().add(node);
            } else {
                roots.add(node);
            }
        }
        sortMenuTree(roots);
        return roots;
    }

    private void sortTree(List<SysPermissionVO> nodes) {
        nodes.sort(Comparator.comparing(SysPermissionVO::getSortOrder, Comparator.nullsLast(Integer::compareTo)).thenComparing(SysPermissionVO::getId));
        nodes.forEach(node -> sortTree(node.getChildren()));
    }

    private void sortMenuTree(List<MenuPermissionVO> nodes) {
        nodes.sort(Comparator.comparing(MenuPermissionVO::getSortOrder, Comparator.nullsLast(Integer::compareTo)).thenComparing(MenuPermissionVO::getId));
        nodes.forEach(node -> sortMenuTree(node.getChildren()));
    }

    private SysPermission getExisting(Long id) {
        SysPermission entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Permission does not exist");
        }
        return entity;
    }

    private void checkPermissionCodeUnique(String permissionCode, Long excludeId) {
        long count = lambdaQuery()
                .eq(SysPermission::getPermissionCode, permissionCode)
                .ne(excludeId != null, SysPermission::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BusinessException("Permission code already exists");
        }
    }

    private void checkParentExists(Long parentId) {
        if (parentId != null && getById(parentId) == null) {
            throw new BusinessException("Parent permission does not exist");
        }
    }

    private void fillEntity(SysPermission entity, String permissionName, String permissionCode, String permissionType,
                            Long parentId, String routePath, String componentPath, String apiPath, String httpMethod,
                            String icon, Integer sortOrder, Integer status, String remark) {
        entity.setPermissionName(permissionName);
        entity.setPermissionCode(permissionCode);
        entity.setPermissionType(permissionType);
        entity.setParentId(parentId);
        entity.setRoutePath(routePath);
        entity.setComponentPath(componentPath);
        entity.setApiPath(apiPath);
        entity.setHttpMethod(StringUtils.hasText(httpMethod) ? httpMethod.toUpperCase() : null);
        entity.setIcon(icon);
        entity.setSortOrder(sortOrder == null ? 0 : sortOrder);
        entity.setStatus(status == null ? ENABLED : status);
        entity.setRemark(remark);
    }

    private SysPermissionVO toVO(SysPermission entity) {
        SysPermissionVO vo = new SysPermissionVO();
        vo.setId(entity.getId());
        vo.setPermissionName(entity.getPermissionName());
        vo.setPermissionCode(entity.getPermissionCode());
        vo.setPermissionType(entity.getPermissionType());
        vo.setParentId(entity.getParentId());
        vo.setRoutePath(entity.getRoutePath());
        vo.setComponentPath(entity.getComponentPath());
        vo.setApiPath(entity.getApiPath());
        vo.setHttpMethod(entity.getHttpMethod());
        vo.setIcon(entity.getIcon());
        vo.setSortOrder(entity.getSortOrder());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private MenuPermissionVO toMenuVO(SysPermission entity) {
        MenuPermissionVO vo = new MenuPermissionVO();
        vo.setId(entity.getId());
        vo.setPermissionName(entity.getPermissionName());
        vo.setPermissionCode(entity.getPermissionCode());
        vo.setRoutePath(entity.getRoutePath());
        vo.setComponentPath(entity.getComponentPath());
        vo.setIcon(entity.getIcon());
        vo.setSortOrder(entity.getSortOrder());
        return vo;
    }

    private long normalizePageNum(Long pageNum) {
        return pageNum == null || pageNum < 1 ? 1L : pageNum;
    }

    private long normalizePageSize(Long pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10L;
        }
        return Math.min(pageSize, 100L);
    }
}
