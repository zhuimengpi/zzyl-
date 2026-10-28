package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.UserDTO;
import com.zhuimeng.entity.User;

public interface UserService {
    Result getCurrentUser();

    Result updateUser(UserDTO userDTO);

    Result modifyPasswords(String oldPassword, String newPassword);

    Result checkPassword(String password);

    Result page(Integer pageNum, Integer pageSize, UserDTO userDTO);

    Result list(UserDTO userDTO);

    Result add(UserDTO userDTO);

    Result update(UserDTO userDTO);

    Result delete(String userIds);

    Result resetPassword(Long userId);

    Result updateIsEnable(Long id, String dataState);

    Result cUserPage(Integer pageNum, Integer pageSize, String name, String idCard);
}
