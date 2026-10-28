package com.zhuimeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 退款记录 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RefundRecordVO {

    /** 退款状态 */
    private String refundStatus;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 退款原因 */
    private String refundMsg;

    /** 退款备注 */
    private String memo;

    /** 申请时间 */
    private LocalDateTime createTime;

    /** 处理时间 */
    private LocalDateTime updateTime;
}
