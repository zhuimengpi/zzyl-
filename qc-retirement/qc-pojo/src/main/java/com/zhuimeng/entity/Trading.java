package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 交易订单
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Trading {

    /**
     * 主键
     */
    private Long id;

    /**
     * 业务系统订单号
     */
    private Long productOrderNo;

    /**
     * 交易系统订单号
     */
    private Long tradingOrderNo;

    /**
     * 支付渠道
     */
    private String tradingChannel;

    /**
     * 交易类型（1:月度费用，2:服务费用）
     */
    private String tradingType;

    /**
     * 交易单状态（1:待付款，2:付款中，3:付款失败，4:已结算，5:取消订单，6:免单，7:挂账）
     */
    private Integer tradingState;

    /**
     * 收款人姓名
     */
    private String payeeName;

    /**
     * 收款人账户ID
     */
    private Long payeeId;

    /**
     * 付款人姓名
     */
    private String payerName;

    /**
     * 付款人Id
     */
    private Long payerId;

    /**
     * 交易金额（元）
     */
    private BigDecimal tradingAmount;

    /**
     * 退款金额（元）
     */
    private BigDecimal refund;

    /**
     * 是否有退款
     */
    private String isRefund;

    /**
     * 第三方交易返回编码
     */
    private String resultCode;

    /**
     * 第三方交易返回提示消息
     */
    private String resultMsg;

    /**
     * 第三方交易返回信息json
     */
    private String resultJson;

    /**
     * 统一下单返回编码
     */
    private String placeOrderCode;

    /**
     * 统一下单返回信息
     */
    private String placeOrderMsg;

    /**
     * 统一下单返回信息json
     */
    private String placeOrderJson;

    /**
     * 商户号
     */
    private Long enterpriseId;

    /**
     * 备注
     */
    private String memo;

    /**
     * 二维码base64数据
     */
    private String qrCode;

    /**
     * open_id标识
     */
    private String openId;

    /**
     * 是否有效
     */
    private String enableFlag;

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
}
