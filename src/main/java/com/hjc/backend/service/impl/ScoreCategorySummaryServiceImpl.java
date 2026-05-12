package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.entity.ScoreCategorySummary;
import com.hjc.backend.mapper.ScoreCategorySummaryMapper;
import com.hjc.backend.service.ScoreCategorySummaryService;
import org.springframework.stereotype.Service;

@Service
public class ScoreCategorySummaryServiceImpl extends ServiceImpl<ScoreCategorySummaryMapper, ScoreCategorySummary> implements ScoreCategorySummaryService {
}
