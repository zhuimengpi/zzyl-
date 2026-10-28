package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.PageDTO;
import com.zhuimeng.dto.ReservationDTO;
import com.zhuimeng.qcmanagement.service.VisitService;
import com.zhuimeng.vo.ReservationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 来访管理
* */

@RestController
@RequestMapping("/sysUser")
public class VisitController {
    @Autowired
    private VisitService visitService;

    /** 预约分页查询
    */
    @GetMapping("/reservation/page")
    public Result.PageData<ReservationVO> reservationPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Long startTime,  // 开始时间，毫秒时间戳
            @RequestParam(required = false) Long endTime     // 结束时间，毫秒时间戳
    ) {
        PageDTO pageDTO = new PageDTO(pageNum, pageSize, name, phone, startTime, endTime, 0, type);
        return visitService.reservationPage(pageDTO);
    }

    /** 到访确认
    * */
    @PutMapping("/reservation/{id}/visit")
    public Result visitConfirm(
            @PathVariable Long id,
            @RequestParam Long time
    ) {
        return visitService.visitConfirm(id, time);
    }

    /** 来访分页查询
     * */
    @GetMapping("/visit/page")
    public Result.PageData<ReservationVO> visitPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Long startTime,  // 开始时间，毫秒时间戳
            @RequestParam(required = false) Long endTime     // 结束时间，毫秒时间戳
    ) {
        PageDTO pageDTO = new PageDTO(pageNum, pageSize,name, phone, startTime, endTime, 1, type);
        return visitService.reservationPage(pageDTO);
    }

    /** 来访添加
     * */
    @PostMapping("/visit")
    public Result visitAdd(@RequestBody ReservationDTO reservationDTO) {
        return visitService.reservationAdd(reservationDTO);
    }
}
