package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.RoleDTO;

public interface RoleService {
    Result page(Integer pageNum, Integer pageSize, RoleDTO roleDTO);

    Result initRoles();

    Result findCheckedResources(Long roleId);

    Result add(RoleDTO roleDTO);

    Result update(RoleDTO roleDTO);

    Result delete(String roleIds);
}
