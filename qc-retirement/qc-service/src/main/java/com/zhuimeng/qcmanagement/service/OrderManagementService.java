package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;

/** 订单管理 Service（员工端）
* */
public interface OrderManagementService {

    /** 分页查询订单记录 */
    Result search(Integer pageNum, Integer pageSize, String status, String orderNo,
                  String elderlyName, String creator, String startTime, String endTime);

    /** 订单详情 */
    Result detail(Long orderId);

    /** 取消订单 */
    Result cancel(Long orderId, String reason);

    /** 分页查询退款记录 */
    Result queryRefundRecord(Integer pageNum, Integer pageSize, String refundStatus,
                             String refundNoStr, String orderNo, String creator,
                             String startTime, String endTime);

    /** 查询退款结果 */
    Result queryRefund(String refundRecordNo);

    /** 发起退款 */
    Result refund(Long tradingOrderNo, String tradingChannel);
}
