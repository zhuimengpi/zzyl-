package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.DeptDTO;

public interface DeptService {
    Result list(DeptDTO deptDTO);

    Result tree(String level);

    Result add(DeptDTO deptDTO);

    Result update(DeptDTO deptDTO);

    Result updateIsEnable(Long id, Integer status);

    Result delete(Long id);
}
