package com.zhuimeng.qcmanagement.controller.family;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.DevicePageDTO;
import com.zhuimeng.dto.ElderDTO;
import com.zhuimeng.qcmanagement.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 家人管理
* */

@RestController
@RequestMapping("/customer/user")
public class FamilyController {
    @Autowired
    private FamilyService familyService;

    /** 绑定家人
    * */
    @PostMapping("/add")
    public Result addFamily(@RequestBody ElderDTO elderDTO){
        return familyService.addFamily(elderDTO);
    }

    /** 解绑家人
    * */
    @DeleteMapping("/deleteById")
    public Result deleteById(Long id){
        return familyService.deleteById(id);
    }

    /** 查询设备数据
     * */
    @GetMapping("/queryServiceProperties/{iotId}")
    public Result queryServiceProperties(@PathVariable Long iotId){
        return familyService.queryServiceProperties(iotId);
    }

    /** 健康数据按日查询
     * */
    @GetMapping("/queryDeviceDataListByDay")
    public Result queryDeviceDataListByDay(DevicePageDTO devicePageDTO){
        return familyService.queryDeviceDataListByDay(devicePageDTO);
    }

    /** 健康数据按周查询
     * */
    @GetMapping("/queryDeviceDataListByWeek")
    public Result queryDeviceDataListByWeek(DevicePageDTO devicePageDTO){
        return familyService.queryDeviceDataListByWeek(devicePageDTO);
    }

    /** 异常数据查询
    * */
    @GetMapping("/queryDeviceDataListAbnormal")
    public Result queryDeviceDataListAbnormal(DevicePageDTO devicePageDTO){
        return familyService.queryDeviceDataListAbnormal(devicePageDTO);
    }
}
