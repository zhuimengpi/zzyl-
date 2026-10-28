package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 订单
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Order {

    /**
     * id
     */
    private Long id;

    /**
     * 交易系统订单号
     */
    private Long tradingOrderNo;

    /**
     * 付款状态（1:未付，2:已付，3:已关闭）
     */
    private Integer paymentStatus;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 退款金额（付款后，单位：元）
     */
    private BigDecimal refund;

    /**
     * 是否有退款（YES，NO）
     */
    private String isRefund;

    /**
     * 客户id
     */
    private Long memberId;

    /**
     * 服务项目id
     */
    private Long projectId;

    /**
     * 服务对象ID
     */
    private Long elderId;

    /**
     * 预计服务时间
     */
    private LocalDateTime estimatedArrivalTime;

    /**
     * 备注
     */
    private String mark;

    /**
     * 取消原因
     */
    private String reason;

    /**
     * 订单状态（0:待支付，1:待执行，2:已执行，3:已完成，4:已关闭，5:已退款）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

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

    /**
     * 是否可见（0:可见，1:不可见）
     */
    private String viewStatus;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 取消人类型（1:前台，2:后台）
     */
    private Integer oCreateType;
}
