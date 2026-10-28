package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 健康数据查询
* */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DevicePageDTO {

    /** 功能标识（如 HeartRate 心率） */
    private String functionId;

    /** 设备ID */
    private String iotId;

    /** 开始时间戳(ms) */
    private Long startTime;

    /** 结束时间戳(ms) */
    private Long endTime;

    /** 当前页码 */
    private Integer pageNum;

    /** 每页条数 */
    private Integer pageSize;
}
