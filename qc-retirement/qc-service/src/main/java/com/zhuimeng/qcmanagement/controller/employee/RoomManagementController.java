package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.RoomDTO;
import com.zhuimeng.qcmanagement.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 房间管理
* */

@RestController
@RequestMapping("/sysUser/room")
public class RoomManagementController {
    @Autowired
    private RoomService roomService;

    /** 获取所有房间
    * */
    @GetMapping("/getAll")
    public Result getAll(){
        return roomService.getAll();
    }

    /** 房间详情
    * */
    @GetMapping("/get/{id}")
    public Result getDetail(@PathVariable Long id){
        return roomService.getDetail(id);
    }

    /** 楼层下房间
     * */
    @GetMapping("/getRoomsByFloorId/{id}")
    public Result getRoomByFloorId(@PathVariable Long id){
        return roomService.getRoomByFloorId(id);
    }

    /** 添加房间
     * */
    @PostMapping("/add")
    public Result add(@RequestBody RoomDTO roomDTO){
        return roomService.addRoom(roomDTO);
    }

    /** 编辑房间
     * */
    @PutMapping("/update")
    public Result edit(@RequestBody RoomDTO roomDTO){
        return roomService.editRoom(roomDTO);
    }

    /** 删除房间
     * */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id){
        return roomService.deleteRoom(id);
    }

    /** 智能床位-房间
     * */
    @GetMapping("/getRoomsWithDeviceByFloorId/{id}")
    public Result getRoomsWithDeviceByFloorId(@PathVariable Long id){
        return roomService.getRoomsWithDeviceByFloorId(id);
    }

    /** 负责老人-房间
     * */
    @GetMapping("/getRoomsWithNurByFloorId/{id}")
    public Result getRoomsWithNurByFloorId(@PathVariable Long id){
        return roomService.getRoomsWithNurByFloorId(id);
    }
}
