package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.entity.Floor;

public interface FloorService {
    Result getAll();

    Result get(Long id);

    Result add(Floor floor);

    Result update(Floor floor);

    Result delete(Long id);

    Result getAllFloorsWithDevice();

    Result getAllFloorsWithNur();

    Result getAllWithRoomAndBed();

    Result getRoomAndBedByBedStatus(Integer status);
}
