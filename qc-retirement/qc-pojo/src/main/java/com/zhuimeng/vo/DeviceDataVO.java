package com.zhuimeng.vo;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 健康监测数据
* */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DeviceDataVO {
    /** 主键ID */
    private Long id;

    /** 设备名称 */
    private String deviceName;

    /** 设备ID */
    private String iotId;

    /** 备注名称 */
    private String noteName;

    /** 所属产品的key */
    private String productId;

    /** 产品名称 */
    private String productName;

    /** 功能名称 */
    private String functionName;

    /** 接入位置 */
    private String accessLocation;

    /** 数据值 */
    private String dataValue;

    /** 报警时间 */
    private LocalDateTime alarmTime;

    /** 处理结果 */
    private String processingResult;

    /** 处理人 */
    private String processor;

    /** 处理时间 */
    private LocalDateTime processingTime;

    /** 状态 0:正常 1:异常 2:待处理 3:已处理 */
    private String status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 创建人id */
    private Long createBy;

    /** 更新人id */
    private Long updateBy;

    /** 备注 */
    private String remark;
}
