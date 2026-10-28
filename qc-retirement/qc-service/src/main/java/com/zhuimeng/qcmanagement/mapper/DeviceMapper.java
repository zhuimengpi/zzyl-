package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.dto.DevicePageDTO;
import com.zhuimeng.entity.Device;
import com.zhuimeng.entity.DeviceData;
import com.zhuimeng.vo.DeviceDataVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface DeviceMapper {

    /** 根据IoT设备ID查询设备数据
    * */
    ArrayList<DeviceData> selectById(Long iotId);

    /** 健康数据按日查询
    * */
    ArrayList<DeviceDataVO> queryDeviceDataListByDay(DevicePageDTO devicePageDTO);

    /** 异常数据查询
     * */
    ArrayList<DeviceDataVO> queryDeviceDataListAbnormal(DevicePageDTO devicePageDTO);

    /** 根据绑定位置和物理位置类型批量查询设备
     * */
    List<Device> selectByLocationList(@Param("bindingLocations") List<String> bindingLocations,
                                      @Param("physicalLocationType") Integer physicalLocationType);

    /** 根据设备ID列表批量查询设备数据
     * */
    ArrayList<DeviceData> selectDataByIotIds(@Param("iotIds") List<String> iotIds);
}
