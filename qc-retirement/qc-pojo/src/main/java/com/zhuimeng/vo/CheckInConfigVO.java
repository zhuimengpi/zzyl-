package com.zhuimeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 入住配置VO（账单详情用）
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CheckInConfigVO {

    /** 床位费用（元/月） */
    private BigDecimal bedCost;

    /** 护理费用（元/月） */
    private BigDecimal nursingCost;

    /** 其他费用（元/月） */
    private BigDecimal otherCost;

    /** 医保支付（元/月） */
    private BigDecimal medicalInsurancePayment;

    /** 政府补贴（元/月） */
    private BigDecimal governmentSubsidy;

    /** 每月应付 */
    private BigDecimal monthCost;

    /** 添加项小计（护理+床位+其他） */
    private BigDecimal add1;

    /** 扣减项小计（医保+补贴） */
    private BigDecimal add2;

    /** 费用开始时间 */
    private LocalDateTime costStartTime;
}
