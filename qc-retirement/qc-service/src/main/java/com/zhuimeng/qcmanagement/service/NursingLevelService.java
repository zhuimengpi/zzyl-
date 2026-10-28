package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.NursingLevelDTO;

public interface NursingLevelService {

    Result listAll();

    Result listByPage(Integer pageNum, Integer pageSize, String name, Integer status);

    Result detail(Long id);

    Result insert(NursingLevelDTO nursingLevelDTO);

    Result update(NursingLevelDTO nursingLevelDTO);

    Result delete(Long id);

    Result updateStatus(Long id, Integer status);
}
