package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.RoomDTO;

public interface RoomService {

    Result getHotRoomTypes(String status);

    Result getAll();

    Result getDetail(Long id);

    Result getRoomByFloorId(Long id);

    Result addRoom(RoomDTO roomDTO);

    Result editRoom(RoomDTO roomDTO);

    Result deleteRoom(Long id);

    Result getRoomsWithDeviceByFloorId(Long id);

    Result getRoomsWithNurByFloorId(Long id);
}
