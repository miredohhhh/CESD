package com.hjc.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hjc.backend.entity.ReviewerScope;
import com.hjc.backend.mapper.ReviewerScopeMapper;
import com.hjc.backend.service.ReviewerScopeService;
import org.springframework.stereotype.Service;

@Service
public class ReviewerScopeServiceImpl extends ServiceImpl<ReviewerScopeMapper, ReviewerScope> implements ReviewerScopeService {
}
