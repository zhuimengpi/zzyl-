package com.zhuimeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 入住申请VO
* */

public class CheckInVO {
    /** 退住详情 - 退款凭证 VO
    * */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RefundVoucherVO {

        /** 退款凭证URL */
        private String refundVoucherUrl;

        /** 提交时间 */
        private String createTime;

        /** 退款方式/支付渠道 */
        private String tradingChannel;

        /** 退款备注 */
        private String remark;
    }
}
