package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.Bed;
import com.zhuimeng.qcmanagement.annotation.AutoCreate;
import com.zhuimeng.qcmanagement.annotation.AutoUpdata;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface BedMapper {
    /** 根据房间id查询床位列表
    * */
    List<Bed> readRoom(Long id);

    /** 根据床位id查询床位详情
     */
    Bed read(Long id);

    /** 添加床位
     */
    @AutoCreate
    void create(Bed bed);

    /** 修改床位信息
     */
    @AutoUpdata
    Integer update(Bed oldBed);

    /** 删除床位
     */
    void delete(Long id);

    /** 根据房间id查询床位列表
     */
    List<Bed> selectByRoomIds(@Param("roomIds") List<Long> roomIds);
}
