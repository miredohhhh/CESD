package com.hjc.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjc.backend.common.PageRequest;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.ScoreSummaryPageRequest;
import com.hjc.backend.entity.ScoreSummary;
import com.hjc.backend.vo.FrontendScoreSummaryVO;
import com.hjc.backend.vo.ScoreCategorySummaryVO;
import com.hjc.backend.vo.ScoreSummaryVO;

import java.util.List;

public interface ScoreSummaryService extends IService<ScoreSummary> {

    ScoreSummaryVO recalculateStudentScore(Long studentId);

    List<ScoreSummaryVO> recalculateClassScores(Long classId);

    List<ScoreSummaryVO> recalculateMajorScores(Long majorId);

    void recalculateAllScores();

    ScoreSummaryVO getStudentScore(Long studentId);

    List<ScoreCategorySummaryVO> listStudentCategoryScores(Long studentId);

    PageResult<ScoreSummaryVO> pageScoreSummaries(ScoreSummaryPageRequest request);

    List<ScoreSummaryVO> listClassRanking(Long classId);

    List<ScoreSummaryVO> listMajorRanking(Long majorId);

    FrontendScoreSummaryVO getMyScore(Long studentId);

    List<ScoreCategorySummaryVO> getMyCategoryScores(Long studentId);

    PageResult<FrontendScoreSummaryVO> pageClassRankingForFrontend(Long classId, PageRequest request);

    PageResult<FrontendScoreSummaryVO> pageMajorRankingForFrontend(Long majorId, PageRequest request);
}
