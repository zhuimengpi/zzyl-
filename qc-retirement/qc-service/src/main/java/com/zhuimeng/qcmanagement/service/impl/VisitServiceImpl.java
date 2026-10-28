package com.zhuimeng.qcmanagement.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.PageDTO;
import com.zhuimeng.dto.ReservationDTO;
import com.zhuimeng.entity.Reservation;
import com.zhuimeng.qcmanagement.mapper.ReservationMapper;
import com.zhuimeng.vo.ReservationVO;
import com.zhuimeng.qcmanagement.service.VisitService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class VisitServiceImpl implements VisitService {
    @Autowired
    private ReservationMapper reservationMapper;

    /** 分页查询
     */
    @Override
    public Result.PageData<ReservationVO> reservationPage(PageDTO pageDTO) {
        // PageHelper 分页（仅对紧跟其后的第一次查询操作生效）
        PageHelper.startPage(pageDTO.getPage(), pageDTO.getSize());
        List<ReservationVO> list = reservationMapper.selectReservationPage(pageDTO);
        PageInfo<ReservationVO> pageInfo = new PageInfo<>(list);

        return new Result.PageData<>(list, pageInfo.getTotal(), pageDTO.getPage().longValue(), pageDTO.getSize().longValue());
    }

    /** 到访确认
     * */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result visitConfirm(Long id, Long time) {
        // 1.查询访问预约(根据预约id查询)
        Reservation reservation = reservationMapper.selectById(id);

        // 2.判断访问是否可用
        if (reservation == null || reservation.getStatus() != 0) {
            // 预约不可用
            return Result.fail("预约不可用");
        }

        // 3.到访确认，修改访问状态
        reservation.setStatus(1);
        reservationMapper.updateById(reservation);

        return Result.ok("到访确认成功");
    }

    /** 来访添加
     * */
    @Override
    public Result reservationAdd(ReservationDTO reservationDTO) {
        // 创建来访
        Reservation reservation = new Reservation();
        BeanUtils.copyProperties(reservationDTO, reservation);

        // 数据完善
        reservation.setTime(LocalDateTime.now());
        reservation.setStatus(1);
        reservation.setRemark("现场来访");

        // 保存来访
        reservationMapper.insertSite(reservation);
        return Result.ok("添加成功");
    }
}
