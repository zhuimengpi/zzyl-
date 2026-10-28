package com.zhuimeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 交易信息 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TradingVO {

    /** 微信支付参数JSON */
    private String placeOrderJson;

    /** 交易更新时间 */
    private LocalDateTime updateTime;
}
