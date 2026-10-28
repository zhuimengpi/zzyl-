package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.qcmanagement.service.OrderManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 订单管理（员工端）
* */

@RestController
public class OrderManagementController {
    @Autowired
    private OrderManagementService orderManagementService;

    // ==================== 订单管理 ====================

    /** 分页查询订单记录
     * 查询条件：orderNo, elderlyName, creator, status, startTime, endTime
     */
    @GetMapping("/sysUser/orders/search")
    public Result search(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String elderlyName,
            @RequestParam(required = false) String creator,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime
    ) {
        return orderManagementService.search(pageNum, pageSize, status, orderNo, elderlyName, creator, startTime, endTime);
    }

    /** 根据条件查询订单详情
     */
    @GetMapping("/sysUser/orders")
    public Result detail(@RequestParam(required = false) Long orderId) {
        return orderManagementService.detail(orderId);
    }

    /** 取消指定订单并填写取消原因
     */
    @PostMapping("/sysUser/orders/{orderId}/cancel")
    public Result cancel(
            @PathVariable Long orderId,
            @RequestParam String reason
    ) {
        return orderManagementService.cancel(orderId, reason);
    }

    // ==================== 退款管理 ====================

    /** 分页查询退款记录
     */
    @PostMapping("/sysUser/trade-common-feign/query-refund-record")
    public Result queryRefundRecord(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String refundStatus,
            @RequestParam(required = false) String refundNoStr,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String creator,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime
    ) {
        return orderManagementService.queryRefundRecord(pageNum, pageSize, refundStatus,
                refundNoStr, orderNo, creator, startTime, endTime);
    }

    /** 根据退款单号查询退款结果
     */
    @GetMapping("/sysUser/trade-common-feign/query-refund")
    public Result queryRefund(@RequestParam String refundRecordNo) {
        return orderManagementService.queryRefund(refundRecordNo);
    }

    /** 发起订单退款请求
     */
    @PostMapping("/sysUser/trade-common-feign/refund")
    public Result refund(@RequestBody Map<String, Object> params) {
        Long tradingOrderNo = Long.valueOf(params.get("tradingOrderNo").toString());
        String tradingChannel = (String) params.get("tradingChannel");
        return orderManagementService.refund(tradingOrderNo, tradingChannel);
    }
}
