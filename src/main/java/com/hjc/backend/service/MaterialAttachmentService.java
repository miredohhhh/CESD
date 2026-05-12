package com.hjc.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateMaterialAttachmentRequest;
import com.hjc.backend.dto.UpdateMaterialAttachmentRequest;
import com.hjc.backend.entity.MaterialAttachment;
import com.hjc.backend.vo.MaterialAttachmentVO;

public interface MaterialAttachmentService extends IService<MaterialAttachment> {

    PageResult<MaterialAttachmentVO> pageQuery(Long pageNum, Long pageSize, Long materialId, String fileType, String keyword);

    MaterialAttachmentVO getDetail(Long id);

    MaterialAttachmentVO create(CreateMaterialAttachmentRequest request);

    MaterialAttachmentVO update(Long id, UpdateMaterialAttachmentRequest request);

    void deleteById(Long id);
}
