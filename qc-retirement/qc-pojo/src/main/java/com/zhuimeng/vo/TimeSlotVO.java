package com.zhuimeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 预约时段统计
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TimeSlotVO {

    /** 时间段（如 09:00-10:00） */
    private String timeSlot;

    /** 已预约数 */
    private Integer count;
}
