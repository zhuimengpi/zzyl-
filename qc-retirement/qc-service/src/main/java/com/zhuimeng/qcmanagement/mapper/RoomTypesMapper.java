package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.dto.RoomTypesDTO;
import com.zhuimeng.entity.RoomTypes;
import com.zhuimeng.qcmanagement.annotation.AutoCreate;
import com.zhuimeng.qcmanagement.annotation.AutoUpdata;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoomTypesMapper {
    /** 查询所有房型
    * */
    List<RoomTypes> getAllRoomTypes();

    /** 根据状态查询房型
    * */
    List<RoomTypes> getRoomTypesByStatus(Integer status);

    /** 根据房型名称查询房型
     */
    List<RoomTypes> getRoomTypesByTypeName(String typeName);

    /** 根据房型id查询房型
     */
    List<RoomTypes> getRoomTypesById(Long id);

    /** 添加房型
     */
    @AutoCreate
    Boolean addRoomTypes(RoomTypes roomTypes);

    /** 修改房型
    * */
    @AutoUpdata
    Boolean updateRoomTypes(RoomTypes roomTypes);

    /** 删除房型
    * */
    Boolean deleteRoomTypes(Long id);
}
