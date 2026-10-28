package com.zhuimeng.qcmanagement.service.impl;

import com.github.pagehelper.PageHelper;
import com.zhuimeng.Result.Result;
import com.zhuimeng.Thread.UserHolder;
import com.zhuimeng.dto.OrderDTO;
import com.zhuimeng.dto.RefundDTO;
import com.zhuimeng.dto.ReservationDTO;
import com.zhuimeng.entity.Order;
import com.zhuimeng.entity.Trading;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.FamilyMapper;
import com.zhuimeng.qcmanagement.mapper.NursingTaskMapper;
import com.zhuimeng.qcmanagement.mapper.OrderMapper;
import com.zhuimeng.qcmanagement.mapper.TradingMapper;
import com.zhuimeng.qcmanagement.service.OrderService;
import com.zhuimeng.util.OrderNumberGenerator;
import com.zhuimeng.vo.OrderPageVO;
import com.zhuimeng.vo.OrderVO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.zhuimeng.vo.TradingVO;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private TradingMapper tradingMapper;
    @Autowired
    private NursingTaskMapper nursingTaskMapper;
    @Autowired
    private FamilyMapper familyMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    //创建线程池
    //static 所有线程共享一个对象
    private static final ExecutorService ORDER_TIME_EXECUTOR = Executors.newSingleThreadExecutor();  //单线程工厂

    //延时队列key
    String key = RedisKeyProperties.ORDER_TIME;
    //控制拉取线程的生死
    private volatile boolean running = true;

    /** 消费者
     * */
    @PostConstruct
    public void consumer() {
        // 阻塞队列
        RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue(key);

        // 消费者线程池
        Thread pullThread = new Thread(() -> {
            while (running) {
                try{
                    //阻塞，等待超时预约（获取预约ID）
                    String orderId = blockingQueue.take();
                    //线程（可异步）执行超时预约
                    ORDER_TIME_EXECUTOR.execute(() -> {
                        // 修改超时预约状态
                        System.out.println("超时预约ID：" + orderId);
                        orderMapper.updateTimeoutOrder(orderId);
                    });
                }catch (Exception e){
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        pullThread.setName("reservation-pull-thread");  //设置线程名称
        pullThread.setDaemon(false); // 非守护线程（只要pullThread还在运行，则JVM不会关闭）
        pullThread.start(); //启动线程
    }

    /** 自动关闭消费者
     * */
    @PreDestroy
    public void close() {
        // 1. 停止拉取（让拉取线程自己退出循环）
        running = false;

        // 2. 关闭工作线程池
        ORDER_TIME_EXECUTOR.shutdown();
        try {
            if (!ORDER_TIME_EXECUTOR.awaitTermination(30, TimeUnit.SECONDS)) {    //等待30秒
                ORDER_TIME_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            ORDER_TIME_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Autowired
    private RedissonClient redissonClient;
    /** Redisson 延时队列
     * */
    public void delayQueue(String orderId,OrderDTO orderDTO) throws InterruptedException {
        // 1. 计算过期时间
        // 预约时间
        LocalDateTime appointmentTime = orderDTO.getEstimatedArrivalTime();
        // 当前时间
        LocalDateTime now = LocalDateTime.now();
        // 计算时间差
        long delay = Duration.between(now, appointmentTime).toMillis();

        // 2. 创建延时队列
        // 阻塞队列
        RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue(key);
        // 延时队列
        RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);

        // 3. 添加到延时队列
        delayedQueue.offer(orderId, 30, TimeUnit.MINUTES);
    }

    //布隆过滤器
    @Autowired
    private RBloomFilter<String> bloomFilter;

    /** 创建订单
     * */
    @Override
    public Result createOrder(OrderDTO orderDTO) throws InterruptedException {
        // 生成订单交易号
        // 时间戳（毫秒） + 6位随机数，确保唯一性
        int tradingOrderNo = (int) (Math.random() * 900000) + 100000; // 6位随机

        // 创建订单
        Order order = new Order();
        BeanUtils.copyProperties(orderDTO, order);
        order.setIsRefund("NO");
        order.setTradingOrderNo(Long.valueOf(tradingOrderNo));
        order.setStatus(0);
        order.setPaymentStatus(1);
        order.setOrderNo(OrderNumberGenerator.generate());
        order.setViewStatus("0");

        // 插入数据库
        orderMapper.insert(order);
        String orderId = order.getId().toString();

        // 加入布隆过滤器
        bloomFilter.add(orderId);

        // Redisson 延时队列
        delayQueue(orderId, orderDTO);

        return Result.ok("下单成功");
    }

    /** 订单校验
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result checkOrder(OrderDTO orderDTO) {
        // 1. 参数校验
        if (orderDTO.getProjectId() == null || orderDTO.getElderId() == null) {
            return Result.fail("项目或老人信息不能为空");
        }
        if (orderDTO.getAmount() == null || orderDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.fail("订单金额无效");
        }

        // 2. 生成交易订单号
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 900000) + 100000;
        Long tradingOrderNo = Long.valueOf(String.valueOf(timestamp).substring(5) + random);

        // 3. 创建交易记录
        Trading trading = new Trading();
        trading.setTradingOrderNo(tradingOrderNo);
        trading.setTradingType("2");
        trading.setTradingState(1);
        trading.setTradingAmount(orderDTO.getAmount());
        trading.setTradingChannel("微信支付");
        trading.setIsRefund("NO");
        trading.setEnableFlag("1");
        trading.setCreateTime(LocalDateTime.now());
        tradingMapper.insert(trading);

        // 4. 生成统一下单参数（mock，后续接入微信支付后替换）
        String placeOrderJson = "{"
                + "\"timeStamp\":\"" + (System.currentTimeMillis() / 1000) + "\","
                + "\"nonceStr\":\"" + UUID.randomUUID().toString().replace("-", "").substring(0, 32) + "\","
                + "\"package\":\"prepay_id=mock" + tradingOrderNo + "\","
                + "\"paySign\":\"MOCK_SIGN_FOR_DEVELOPMENT\""
                + "}";

        // 5. 组装返回VO
        OrderVO orderVO = new OrderVO();
        orderVO.setAmount(orderDTO.getAmount());
        orderVO.setEstimatedArrivalTime(orderDTO.getEstimatedArrivalTime());
        orderVO.setCreateTime(LocalDateTime.now());
        orderVO.setTradingOrderNo(tradingOrderNo);

        TradingVO tradingVO = new TradingVO();
        tradingVO.setPlaceOrderJson(placeOrderJson);
        orderVO.setTradingVo(tradingVO);

        return Result.ok(orderVO);
    }

    /** 支付确认
      */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result payOrder(Long orderId, Long tradingOrderNo) {
        Order order = orderMapper.selectOrderById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (order.getPaymentStatus() != 1) {
            return Result.fail("订单状态异常");
        }
        // 更新订单为已支付
        order.setPaymentStatus(2);
        order.setStatus(1);
        orderMapper.updateOrder(order);
        // 更新交易记录
        if (tradingOrderNo != null && tradingOrderNo > 0) {
            Trading trading = tradingMapper.selectByTradingOrderNo(tradingOrderNo);
            if (trading != null) {
                trading.setTradingState(4);
                trading.setResultCode("SUCCESS");
                trading.setResultMsg("支付成功");
                tradingMapper.updateTrading(trading);
            }
        }

        // 支付成功后生成护理任务（任务类型=1 订单任务）
        generateNursingTask(order);

        return Result.okMsg("支付成功");
    }

    /** 订单列表
     * */
    @Override
    public Result orderPage(Integer pageNum, Integer pageSize, String status) {
         /*status   "" 查询 全部
                   0 查询 待支付
                  1 查询 待执行
                  2 查询 已执行
                  3 查询 已完成
                  4 查询 已关闭
                  5 查询 已退款
                  */

        // 查询Redis缓存
        String userId = UserHolder.getId();
        String cacheKey = RedisKeyProperties.ORDER_LIST + userId + ":" + pageNum + ":" + pageSize;
        if (status != null && !status.isEmpty()) {
            cacheKey += ":" + status;
        }
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Result.ok(cached);
        }

        //分页查询
        PageHelper.startPage(pageNum, pageSize);
        ArrayList<OrderPageVO> orderPageVOS = orderMapper.selectByStatus(status);

        // 存入Redis（5分钟）
        redisTemplate.opsForValue().set(cacheKey, orderPageVOS, 5, TimeUnit.MINUTES);

        return Result.ok(orderPageVOS);
    }

    /** 订单详情
     * */
    @Override
    public Result orderDetail(Long id) {
        //布隆查询
        if (!bloomFilter.contains(id.toString())) {
            return Result.fail("订单不存在");
        }

        // 查询Redis缓存
        String cacheKey = RedisKeyProperties.ORDER_LIST + "detail:" + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Result.ok(cached);
        }

        //获取订单
        OrderVO orderVO = orderMapper.selectById(id);

        if (orderVO == null) {
            return Result.fail("订单不存在");
        }

        // 防止前端取值报错，null 对象补空
        fillNullFields(orderVO);

        // 存入Redis（5分钟）
        redisTemplate.opsForValue().set(cacheKey, orderVO, 5, TimeUnit.MINUTES);

        return Result.ok(orderVO);
    }

    /** 补全 null 的嵌套对象，防止前端取值报错 */
    private void fillNullFields(OrderVO vo) {
        if (vo.getBedVo() == null) {
            vo.setBedVo(new java.util.HashMap<>());
        }
        if (vo.getMemberVo() == null) {
            vo.setMemberVo(new java.util.HashMap<>());
        }
        if (vo.getTradingVo() == null) {
            vo.setTradingVo(new com.zhuimeng.vo.TradingVO());
        }
        if (vo.getRefundRecordVo() == null) {
            vo.setRefundRecordVo(new com.zhuimeng.vo.RefundRecordVO());
        }
        if (vo.getNursingTaskVo() == null) {
            vo.setNursingTaskVo(new com.zhuimeng.vo.NursingTaskVO());
        }
    }

    /** 取消订单
     * */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result cancelOrder(Long orderId, String reason) {
        // 1. 查询订单
        Order order = orderMapper.selectOrderById(orderId);

        // 2. 查询订单状态
        if( order == null){
            return Result.fail("订单不存在");
        }
        if (order.getStatus() == 1) {
            return Result.fail("订单已取消");
        }

        // 3. 取消订单
        order.setStatus(4);
        order.setReason(reason);
        orderMapper.updateOrder(order);

        // 4. 清除缓存
        clearOrderRedisCache(orderId);

        return Result.ok("取消订单成功");
    }

    /** 生成护理任务（支付成功后调用） */
    private void generateNursingTask(Order order) {
        try {
            // 查询老人床位号
            String bedNumber = null;
            com.zhuimeng.entity.Elder elder = familyMapper.selectElderById(order.getElderId());
            if (elder != null) {
                bedNumber = elder.getBedNumber();
            }

            // 查询老人绑定的护理员
            Long nursingId = nursingTaskMapper.selectNursingIdByElderId(order.getElderId());

            // 插入护理任务
            Map<String, Object> taskParams = new HashMap<>();
            taskParams.put("nursingId", nursingId);
            taskParams.put("projectId", order.getProjectId());
            taskParams.put("elderId", order.getElderId());
            taskParams.put("bedNumber", bedNumber);
            taskParams.put("taskType", 1);  // 订单任务
            taskParams.put("estimatedServerTime", order.getEstimatedArrivalTime());
            taskParams.put("status", 1);    // 待执行
            taskParams.put("relNo", order.getOrderNo());
            taskParams.put("createBy", order.getCreateBy());
            nursingTaskMapper.insertNursingTask(taskParams);
        } catch (Exception e) {
            // 任务生成失败不影响支付结果，仅记录日志
            System.err.println("生成护理任务失败: " + e.getMessage());
        }
    }

    /** 清除订单Redis缓存 */
    private void clearOrderRedisCache(Long orderId) {
        String detailKey = RedisKeyProperties.ORDER_LIST + "detail:" + orderId;
        redisTemplate.delete(detailKey);
        // 清除列表缓存
        String userId = UserHolder.getId();
        Set<String> keys = redisTemplate.keys(RedisKeyProperties.ORDER_LIST + userId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /** 删除订单
     * */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result deleteOrder(Long id) {
        // 1. 查询订单
        Order order = orderMapper.selectOrderById(id);

        // 2. 查询订单状态
        if( order == null){
            return Result.fail("订单不存在");
        }

        // 3. 删除订单
        orderMapper.deleteOrder(order);

        return Result.ok("删除订单成功");
    }

    /** 申请退款
     * */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result refundOrder(RefundDTO refundDTO) {
        // 1. 查询订单
        Order order = orderMapper.selectOrderById(Long.valueOf(refundDTO.getProductOrderNo()));

        // 2. 判断订单
        if( order == null){
            return Result.fail("订单不存在");
        }
        if (order.getStatus() == 0) {
            return Result.fail("订单未支付");
        }
        if (order.getStatus() == 3) {
            return Result.fail("订单已完成");
        }
        if (order.getStatus() == 4) {
            return Result.fail("订单已取消");
        }
        if (order.getStatus() == 5) {
            return Result.fail("订单已退款");
        }

        // 3. 退款
        order.setStatus(5);
        order.setReason(refundDTO.getTradingChannel());
        order.setRefund(order.getAmount());
        order.setOCreateType(1);
        orderMapper.updateOrder(order);

        // 3.1 自动取消关联的护理任务
        cancelNursingTaskByOrder(order.getOrderNo(), refundDTO.getTradingChannel());

        // 4. 清除缓存
        clearOrderRedisCache(order.getId());

        return Result.ok("退款成功");
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
