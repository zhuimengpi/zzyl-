package com.zhuimeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 账单列表VO
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BillVO {

    /** 账单ID */
    private Long id;

    /** 账单编号 */
    private String billNo;

    /** 账单类型（0：月度账单，1：服务账单） */
    private Integer billType;

    /** 账单月份 */
    private String billMonth;

    /** 账单金额 */
    private BigDecimal billAmount;

    /** 应付金额 */
    private BigDecimal payableAmount;

    /** 支付状态 */
    private Integer transactionStatus;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 支付截止时间 */
    private LocalDateTime paymentDeadline;

    /** 老人信息 */
    private ElderVO elderVo;

    /** 本期应付 */
    private BigDecimal currentCost;

    /** 押金金额 */
    private BigDecimal depositAmount;

    /** 预付款支付金额 */
    private BigDecimal prepaidAmount;

    /** 账单开始时间 */
    private LocalDateTime billStartTime;

    /** 账单结束时间 */
    private LocalDateTime billEndTime;

    /** 共计天数 */
    private Integer totalDays;

    /** 护理等级名称 */
    private String lname;

    /** 房间类型名称 */
    private String typeName;

    /** 入住配置（详情用） */
    private CheckInConfigVO checkInConfigVo;

    /** 创建人姓名 */
    private String creator;

    /** 丙方姓名（费用账单创建人） */
    private String memberCreator;

    /** 关联订单号 */
    private String orderNo;

    /** 关联订单ID */
    private Long orderId;

    /** 备注 */
    private String remark;
}
