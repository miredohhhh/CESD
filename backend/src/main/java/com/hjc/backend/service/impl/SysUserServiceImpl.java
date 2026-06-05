package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.common.ResultCode;
import com.hjc.backend.dto.CreateSysUserRequest;
import com.hjc.backend.dto.ResetSysUserPasswordRequest;
import com.hjc.backend.dto.UpdateSysUserRequest;
import com.hjc.backend.entity.SysUser;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.mapper.SysUserMapper;
import com.hjc.backend.service.OperationLogService;
import com.hjc.backend.service.SysRoleService;
import com.hjc.backend.service.SysUserService;
import com.hjc.backend.vo.SysUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysRoleService sysRoleService;

    private final PasswordEncoder passwordEncoder;

    private final OperationLogService operationLogService;

    @Override
    public PageResult<SysUserVO> pageQuery(Long pageNum, Long pageSize, String keyword, Integer status, Long roleId) {
        Page<SysUser> page = new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize));
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, SysUser::getStatus, status)
                .eq(roleId != null, SysUser::getRoleId, roleId)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(SysUser::getUsername, keyword)
                        .or()
                        .like(SysUser::getRealName, keyword)
                        .or()
                        .like(SysUser::getPhone, keyword)
                        .or()
                        .like(SysUser::getEmail, keyword))
                .orderByDesc(SysUser::getId);
        Page<SysUser> result = page(page, wrapper);
        List<SysUserVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public SysUserVO getDetail(Long id) {
        return toVO(getExisting(id));
    }

    @Override
    public SysUserVO create(CreateSysUserRequest request) {
        checkUsernameUnique(request.getUsername(), null);
        checkRoleExists(request.getRoleId());
        SysUser entity = new SysUser();
        entity.setUsername(request.getUsername());
        entity.setPasswordHash(encodePassword(request.getPasswordHash()));
        entity.setRealName(request.getRealName());
        entity.setRoleId(request.getRoleId());
        if (request.getPhone() != null) {
            entity.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            entity.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) {
            entity.setAvatar(request.getAvatar());
        }
        entity.setStatus(request.getStatus());
        save(entity);
        operationLogService.recordSuccess("USER", "CREATE", "Create user " + entity.getUsername(), "userId=" + entity.getId());
        return getDetail(entity.getId());
    }

    @Override
    public SysUserVO update(Long id, UpdateSysUserRequest request) {
        SysUser entity = getExisting(id);
        if (StringUtils.hasText(request.getUsername())) {
            checkUsernameUnique(request.getUsername(), id);
            entity.setUsername(request.getUsername());
        }
        if (StringUtils.hasText(request.getPasswordHash())) {
            entity.setPasswordHash(encodePassword(request.getPasswordHash()));
        }
        if (StringUtils.hasText(request.getRealName())) {
            entity.setRealName(request.getRealName());
        }
        if (request.getRoleId() != null) {
            checkRoleExists(request.getRoleId());
            entity.setRoleId(request.getRoleId());
        }
        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
        entity.setAvatar(request.getAvatar());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        updateById(entity);
        operationLogService.recordSuccess("USER", "UPDATE", "Update user " + entity.getUsername(), "userId=" + id);
        return getDetail(id);
    }

    @Override
    public SysUserVO resetPassword(Long id, ResetSysUserPasswordRequest request) {
        SysUser entity = getExisting(id);
        entity.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        updateById(entity);
        operationLogService.recordSuccess("USER", "RESET_PASSWORD", "Reset user password " + entity.getUsername(), "userId=" + id);
        return getDetail(id);
    }

    @Override
    public void deleteById(Long id) {
        getExisting(id);
        // TODO Check related student and review records before deleting a user.
        removeById(id);
        operationLogService.recordSuccess("USER", "DELETE", "Delete user", "userId=" + id);
    }

    private SysUser getExisting(Long id) {
        SysUser entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return entity;
    }

    private void checkUsernameUnique(String username, Long excludeId) {
        long count = lambdaQuery()
                .eq(SysUser::getUsername, username)
                .ne(excludeId != null, SysUser::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
    }

    private void checkRoleExists(Long roleId) {
        if (sysRoleService.getById(roleId) == null) {
            throw new BusinessException("角色不存在或已删除");
        }
    }

    private SysUserVO toVO(SysUser entity) {
        SysUserVO vo = new SysUserVO();
        vo.setId(entity.getId());
        vo.setUsername(entity.getUsername());
        vo.setRealName(entity.getRealName());
        vo.setRoleId(entity.getRoleId());
        vo.setPhone(entity.getPhone());
        vo.setEmail(entity.getEmail());
        vo.setAvatar(entity.getAvatar());
        vo.setStatus(entity.getStatus());
        vo.setLastLoginTime(entity.getLastLoginTime());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private String encodePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new BusinessException("password must not be blank");
        }
        if (isBcryptHash(password)) {
            return password;
        }
        return passwordEncoder.encode(password);
    }

    private boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
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
