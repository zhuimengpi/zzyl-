package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.RoomTypesDTO;
import com.zhuimeng.qcmanagement.service.RoomTypesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 房型管理
* */

@RestController
@RequestMapping("/sysUser/roomTypes")
public class RoomTypesController {
    @Autowired
    private RoomTypesService roomTypesService;

    /** 获取所有房型
    * */
    @GetMapping()
    public Result getAllRoomTypes(){
        return roomTypesService.getAllRoomTypes();
    }

    /** 按状态查询房型
    * */
    @GetMapping("/status")
    public Result getRoomTypesByStatus(Integer status){
        return roomTypesService.getRoomTypesByStatus(status);
    }

    /** 按房型名称查询房型
    * */
    @GetMapping("/typeName")
    public Result getRoomTypesByTypeName(String typeName){
        return roomTypesService.getRoomTypesByTypeName(typeName);
    }

    /** 房型详情
    * */
    @GetMapping("/{id}")
    public Result getRoomTypesById(@PathVariable Long id) {
        return roomTypesService.getRoomTypesById(id);
    }

    /** 添加房型
    * */
    @PostMapping("")
    public Result addRoomTypes(@RequestBody RoomTypesDTO roomTypesDTO){
        return roomTypesService.addRoomTypes(roomTypesDTO);
    }

    /** 修改房型
     */
    @PutMapping("/{id}")
    public Result updateRoomTypes(@PathVariable Long id, @RequestBody RoomTypesDTO roomTypesDTO){
        return roomTypesService.updateRoomTypes(id, roomTypesDTO);
    }

    /** 删除房型
    * */
    @DeleteMapping("/{id}")
    public Result deleteRoomTypes(@PathVariable Long id){
        return roomTypesService.deleteRoomTypes(id);
    }

    /** 房型启用/禁用
    * */
    @PutMapping("/{id}/status/{status}")
    public Result updateRoomTypesStatus(@PathVariable Long id, @PathVariable Integer status){
        return roomTypesService.updateRoomTypesStatus(id, status);
    }
}
