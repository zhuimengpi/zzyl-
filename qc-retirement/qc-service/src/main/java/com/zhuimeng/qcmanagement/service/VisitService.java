package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.PageDTO;
import com.zhuimeng.dto.ReservationDTO;
import com.zhuimeng.vo.ReservationVO;

public interface VisitService {

    Result.PageData<ReservationVO> reservationPage(PageDTO pageDTO);

    Result visitConfirm(Long id, Long time);

    Result reservationAdd(ReservationDTO reservationDTO);
}
