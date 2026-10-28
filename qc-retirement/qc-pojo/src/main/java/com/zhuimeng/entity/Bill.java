package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 账单
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Bill {

    /**
     * id
     */
    private Long id;

    /**
     * 账单编号
     */
    private String billNo;

    /**
     * 交易系统订单号
     */
    private Long tradingOrderNo;

    /**
     * 账单类型（0：月度账单，1：服务账单）
     */
    private Integer billType;

    /**
     * 账单月份
     */
    private String billMonth;

    /**
     * 老人ID
     */
    private Long elderId;

    /**
     * 账单金额（元）
     */
    private BigDecimal billAmount;

    /**
     * 应付金额（元）
     */
    private BigDecimal payableAmount;

    /**
     * 预付款支付金额
     */
    private BigDecimal prepaidAmount;

    /**
     * 押金金额
     */
    private BigDecimal depositAmount;

    /**
     * 本期应付
     */
    private BigDecimal currentCost;

    /**
     * 支付截止时间
     */
    private LocalDateTime paymentDeadline;

    /**
     * 账单状态（0：未支付，1：已支付，2：已关闭）
     */
    private Integer transactionStatus;

    /**
     * 账单开始时间
     */
    private LocalDateTime billStartTime;

    /**
     * 账单结束时间
     */
    private LocalDateTime billEndTime;

    /**
     * 共计天数（天）
     */
    private Integer totalDays;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 护理等级名称
     */
    private String lname;

    /**
     * 房间类型名称
     */
    private String typeName;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 更新人id
     */
    private Long updateBy;

    /**
     * 备注
     */
    private String remark;
}
