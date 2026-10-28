package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.Order;
import com.zhuimeng.qcmanagement.annotation.AutoCreate;
import com.zhuimeng.qcmanagement.annotation.AutoUpdata;
import com.zhuimeng.vo.OrderPageVO;
import com.zhuimeng.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;

@Mapper
public interface OrderMapper {

    /**
     * 创建订单
     */
    @AutoCreate
    Integer insert(Order order);

    /** 订单列表
     * */
    ArrayList<OrderPageVO> selectByStatus(String status);

    /** 订单VO详情
     */
    OrderVO selectById(Long id);

    /** 订单详情
    * */
    Order selectOrderById(Long orderId);

    /** 更新订单
     */
    @AutoUpdata
    void updateOrder(Order order);

    /** 删除订单
    * */
    @AutoUpdata
    void deleteOrder(Order order);

    /** 根据交易单号查询订单
    * */
    Order selectByTradingOrderNo(Long tradingOrderNo);

    /** 精准支付过期
    * */
    void updateTimeoutOrder(String orderId);

    /** 支付过期定时任务
    * */
    void updateOrderStatus();

    // ==================== 员工端订单管理 ====================

    /** 订单搜索（多条件分页） */
    ArrayList<OrderPageVO> searchOrders(@org.apache.ibatis.annotations.Param("status") String status,
                                        @org.apache.ibatis.annotations.Param("orderNo") String orderNo,
                                        @org.apache.ibatis.annotations.Param("elderlyName") String elderlyName,
                                        @org.apache.ibatis.annotations.Param("creator") String creator,
                                        @org.apache.ibatis.annotations.Param("startTime") String startTime,
                                        @org.apache.ibatis.annotations.Param("endTime") String endTime);

    /** 退款记录分页查询 */
    java.util.List<java.util.Map<String, Object>> searchRefundRecords(
            @org.apache.ibatis.annotations.Param("refundStatus") String refundStatus,
            @org.apache.ibatis.annotations.Param("refundNoStr") String refundNoStr,
            @org.apache.ibatis.annotations.Param("orderNo") String orderNo,
            @org.apache.ibatis.annotations.Param("creator") String creator,
            @org.apache.ibatis.annotations.Param("startTime") String startTime,
            @org.apache.ibatis.annotations.Param("endTime") String endTime);

    /** 根据退款单号查询退款记录 */
    java.util.Map<String, Object> selectRefundByNo(@org.apache.ibatis.annotations.Param("refundRecordNo") String refundRecordNo);

    /** 插入退款记录 */
    void insertRefundRecord(@org.apache.ibatis.annotations.Param("refundNo") String refundNo,
                            @org.apache.ibatis.annotations.Param("productOrderNo") String productOrderNo,
                            @org.apache.ibatis.annotations.Param("refundAmount") java.math.BigDecimal refundAmount,
                            @org.apache.ibatis.annotations.Param("refundStatus") String refundStatus,
                            @org.apache.ibatis.annotations.Param("memo") String memo,
                            @org.apache.ibatis.annotations.Param("createBy") Long createBy);
}
