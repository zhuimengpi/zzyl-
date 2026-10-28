package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.RoomTypesDTO;

public interface RoomTypesService {
    Result getAllRoomTypes();

    Result getRoomTypesByStatus(Integer status);

    Result getRoomTypesByTypeName(String typeName);

    Result getRoomTypesById(Long id);

    Result addRoomTypes(RoomTypesDTO roomTypesDTO);

    Result updateRoomTypes(Long id, RoomTypesDTO roomTypesDTO);

    Result deleteRoomTypes(Long id);

    Result updateRoomTypesStatus(Long id, Integer status);
}
