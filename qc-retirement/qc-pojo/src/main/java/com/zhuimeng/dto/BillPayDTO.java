package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 账单支付请求
* */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillPayDTO {

    /** 账单ID */
    private Long id;

    /** 账单ID（前端兼容字段） */
    private Long productOrderNo;

    /** 应付金额 */
    private BigDecimal payableAmount;

    /** 获取有效的账单ID */
    public Long getEffectiveId() {
        return id != null ? id : productOrderNo;
    }
}
