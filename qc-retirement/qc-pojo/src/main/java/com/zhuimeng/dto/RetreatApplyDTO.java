package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 退住申请 V2 提交数据
* */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RetreatApplyDTO {

    /** 退住老人信息 */
    private RetreatElderDTO retreatElderDto;

    /** 账单JSON数据 */
    private String billJson;

    /** 解除日期 */
    private String releaseDate;

    /** 解除协议PDF地址 */
    private String releasePdfUrl;

    /** 退款凭证信息 */
    private RefundVoucherDTO refundVoucherDto;
}
