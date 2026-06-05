package com.hjc.backend.service;

import com.hjc.backend.dto.MaterialApplicationExportRequest;
import com.hjc.backend.dto.ScoreSummaryPageRequest;

public interface ExcelExportService {

    byte[] exportScoreSummaries(ScoreSummaryPageRequest request);

    byte[] exportClassRanking(Long classId);

    byte[] exportMajorRanking(Long majorId);

    byte[] exportMaterialApplications(MaterialApplicationExportRequest request);
}
