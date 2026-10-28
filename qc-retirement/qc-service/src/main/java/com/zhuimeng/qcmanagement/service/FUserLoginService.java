package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.FUserDTO;

public interface FUserLoginService {

    Result login(FUserDTO fUserDTO);

    Result logout();

    Result checkLogin(String token);
}
