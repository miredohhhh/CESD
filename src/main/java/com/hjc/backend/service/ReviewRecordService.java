package com.hjc.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.CreateReviewRecordRequest;
import com.hjc.backend.dto.ReviewRecordPageRequest;
import com.hjc.backend.entity.ReviewRecord;
import com.hjc.backend.vo.ReviewRecordVO;

import java.util.List;

public interface ReviewRecordService extends IService<ReviewRecord> {

    PageResult<ReviewRecordVO> pageReviewRecords(ReviewRecordPageRequest request);

    ReviewRecordVO getReviewRecordDetail(Long id);

    ReviewRecordVO createReviewRecord(CreateReviewRecordRequest request);

    List<ReviewRecordVO> listByMaterialId(Long materialId);
}
