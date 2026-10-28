package com.zhuimeng.qcmanagement.task;

import com.zhuimeng.qcmanagement.mapper.ReservationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 预约过期定时任务
 * */

@Component
public class ReservationTask {
    @Autowired
    private ReservationMapper reservationMapper;

    /** 预约过期定时任务
    * */
    @Scheduled(cron = "0 0 0 * * ?")  // 每天0点执行一次
    public void reservationTask(){
        //将所有到期，status=0 的预约，修改为status=2
        reservationMapper.updateReservationStatus();
    }
}
