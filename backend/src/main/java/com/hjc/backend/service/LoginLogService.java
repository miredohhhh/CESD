package com.hjc.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hjc.backend.common.PageResult;
import com.hjc.backend.dto.LoginLogPageRequest;
import com.hjc.backend.entity.LoginLog;
import com.hjc.backend.vo.LoginLogVO;
import com.hjc.backend.vo.LoginUserVO;

public interface LoginLogService extends IService<LoginLog> {

    String TYPE_LOGIN = "LOGIN";

    String TYPE_LOGOUT = "LOGOUT";

    String RESULT_SUCCESS = "SUCCESS";

    String RESULT_FAIL = "FAIL";

    PageResult<LoginLogVO> pageQuery(LoginLogPageRequest request);

    LoginLogVO getDetail(Long id);

    void recordLoginSuccess(LoginUserVO user);

    void recordLoginFailure(String username, String errorMessage);

    void recordLogoutSuccess();
}
