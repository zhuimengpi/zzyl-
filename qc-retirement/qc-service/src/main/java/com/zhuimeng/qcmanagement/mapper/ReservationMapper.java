package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.dto.PageDTO;
import com.zhuimeng.dto.ReservationDTO;
import com.zhuimeng.entity.Reservation;
import com.zhuimeng.qcmanagement.annotation.AutoCreate;
import com.zhuimeng.qcmanagement.annotation.AutoUpdata;
import com.zhuimeng.vo.ReservationVO;
import com.zhuimeng.vo.TimeSlotVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface ReservationMapper {
    /**
     * 新增预约
     */
    @AutoCreate
    Integer insert(ReservationDTO reservationDTO);

    /** 根据时间查询预约数量
     */
    ArrayList<TimeSlotVO> countByTime(String time);

    /** 分页查询预约
     */
    ArrayList<ReservationVO> page(Integer status);

    /** 根据id查询预约
     */
    Reservation selectById(Long id);

    /** 根据id更新预约
     */
    @AutoUpdata
    void updateById(Reservation reservation);

    /** 根据(name, mobile, time, visitor)，查询
    * */
    Reservation selectByReservation(ReservationDTO reservationDTO);

    /** 分页查询预约（系统端通用）
     * 姓名模糊查询
     * 手机号精准查询
     * 时间范围查询
    * */
    List<ReservationVO> selectReservationPage(PageDTO pageDTO);

    /** 现场来访
     */
    @AutoCreate
    void insertSite(Reservation reservation);

    /** 预约过期定时任务
     */
    void updateReservationStatus();

    /** 精准过期预约
    * */
    void updateTimeoutReservation(String reservationId);
}
