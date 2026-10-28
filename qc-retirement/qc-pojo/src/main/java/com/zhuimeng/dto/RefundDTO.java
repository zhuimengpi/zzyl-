package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 退款DTO
* */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundDTO {
    // 订单ID
    private String productOrderNo;

    // 交易号
    private String tradingOrderNo;

    // 退款原因
    private String tradingChannel;
}
