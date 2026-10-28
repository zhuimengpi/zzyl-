package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.ReservationDTO;

public interface ReservationService {
    Result addReservation(ReservationDTO reservationDTO) throws InterruptedException;

    Result countByTime(String time);

    Result page(Integer pageNum, Integer pageSize, Integer status);

    Result cancelReservation(Long id);

    Result cancelledCount();
}
