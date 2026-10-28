package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;

public interface UserLoginService {
    Result login(String username, String password);

    Result getMenus();

    Result getMyButtons();
}
