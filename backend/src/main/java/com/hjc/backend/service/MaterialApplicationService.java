package com.hjc.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.ApproveMaterialApplicationRequest;
import com.hjc.backend.dto.CreateMaterialApplicationRequest;
import com.hjc.backend.dto.MyMaterialApplicationPageRequest;
import com.hjc.backend.dto.PendingMaterialApplicationPageRequest;
import com.hjc.backend.dto.RejectMaterialApplicationRequest;
import com.hjc.backend.dto.UpdateMaterialApplicationRequest;
import com.hjc.backend.dto.WithdrawMaterialApplicationRequest;
import com.hjc.backend.entity.MaterialApplication;
import com.hjc.backend.vo.MaterialApplicationDetailVO;
import com.hjc.backend.vo.MaterialApplicationVO;
import com.hjc.backend.vo.MyApplicationStatisticsVO;
import com.hjc.backend.vo.MyMaterialApplicationVO;
import com.hjc.backend.vo.PendingMaterialApplicationVO;

public interface MaterialApplicationService extends IService<MaterialApplication> {

    PageResult<MaterialApplicationVO> pageQuery(Long pageNum, Long pageSize, String keyword, String status, Long studentId, Long itemId);

    MaterialApplicationVO getDetail(Long id);

    MaterialApplicationVO create(CreateMaterialApplicationRequest request);

    MaterialApplicationVO update(Long id, UpdateMaterialApplicationRequest request);

    void deleteById(Long id);

    MaterialApplicationVO submitMaterialApplication(Long id);

    MaterialApplicationVO withdrawMaterialApplication(Long id, WithdrawMaterialApplicationRequest request);

    MaterialApplicationVO approveMaterialApplication(Long id, ApproveMaterialApplicationRequest request);

    MaterialApplicationVO rejectMaterialApplication(Long id, RejectMaterialApplicationRequest request);

    PageResult<MyMaterialApplicationVO> pageMyApplications(MyMaterialApplicationPageRequest request);

    MaterialApplicationDetailVO getFrontendApplicationDetail(Long id);

    MyApplicationStatisticsVO getMyApplicationStatistics();

    PageResult<PendingMaterialApplicationVO> pagePendingApplications(PendingMaterialApplicationPageRequest request);
}
