package com.zhuimeng.qcmanagement.controller.family;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.OrderDTO;
import com.zhuimeng.dto.RefundDTO;
import com.zhuimeng.qcmanagement.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 订单管理
* */

@RestController
@RequestMapping("/customer/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /** 创建订单
    * */
    @PostMapping()
    public Result createOrder(@RequestBody OrderDTO orderDTO) throws InterruptedException {
        return orderService.createOrder(orderDTO);
    }

    /** 订单校验
    * */
    @PostMapping("/check")
    public Result checkOrder(@RequestBody OrderDTO orderDTO){
        return orderService.checkOrder(orderDTO);
    }

    /** 订单列表
    * */
    @GetMapping("/order/page")
    public Result orderPage(Integer pageNum, Integer pageSize, String status){
        return orderService.orderPage(pageNum, pageSize, status);
    }

    /** 订单详情
    * */
    @GetMapping("/{id}")
    public Result orderDetail(@PathVariable("id") Long id){
        return orderService.orderDetail(id);
    }

    /** 取消订单
    * */
    @PostMapping("/{orderId}/cancel")
    public Result cancelOrder(@PathVariable("orderId") Long orderId, String reason){
        return orderService.cancelOrder(orderId, reason);
    }

    /** 删除订单
    * */
    @DeleteMapping("/{Id}")
    public Result deleteOrder(@PathVariable("Id") Long Id){
        return orderService.deleteOrder(Id);
    }

    /** 申请退款
    * */
    @PostMapping("/refund")
    public Result refundOrder(@RequestBody RefundDTO refundDTO){
        return orderService.refundOrder(refundDTO);
    }

    /** 支付确认
     * */
    @PutMapping("/{orderId}/pay")
    public Result payOrder(@PathVariable Long orderId, Long tradingOrderNo) {
        return orderService.payOrder(orderId, tradingOrderNo);
    }
}
