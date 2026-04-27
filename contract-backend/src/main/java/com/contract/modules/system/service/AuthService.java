package com.contract.modules.system.service;

import com.contract.vo.LoginResultVO;
import com.contract.vo.LoginVO;

public interface AuthService {

    LoginResultVO login(LoginVO loginVO);

    void logout();

    LoginResultVO getCurrentUserInfo();
}
