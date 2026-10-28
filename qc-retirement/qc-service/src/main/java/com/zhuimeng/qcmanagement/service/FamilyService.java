package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.DevicePageDTO;
import com.zhuimeng.dto.ElderDTO;

public interface FamilyService {
    Result listByPage(Integer pageNum, Integer pageSize);

    Result listAll();

    Result addFamily(ElderDTO elderDTO);

    Result deleteById(Long elderId);

    Result queryServiceProperties(Long elderId);

    Result queryDeviceDataListByDay(DevicePageDTO devicePageDTO);

    Result queryDeviceDataListByWeek(DevicePageDTO devicePageDTO);

    Result queryDeviceDataListAbnormal(DevicePageDTO devicePageDTO);
}
