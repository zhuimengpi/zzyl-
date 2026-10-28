package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.entity.Floor;
import com.zhuimeng.qcmanagement.service.FloorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 楼层管理
* */

@RestController
@RequestMapping("/sysUser/floor")
public class FloorController {
    @Autowired
    private FloorService floorService;

    /** 获取楼层列表
    * */
    @GetMapping("/getAll")
    public Result getAll() {
        return floorService.getAll();
    }

    /** 楼层详情
    * */
    @GetMapping("/get/{id}")
    public Result get(@PathVariable Long id) {
        return floorService.get(id);
    }

    /** 添加楼层
    * */
    @PostMapping("/add")
    public Result add(@RequestBody Floor floor) {
        return floorService.add(floor);
    }

    /** 编辑楼层
    * */
    @PutMapping("/update")
    public Result update(@RequestBody Floor floor) {
        return floorService.update(floor);
    }

    /** 删除楼层
    * */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        return floorService.delete(id);
    }

    /** 获取所有楼层及关联智能设备（智能床位用）
    * */
    @GetMapping("/getAllFloorsWithDevice")
    public Result getAllFloorsWithDevice() {
        return floorService.getAllFloorsWithDevice();
    }

    /** 获取所有楼层及关联护理员（负责老人用）
    * */
    @GetMapping("/getAllFloorsWithNur")
    public Result getAllFloorsWithNur() {
        return floorService.getAllFloorsWithNur();
    }

    /** 获取所有楼层、房间、床位完整结构（添加智能设备用）
    * */
    @GetMapping("/getAllWithRoomAndBed")
    public Result getAllWithRoomAndBed() {
        return floorService.getAllWithRoomAndBed();
    }

    /** 按床位状态筛选楼层-房间-床位（入住床位选择用）
    * */
    @GetMapping("/getRoomAndBedByBedStatus/{status}")
    public Result getRoomAndBedByBedStatus(@PathVariable Integer status) {
        return floorService.getRoomAndBedByBedStatus(status);
    }
}
