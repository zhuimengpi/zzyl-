package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.CheckInApplyDTO;
import com.zhuimeng.dto.RetreatApplyDTO;

/** 入住/退住管理 Service
* */
public interface CheckInService {

    // ==================== 入住 V2 ====================

    /** 分页查询入住申请列表（V2） */
    Result pageQuery(Integer pageNum, Integer pageSize, Integer status,
                     String elderName, String startTime, String endTime);

    /** 提交入住申请（V2） */
    Result apply(CheckInApplyDTO dto);

    /** 入住详情（V2） */
    Result detail(Long id);

    // ==================== 退住 V2 ====================

    /** 分页查询退住申请列表（V2） */
    Result retreatPageQuery(Integer pageNum, Integer pageSize,
                            String elderName, String elderIdCardNo,
                            String retreatStartTime, String retreatEndTime);

    /** 退住详情（V2） */
    Result retreatDetail(Long id);

    /** 提交退住申请（V2） */
    Result retreatApply(RetreatApplyDTO dto);
}
