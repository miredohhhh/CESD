package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.common.ResultCode;
import com.hjc.backend.dto.CreateMaterialAttachmentRequest;
import com.hjc.backend.dto.UpdateMaterialAttachmentRequest;
import com.hjc.backend.entity.MaterialAttachment;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.mapper.MaterialAttachmentMapper;
import com.hjc.backend.service.MaterialApplicationService;
import com.hjc.backend.service.MaterialAttachmentService;
import com.hjc.backend.vo.MaterialAttachmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialAttachmentServiceImpl extends ServiceImpl<MaterialAttachmentMapper, MaterialAttachment> implements MaterialAttachmentService {

    private final MaterialApplicationService materialApplicationService;

    @Override
    public PageResult<MaterialAttachmentVO> pageQuery(Long pageNum, Long pageSize, Long materialId, String fileType, String keyword) {
        Page<MaterialAttachment> page = new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize));
        LambdaQueryWrapper<MaterialAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(materialId != null, MaterialAttachment::getMaterialId, materialId)
                .eq(StringUtils.hasText(fileType), MaterialAttachment::getFileType, fileType)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(MaterialAttachment::getOriginalName, keyword)
                        .or()
                        .like(MaterialAttachment::getStoredName, keyword)
                        .or()
                        .like(MaterialAttachment::getFilePath, keyword)
                        .or()
                        .like(MaterialAttachment::getFileUrl, keyword))
                .orderByDesc(MaterialAttachment::getId);
        Page<MaterialAttachment> result = page(page, wrapper);
        List<MaterialAttachmentVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public MaterialAttachmentVO getDetail(Long id) {
        return toVO(getExisting(id));
    }

    @Override
    public MaterialAttachmentVO create(CreateMaterialAttachmentRequest request) {
        checkMaterialExists(request.getMaterialId());
        checkFileSize(request.getFileSize());
        MaterialAttachment entity = new MaterialAttachment();
        entity.setMaterialId(request.getMaterialId());
        entity.setOriginalName(request.getOriginalName());
        entity.setStoredName(request.getStoredName());
        entity.setFilePath(request.getFilePath());
        entity.setFileUrl(request.getFileUrl());
        entity.setFileType(request.getFileType());
        entity.setFileSize(request.getFileSize());
        save(entity);
        return getDetail(entity.getId());
    }

    @Override
    public MaterialAttachmentVO update(Long id, UpdateMaterialAttachmentRequest request) {
        MaterialAttachment entity = getExisting(id);
        if (request.getMaterialId() != null) {
            checkMaterialExists(request.getMaterialId());
            entity.setMaterialId(request.getMaterialId());
        }
        if (StringUtils.hasText(request.getOriginalName())) {
            entity.setOriginalName(request.getOriginalName());
        }
        if (StringUtils.hasText(request.getStoredName())) {
            entity.setStoredName(request.getStoredName());
        }
        if (StringUtils.hasText(request.getFilePath())) {
            entity.setFilePath(request.getFilePath());
        }
        if (request.getFileUrl() != null) {
            entity.setFileUrl(request.getFileUrl());
        }
        if (request.getFileType() != null) {
            entity.setFileType(request.getFileType());
        }
        if (request.getFileSize() != null) {
            checkFileSize(request.getFileSize());
            entity.setFileSize(request.getFileSize());
        }
        updateById(entity);
        return getDetail(id);
    }

    @Override
    public void deleteById(Long id) {
        getExisting(id);
        removeById(id);
    }

    private MaterialAttachment getExisting(Long id) {
        MaterialAttachment entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Material attachment does not exist");
        }
        return entity;
    }

    private void checkMaterialExists(Long materialId) {
        if (materialApplicationService.getById(materialId) == null) {
            throw new BusinessException("Material application does not exist or has been deleted");
        }
    }

    private void checkFileSize(Long fileSize) {
        if (fileSize != null && fileSize < 0) {
            throw new BusinessException("fileSize must be greater than or equal to 0");
        }
    }

    private MaterialAttachmentVO toVO(MaterialAttachment entity) {
        MaterialAttachmentVO vo = new MaterialAttachmentVO();
        vo.setId(entity.getId());
        vo.setMaterialId(entity.getMaterialId());
        vo.setOriginalName(entity.getOriginalName());
        vo.setStoredName(entity.getStoredName());
        vo.setFilePath(entity.getFilePath());
        vo.setFileUrl(entity.getFileUrl());
        vo.setFileType(entity.getFileType());
        vo.setFileSize(entity.getFileSize());
        vo.setUploadTime(entity.getUploadTime());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
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
