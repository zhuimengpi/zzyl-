package com.zhuimeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/** 订单分页VO
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderPageVO {

    /** 订单ID */
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 金额 */
    private BigDecimal amount;

    /** 状态 */
    private Integer status;

    /** 支付状态 */
    private Integer paymentStatus;

    /** 交易流水号 */
    private Long tradingOrderNo;

    /** 老人信息 */
    private ElderVO elderVo;

    /** 床位信息 */
    private Map<String, Object> bedVo;

    /** 服务项目信息 */
    private NursingProjectVO nursingProjectVo;

    /** 客户信息（下单人） */
    private Map<String, Object> memberVo;

    /** 期望服务时间 */
    private LocalDateTime estimatedArrivalTime;

    /** 创建时间 */
    private LocalDateTime createTime;
}
