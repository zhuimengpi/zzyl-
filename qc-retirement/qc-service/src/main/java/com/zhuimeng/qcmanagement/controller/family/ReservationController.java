package com.zhuimeng.qcmanagement.controller.family;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.ReservationDTO;
import com.zhuimeng.qcmanagement.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 预约管理
* */

@RestController
@RequestMapping("/customer/reservation")
public class ReservationController {
    @Autowired
    private ReservationService reservationService;

    /** 新增预约
    */
    @PostMapping()
    public Result addReservation(@RequestBody ReservationDTO reservationDTO) throws InterruptedException {
        return reservationService.addReservation(reservationDTO);
    }

    /** 预约时段统计
    * */
    @GetMapping("/countByTime")
    public Result countByTime(String time) {
        return reservationService.countByTime(time);
    }

    /** 预约列表
    * */
    @GetMapping("/page")
    public Result page(Integer pageNum, Integer pageSize, Integer status) {
        return reservationService.page(pageNum, pageSize, status);
    }

    /** 取消预约
    * */
    @PutMapping("/{id}/cancel")
    public Result cancelReservation(@PathVariable("id") Long id) {
        return reservationService.cancelReservation(id);
    }

    /** 已取消次数查询
    * */
    @GetMapping("/cancelled-count")
    public Result cancelledCount() {
        return reservationService.cancelledCount();
    }
}
