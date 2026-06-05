package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.common.ResultCode;
import com.hjc.backend.config.UploadProperties;
import com.hjc.backend.dto.CreateMaterialAttachmentRequest;
import com.hjc.backend.dto.UpdateMaterialAttachmentRequest;
import com.hjc.backend.entity.MaterialApplication;
import com.hjc.backend.entity.MaterialAttachment;
import com.hjc.backend.exception.BusinessException;
import com.hjc.backend.mapper.MaterialAttachmentMapper;
import com.hjc.backend.security.CurrentUserUtils;
import com.hjc.backend.service.MaterialApplicationService;
import com.hjc.backend.service.MaterialAttachmentService;
import com.hjc.backend.service.OperationLogService;
import com.hjc.backend.vo.MaterialAttachmentDownloadResource;
import com.hjc.backend.vo.MaterialAttachmentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialAttachmentServiceImpl extends ServiceImpl<MaterialAttachmentMapper, MaterialAttachment> implements MaterialAttachmentService {

    private static final List<String> EDITABLE_MATERIAL_STATUSES = List.of("DRAFT", "REJECTED", "CANCELLED");

    private final MaterialApplicationService materialApplicationService;

    private final UploadProperties uploadProperties;

    private final OperationLogService operationLogService;

    @Override
    public PageResult<MaterialAttachmentVO> pageQuery(Long pageNum, Long pageSize, Long materialId, String fileType, String keyword) {
        CurrentUserUtils.requireUserId();
        Page<MaterialAttachment> page = new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize));
        LambdaQueryWrapper<MaterialAttachment> wrapper = new LambdaQueryWrapper<>();
        applyViewerScope(wrapper, materialId);
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
        MaterialAttachment attachment = getExisting(id);
        checkViewAccess(attachment);
        return toVO(attachment);
    }

    @Override
    @Transactional
    public MaterialAttachmentVO create(CreateMaterialAttachmentRequest request) {
        MaterialApplication material = getExistingMaterial(request.getMaterialId());
        checkOwnerCanModify(material);
        checkFileSize(request.getFileSize());
        MaterialAttachment entity = new MaterialAttachment();
        entity.setMaterialId(request.getMaterialId());
        entity.setOriginalName(request.getOriginalName());
        entity.setStoredName(request.getStoredName());
        entity.setFilePath(request.getFilePath());
        entity.setFileUrl(request.getFileUrl());
        entity.setFileType(request.getFileType());
        entity.setFileSize(request.getFileSize());
        LocalDateTime now = LocalDateTime.now();
        entity.setUploadTime(now);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        save(entity);
        return getDetail(entity.getId());
    }

    @Override
    @Transactional
    public MaterialAttachmentVO upload(Long materialId, MultipartFile file) {
        MaterialApplication material = getExistingMaterial(materialId);
        checkOwnerCanModify(material);
        validateUploadFile(file);

        String originalName = cleanOriginalFilename(file.getOriginalFilename());
        String extension = getExtension(originalName);
        String storedName = UUID.randomUUID() + "." + extension;
        String relativePath = "material-" + materialId + "/" + storedName;
        Path target = resolveUnderRoot(relativePath);

        try {
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Failed to save uploaded file");
        }

        MaterialAttachment entity = new MaterialAttachment();
        entity.setMaterialId(materialId);
        entity.setOriginalName(originalName);
        entity.setStoredName(storedName);
        entity.setFilePath(relativePath);
        entity.setFileType(extension);
        entity.setFileSize(file.getSize());
        LocalDateTime now = LocalDateTime.now();
        entity.setUploadTime(now);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);

        try {
            save(entity);
            entity.setFileUrl(normalizePrefix(uploadProperties.getAccessUrlPrefix()) + "/" + entity.getId() + "/download");
            updateById(entity);
        } catch (RuntimeException ex) {
            deletePhysicalFileQuietly(target);
            throw ex;
        }
        operationLogService.recordSuccess("ATTACHMENT", "UPLOAD", "Upload material attachment " + originalName, "attachmentId=" + entity.getId() + ",materialId=" + materialId);
        return getDetail(entity.getId());
    }

    @Override
    public MaterialAttachmentDownloadResource getDownloadResource(Long id) {
        MaterialAttachment attachment = getExisting(id);
        checkViewAccess(attachment);
        Path filePath = resolveUnderRoot(attachment.getFilePath());
        if (!Files.isRegularFile(filePath)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Uploaded file does not exist");
        }
        String contentType;
        try {
            contentType = Files.probeContentType(filePath);
        } catch (IOException ex) {
            contentType = null;
        }
        if (!StringUtils.hasText(contentType)) {
            contentType = "application/octet-stream";
        }
        Resource resource = new FileSystemResource(filePath);
        long contentLength;
        try {
            contentLength = Files.size(filePath);
        } catch (IOException ex) {
            contentLength = attachment.getFileSize() == null ? -1L : attachment.getFileSize();
        }
        operationLogService.recordSuccess("ATTACHMENT", "DOWNLOAD", "Download material attachment " + attachment.getOriginalName(), "attachmentId=" + id + ",materialId=" + attachment.getMaterialId());
        return new MaterialAttachmentDownloadResource(resource, attachment.getOriginalName(), contentType, contentLength);
    }

    @Override
    @Transactional
    public MaterialAttachmentVO update(Long id, UpdateMaterialAttachmentRequest request) {
        MaterialAttachment entity = getExisting(id);
        MaterialApplication material = getExistingMaterial(entity.getMaterialId());
        checkOwnerCanModify(material);
        if (request.getMaterialId() != null && !request.getMaterialId().equals(entity.getMaterialId())) {
            MaterialApplication targetMaterial = getExistingMaterial(request.getMaterialId());
            checkOwnerCanModify(targetMaterial);
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
        entity.setUpdateTime(LocalDateTime.now());
        updateById(entity);
        return getDetail(id);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        MaterialAttachment attachment = getExisting(id);
        MaterialApplication material = getExistingMaterial(attachment.getMaterialId());
        checkOwnerCanModify(material);
        removeById(id);
        if (StringUtils.hasText(attachment.getFilePath())) {
            deletePhysicalFileQuietly(resolveUnderRoot(attachment.getFilePath()));
        }
        operationLogService.recordSuccess("ATTACHMENT", "DELETE", "Delete material attachment " + attachment.getOriginalName(), "attachmentId=" + id + ",materialId=" + attachment.getMaterialId());
    }

    private MaterialAttachment getExisting(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Material attachment id must not be null");
        }
        MaterialAttachment entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Material attachment does not exist");
        }
        return entity;
    }

    private MaterialApplication getExistingMaterial(Long materialId) {
        if (materialId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "materialId must not be null");
        }
        MaterialApplication material = materialApplicationService.getById(materialId);
        if (material == null) {
            throw new BusinessException("Material application does not exist or has been deleted");
        }
        return material;
    }

    private void applyViewerScope(LambdaQueryWrapper<MaterialAttachment> wrapper, Long materialId) {
        if (CurrentUserUtils.isAdmin() || CurrentUserUtils.isAuditor()) {
            return;
        }
        Long studentId = CurrentUserUtils.requireStudentId();
        List<Long> materialIds = materialApplicationService.lambdaQuery()
                .select(MaterialApplication::getId)
                .eq(MaterialApplication::getStudentId, studentId)
                .list()
                .stream()
                .map(MaterialApplication::getId)
                .toList();
        if (materialIds.isEmpty()) {
            wrapper.eq(MaterialAttachment::getId, -1L);
            return;
        }
        if (materialId != null && !materialIds.contains(materialId)) {
            wrapper.eq(MaterialAttachment::getId, -1L);
            return;
        }
        wrapper.in(MaterialAttachment::getMaterialId, materialIds);
    }

    private void checkViewAccess(MaterialAttachment attachment) {
        CurrentUserUtils.requireUserId();
        MaterialApplication material = getExistingMaterial(attachment.getMaterialId());
        if (CurrentUserUtils.isAdmin() || CurrentUserUtils.isAuditor()) {
            return;
        }
        Long studentId = CurrentUserUtils.requireStudentId();
        if (!studentId.equals(material.getStudentId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Current account cannot access this attachment");
        }
    }

    private void checkOwnerCanModify(MaterialApplication material) {
        CurrentUserUtils.requireUserId();
        Long studentId = CurrentUserUtils.requireStudentId();
        if (!studentId.equals(material.getStudentId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Current account cannot modify attachments for this material");
        }
        if (!EDITABLE_MATERIAL_STATUSES.contains(material.getStatus())) {
            throw new BusinessException("Only draft, rejected, or cancelled materials can modify attachments");
        }
    }

    private void validateUploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Uploaded file must not be empty");
        }
        if (file.getSize() > uploadProperties.getMaxFileSize()) {
            throw new BusinessException("Uploaded file exceeds max size");
        }
        String originalName = cleanOriginalFilename(file.getOriginalFilename());
        String extension = getExtension(originalName);
        List<String> allowed = uploadProperties.getAllowedExtensions().stream()
                .filter(StringUtils::hasText)
                .map(item -> item.toLowerCase(Locale.ROOT))
                .toList();
        if (!allowed.contains(extension)) {
            throw new BusinessException("Uploaded file type is not allowed");
        }
    }

    private String cleanOriginalFilename(String originalFilename) {
        String originalName = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);
        if (!StringUtils.hasText(originalName) || originalName.contains("..") || originalName.contains("/") || originalName.contains("\\")) {
            throw new BusinessException("Uploaded file name is invalid");
        }
        return originalName;
    }

    private String getExtension(String originalName) {
        int index = originalName.lastIndexOf('.');
        if (index < 0 || index == originalName.length() - 1) {
            throw new BusinessException("Uploaded file extension is required");
        }
        return originalName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private Path uploadRoot() {
        return Path.of(uploadProperties.getRootDir()).toAbsolutePath().normalize();
    }

    private Path resolveUnderRoot(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            throw new BusinessException("Uploaded file path is invalid");
        }
        Path root = uploadRoot();
        Path resolved = root.resolve(relativePath).normalize();
        if (!resolved.startsWith(root)) {
            throw new BusinessException("Uploaded file path is invalid");
        }
        return resolved;
    }

    private void deletePhysicalFileQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            log.warn("Failed to delete uploaded file: {}", path, ex);
        }
    }

    private String normalizePrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "/api/material-attachments";
        }
        return prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
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
