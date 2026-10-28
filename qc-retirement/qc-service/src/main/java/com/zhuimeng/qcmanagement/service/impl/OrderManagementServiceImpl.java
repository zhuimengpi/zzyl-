package com.zhuimeng.qcmanagement.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhuimeng.Result.Result;
import com.zhuimeng.Thread.UserHolder;
import com.zhuimeng.entity.Order;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.NursingTaskMapper;
import com.zhuimeng.qcmanagement.mapper.OrderMapper;
import com.zhuimeng.qcmanagement.mapper.TradingMapper;
import com.zhuimeng.qcmanagement.service.OrderManagementService;
import com.zhuimeng.vo.OrderPageVO;
import com.zhuimeng.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** 订单管理 Service 实现（员工端）
* */
@Service
public class OrderManagementServiceImpl implements OrderManagementService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private TradingMapper tradingMapper;
    @Autowired
    private NursingTaskMapper nursingTaskMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    //订单key
    private static final String key = RedisKeyProperties.ORDER_LIST;

    /** 分页查询订单记录 */
    @Override
    public Result search(Integer pageNum, Integer pageSize, String status,
                         String orderNo, String elderlyName, String creator,
                         String startTime, String endTime) {
        // 转换时间戳
        startTime = convertTimestamp(startTime);
        endTime = convertTimestamp(endTime);

        // 无筛选条件时，尝试从Redis获取缓存
        boolean noFilter = isNoFilter(status, orderNo, elderlyName, creator, startTime, endTime);
        if (noFilter) {
            String cacheKey = key + pageNum + ":" + pageSize;
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return Result.ok(cached);
            }
        }

        PageHelper.startPage(pageNum, pageSize);
        List<OrderPageVO> list = orderMapper.searchOrders(status, orderNo, elderlyName, creator, startTime, endTime);
        PageInfo<OrderPageVO> pageInfo = new PageInfo<>(list);

        Map<String, Object> data = new HashMap<>();
        data.put("records", list);
        data.put("pages", pageInfo.getPages());
        data.put("total", pageInfo.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        // 无筛选条件时缓存（5分钟）
        if (noFilter) {
            String cacheKey = key + pageNum + ":" + pageSize;
            redisTemplate.opsForValue().set(cacheKey, data, 5, TimeUnit.MINUTES);
        }

        return Result.ok(data);
    }

    /** 订单详情 */
    @Override
    public Result detail(Long orderId) {
        if (orderId == null) {
            return Result.fail("订单ID不能为空");
        }

        // 尝试从Redis获取
        String cacheKey = key + "detail:" + orderId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Result.ok(cached);
        }

        OrderVO orderVO = orderMapper.selectById(orderId);
        if (orderVO == null) {
            return Result.fail("订单不存在");
        }

        // 防止前端取值报错，null 对象补空 Map
        if (orderVO.getBedVo() == null) {
            orderVO.setBedVo(new HashMap<>());
        }
        if (orderVO.getMemberVo() == null) {
            orderVO.setMemberVo(new HashMap<>());
        }
        if (orderVO.getTradingVo() == null) {
            orderVO.setTradingVo(new com.zhuimeng.vo.TradingVO());
        }
        if (orderVO.getRefundRecordVo() == null) {
            orderVO.setRefundRecordVo(new com.zhuimeng.vo.RefundRecordVO());
        }
        if (orderVO.getNursingTaskVo() == null) {
            orderVO.setNursingTaskVo(new com.zhuimeng.vo.NursingTaskVO());
        }

        // 缓存（5分钟）
        redisTemplate.opsForValue().set(cacheKey, orderVO, 5, TimeUnit.MINUTES);

        return Result.ok(orderVO);
    }

    /** 取消订单 */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result cancel(Long orderId, String reason) {
        if (orderId == null) {
            return Result.fail("订单ID不能为空");
        }
        if (reason == null || reason.isBlank()) {
            return Result.fail("取消原因不能为空");
        }

        Order order = orderMapper.selectOrderById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (order.getStatus() == 4) {
            return Result.fail("订单已取消");
        }

        // 取消订单
        order.setStatus(4);
        order.setReason(reason);
        orderMapper.updateOrder(order);

        // 清除相关缓存
        clearOrderCache(orderId);

        return Result.ok("取消成功");
    }

    /** 分页查询退款记录 */
    @Override
    public Result queryRefundRecord(Integer pageNum, Integer pageSize, String refundStatus,
                                    String refundNoStr, String orderNo, String creator,
                                    String startTime, String endTime) {
        startTime = convertTimestamp(startTime);
        endTime = convertTimestamp(endTime);

        PageHelper.startPage(pageNum, pageSize);
        List<Map<String, Object>> list = orderMapper.searchRefundRecords(
                refundStatus, refundNoStr, orderNo, creator, startTime, endTime);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);

        Map<String, Object> data = new HashMap<>();
        data.put("records", list);
        data.put("pages", pageInfo.getPages());
        data.put("total", pageInfo.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        return Result.ok(data);
    }

    /** 根据退款单号查询退款结果 */
    @Override
    public Result queryRefund(String refundRecordNo) {
        if (refundRecordNo == null || refundRecordNo.isBlank()) {
            return Result.fail("退款单号不能为空");
        }

        Map<String, Object> refund = orderMapper.selectRefundByNo(refundRecordNo);
        if (refund == null) {
            return Result.fail("退款记录不存在");
        }

        return Result.ok(refund);
    }

    /** 发起退款 */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result refund(Long tradingOrderNo, String tradingChannel) {
        if (tradingOrderNo == null) {
            return Result.fail("交易单号不能为空");
        }

        // 查询订单
        Order order = orderMapper.selectByTradingOrderNo(tradingOrderNo);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (order.getStatus() == 0) {
            return Result.fail("订单未支付");
        }
        if (order.getStatus() == 4) {
            return Result.fail("订单已取消");
        }
        if (order.getStatus() == 5) {
            return Result.fail("订单已退款");
        }

        // 执行退款
        order.setStatus(5);
        order.setRefund(order.getAmount());
        order.setOCreateType(1);
        order.setReason(tradingChannel);
        orderMapper.updateOrder(order);

        // 插入退款记录
        String refundNo = "RF" + System.currentTimeMillis();
        String userId = UserHolder.getId();
        orderMapper.insertRefundRecord(refundNo, order.getOrderNo(),
                order.getAmount(), "2", tradingChannel,
                userId != null ? Long.valueOf(userId) : null);

        // 自动取消关联的护理任务
        cancelNursingTaskByOrder(order.getOrderNo(), tradingChannel);

        // 清除缓存
        clearOrderCache(order.getId());

        return Result.ok("退款成功");
    }

    /** 判断是否无筛选条件 */
    private boolean isNoFilter(String... filters) {
        for (String f : filters) {
            if (f != null && !f.isEmpty()) return false;
        }
        return true;
    }

    /** 毫秒时间戳转字符串 */
    private String convertTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return null;
        try {
            long ms = Long.parseLong(timestamp);
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault())
                    .toString().replace("T", " ");
        } catch (NumberFormatException e) {
            return timestamp;
        }
    }

    /** 清除订单相关缓存 */
    private void clearOrderCache(Long orderId) {
        // 清除详情缓存
        redisTemplate.delete(key + "detail:" + orderId);
        // 清除列表缓存（模糊删除，直接删前缀key）
        Set<String> keys = redisTemplate.keys(key + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /** 退款后自动取消护理任务 */
    private void cancelNursingTaskByOrder(String orderNo, String reason) {
        try {
            Map<String, Object> task = nursingTaskMapper.selectByOrderNo(orderNo);
            if (task != null) {
                Long taskId = Long.valueOf(task.get("id").toString());
                String cancelReason = reason != null ? reason : "订单退款";
                String userId = UserHolder.getId();
                nursingTaskMapper.updateTaskCancel(taskId, cancelReason,
                        userId != null ? Long.valueOf(userId) : 1L);
            }
        } catch (Exception e) {
            System.err.println("取消护理任务失败: " + e.getMessage());
        }
    }
}
