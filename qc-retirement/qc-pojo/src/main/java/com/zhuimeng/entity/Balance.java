package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 老人余额
* */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Balance {

    /** 主键 */
    private Long id;

    /** 预交款余额 */
    private BigDecimal prepaidBalance;

    /** 押金金额 */
    private BigDecimal depositAmount;

    /** 欠费金额（元） */
    private BigDecimal arrearsAmount;

    /** 支付截止时间 */
    private LocalDateTime paymentDeadline;

    /** 状态（0：正常，1：退住  2：入住未缴押金） */
    private Integer status;

    /** 老人ID */
    private Long elderId;

    /** 老人姓名 */
    private String elderName;

    /** 床位号 */
    private String bedNo;

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
