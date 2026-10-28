package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.Room;
import com.zhuimeng.entity.RoomType;
import com.zhuimeng.qcmanagement.annotation.AutoCreate;
import com.zhuimeng.qcmanagement.annotation.AutoUpdata;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;

@Mapper
public interface RoomMapper {
    /** 查询所有房间类型
     */
    ArrayList<RoomType> getRoomTypes(String status);

    /** 查询所有房间
    * */
    ArrayList<Room> getAll();

    /** 查询房间详情
     */
    Room getDetail(Long id);

    /** 根据楼层查询房间
     */
    ArrayList<Room> getRoomByFloorId(Long id);

    /** 添加房间
     */
    @AutoCreate
    Integer addRoom(Room room);

    /** 编辑房间
     */
    @AutoUpdata
    Boolean editRoom(Room room);

    /** 删除房间
     */
    Boolean deleteRoom(Long id);
}
