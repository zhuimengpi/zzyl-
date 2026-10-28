package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.OrderDTO;
import com.zhuimeng.dto.RefundDTO;

public interface OrderService {
    Result createOrder(OrderDTO orderDTO) throws InterruptedException;

    Result checkOrder(OrderDTO orderDTO);

    Result orderPage(Integer pageNum, Integer pageSize, String status);

    Result orderDetail(Long id);

    Result cancelOrder(Long orderId, String reason);

    Result deleteOrder(Long id);

    Result refundOrder(RefundDTO refundDTO);

    Result payOrder(Long orderId, Long tradingOrderNo);
}
