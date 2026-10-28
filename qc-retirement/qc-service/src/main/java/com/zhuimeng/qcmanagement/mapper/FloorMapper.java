package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.Floor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FloorMapper {
    /** 获取楼层列表
     * */
    List<Floor> getAll();

    /** 根据ID查询楼层详情
     * */
    Floor getById(Long id);

    /** 添加楼层
     * */
    void add(Floor floor);

    /** 编辑楼层
     * */
    Integer update(Floor floor);

    /** 删除楼层
     * */
    void delete(Long id);

    /** 获取所有楼层及关联智能设备
     * */
    List<Floor> getAllFloorsWithDevice();

    /** 获取所有楼层及关联护理员
     * */
    List<Floor> getAllFloorsWithNur();

    /** 获取所有楼层、房间、床位完整结构
     * */
    List<Floor> getAllWithRoomAndBed();

    /** 按床位状态筛选楼层-房间-床位
     * */
    List<Floor> getRoomAndBedByBedStatus(Integer status);
}
