package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.NursingProjectDTO;

public interface NursingProjectService {
    Result listAll();

    Result listByPage(Integer pageNum, Integer pageSize, String name, Integer status);

    Result detail(Long id);

    Result insert(NursingProjectDTO nursingProjectDTO);

    Result update(NursingProjectDTO nursingProjectDTO);

    Result delete(Long id);

    Result updateStatus(Long id, Integer status);
}
