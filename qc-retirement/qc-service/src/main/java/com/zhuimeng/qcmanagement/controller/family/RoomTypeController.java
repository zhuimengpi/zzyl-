package com.zhuimeng.qcmanagement.controller.family;

import com.zhuimeng.Result.Result;
import com.zhuimeng.qcmanagement.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 用户住房模块
* */

@RestController
@RequestMapping("/customer/roomTypes")
public class RoomTypeController {
    @Autowired
    private RoomService roomService;

    /** 获取热门住房
    * */
    @GetMapping
    public Result getHotRoomTypes(String status){
        return roomService.getHotRoomTypes(status);
    }
}
