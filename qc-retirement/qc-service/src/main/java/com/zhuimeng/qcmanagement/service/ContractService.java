package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;

public interface ContractService {
    Result contractList(Integer pageNum, Integer pageSize, String contractNo, String elderName, Integer status, Long startTime, Long endTime);

    Result contractDetail(Long id);
}
