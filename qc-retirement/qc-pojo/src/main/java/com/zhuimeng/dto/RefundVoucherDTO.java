package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 退住申请 - 退款凭证
* */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundVoucherDTO {

    /** 退款方式/支付渠道 */
    private String tradingChannel;

    /** 退款凭证URL */
    private String refundVoucherUrl;

    /** 退款备注 */
    private String remark;

    /** 老人ID */
    private Long elderId;
}
