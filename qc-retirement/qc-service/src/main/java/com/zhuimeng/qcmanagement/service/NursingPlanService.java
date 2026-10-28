package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.NursingPlanDTO;

public interface NursingPlanService {
    Result list();

    Result page(Integer pageSize, Integer pageNum, String name, Integer status);

    Result detail(Long id);

    Result add(NursingPlanDTO nursingPlanDTO);

    Result edit(Long id, NursingPlanDTO nursingPlanDTO);

    Result delete(Long id);

    Result status(Long id, Integer status);
}
