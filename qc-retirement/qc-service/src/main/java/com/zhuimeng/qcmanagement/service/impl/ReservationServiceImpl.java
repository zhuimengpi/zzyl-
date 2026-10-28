package com.zhuimeng.qcmanagement.service.impl;

import com.github.pagehelper.PageHelper;
import com.zhuimeng.Result.Result;
import com.zhuimeng.Thread.UserHolder;
import com.zhuimeng.dto.ReservationDTO;
import com.zhuimeng.entity.Reservation;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.ReservationMapper;
import com.zhuimeng.qcmanagement.service.ReservationService;
import com.zhuimeng.vo.ReservationVO;
import com.zhuimeng.vo.TimeSlotVO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ReservationServiceImpl implements ReservationService {
    @Autowired
    private ReservationMapper reservationMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    // 消费者组名（多实例共享同一个组）
    private static final String RESERVATION_GROUP = "reservation:group";
    // 消费者名（组内所有实例共用，任一实例都能读到 PEL 里的待确认消息，避免实例重启后消息无人处理）
    private static final String RESERVATION_CONSUMER = "reservation:consumer";

    // 消息队列key
    String streamKey = RedisKeyProperties.RESERVATION_LIST;

    //控制拉取线程的生死
    private volatile boolean running = true;
    /** 消费者
    * */
    @PostConstruct
    public void consumer() {
        // 创建消费者组（多实例只有第一个能创建成功，其余报 BUSYGROUP 忽略）
        try {
            redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), RESERVATION_GROUP);
        } catch (Exception e) {
            // 组已存在，忽略
        }

        Thread pullThread = new Thread(() -> {
            while (running) {
                try {
                    long now = System.currentTimeMillis();

                    // 1. 读新消息（XREADGROUP ... >，阻塞 2s，没消息时正好当轮询间隔）
                    List<MapRecord<String, Object, Object>> messages = redisTemplate.opsForStream().read(
                            Consumer.from(RESERVATION_GROUP, RESERVATION_CONSUMER),
                            StreamReadOptions.empty().count(100).block(Duration.ofSeconds(2)),
                            StreamOffset.create(streamKey, ReadOffset.lastConsumed()));
                    handle(messages, now);

                    // 2. 读本消费者 PEL 中未确认的消息（未到期的都留在这里，到期才处理）
                    List<MapRecord<String, Object, Object>> pending = redisTemplate.opsForStream().read(
                            Consumer.from(RESERVATION_GROUP, RESERVATION_CONSUMER),
                            StreamReadOptions.empty().count(100),
                            StreamOffset.create(streamKey, ReadOffset.from("0")));
                    handle(pending, now);

                } catch (Exception e) {
                    log.error("消费者循环异常，线程退出", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        pullThread.setName("reservation-pull-thread");  //设置线程名称
        pullThread.setDaemon(false); // 非守护线程（只要pullThread还在运行，则JVM不会关闭）
        pullThread.start(); //启动线程
    }

    /** 处理一批消息：到期的更新 + XACK，未到期的留在 PEL 下轮再查
    * */
    private void handle(List<MapRecord<String, Object, Object>> messages, long now) {
        for (MapRecord<String, Object, Object> record : messages) {
            String msgId = record.getId().getValue();  // 消息ID
            Map<Object, Object> fields = record.getValue(); // 字段Map

            Object reservationIdObj = fields.get("reservationId");
            Object timestampObj = fields.get("timestamp");
            if (reservationIdObj == null || timestampObj == null) {
                // 字段缺失，直接确认丢弃，避免一直卡在 PEL
                redisTemplate.opsForStream().acknowledge(streamKey, RESERVATION_GROUP, msgId);
                continue;
            }

            try {
                String reservationId = reservationIdObj.toString();
                long expireAt = Long.parseLong(timestampObj.toString());

                // 仅处理已到期的预约
                if (expireAt <= now) {
                    log.info("超时预约id:{}", reservationId);
                    // 幂等：UPDATE ... WHERE status=0，重复执行无害
                    reservationMapper.updateTimeoutReservation(reservationId);
                    // 确认消费（XACK）
                    redisTemplate.opsForStream().acknowledge(streamKey, RESERVATION_GROUP, msgId);
                }
                // 未到期：不 ACK，留在 PEL 下轮再查
            } catch (Exception e) {
                // 单条消息处理失败不 ACK，留在 PEL 下轮重试，也不影响后续消息
                log.error("处理预约消息失败, msgId:{}", msgId, e);
            }
        }
    }

//    /** 消费者
//     * */
//    @PostConstruct
//    public void consumer() {
//        // 阻塞队列
//        RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue(key);
//
//        // 消费者线程池
//        Thread pullThread = new Thread(() -> {
//            while (running) {
//                try{
//                    //阻塞，等待超时预约（获取预约ID）
//                    String reservationId = blockingQueue.take();
//                    //线程（可异步）执行超时预约
//                    RESERVATION_TIME_EXECUTOR.execute(() -> {
//                        // 修改超时预约状态
//                        reservationMapper.updateTimeoutReservation(reservationId);
//                    });
//                }catch (Exception e){
//                    Thread.currentThread().interrupt();
//                    break;
//                }
//            }
//        });
//        pullThread.setName("reservation-pull-thread");  //设置线程名称
//        pullThread.setDaemon(false); // 非守护线程（只要pullThread还在运行，则JVM不会关闭）
//        pullThread.start(); //启动线程
//    }

    /** 自动关闭消费者
    * */
    @PreDestroy
    public void close() {
        // 停止拉取（让拉取线程自己退出循环）
        running = false;
    }

//    @Autowired
//    private RedissonClient redissonClient;
//    /** Redisson 延时队列
//    * */
//    public void delayQueue(String reservationId,ReservationDTO reservationDTO) throws InterruptedException {
//        // 1. 计算过期时间
//        // 预约时间
//        LocalDateTime appointmentTime = reservationDTO.getTime();
//        // 当前时间
//        LocalDateTime now = LocalDateTime.now();
//        // 计算时间差
//        long delay = Duration.between(now, appointmentTime).toMillis();
//
//        // 2. 创建延时队列
//        // 阻塞队列
//        RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue(key);
//        // 延时队列
//        RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
//
//        // 3. 添加到延时队列
//        delayedQueue.offer(reservationId, delay, TimeUnit.MILLISECONDS);
//    }

    /** 新增预约
     */
    @Transactional(rollbackFor = {Exception.class})        //所有异常都进行回滚
    @Override
    public Result addReservation(ReservationDTO reservationDTO) throws InterruptedException {
        // 校验必填字段
        if (reservationDTO.getName() == null || reservationDTO.getName().isBlank()) {
            return Result.fail("预约人姓名不能为空");
        }
        if (reservationDTO.getMobile() == null || reservationDTO.getMobile().isBlank()) {
            return Result.fail("手机号不能为空");
        }

        // 1. 查询预约（根据(name, mobile, time, visitor)，查询）
        Reservation reservation = reservationMapper.selectByReservation(reservationDTO);

        // 2. 判断预约
        if (reservation != null) {
            return Result.fail("该时段已预约");
        }

        // 3. 新增预约
        reservationMapper.insert(reservationDTO);
        String reservationId = String.valueOf(reservationDTO.getId());

        //预约时间戳:
        long timestamp = reservationDTO.getTime().toInstant(ZoneOffset.of("+8")).toEpochMilli();

        LocalDateTime now = LocalDateTime.now();
        // 加 1 分钟
        LocalDateTime future = now.plusMinutes(2);

        timestamp = future.toInstant(ZoneOffset.of("+8")).toEpochMilli();


        // 4. 将预约加入消息队列（自动ID，字段存 reservationId + 预约时间戳）
        redisTemplate.opsForStream().add(MapRecord.create(
                streamKey,
                Map.of("reservationId", reservationId, "timestamp", String.valueOf(timestamp))
        ));

//        // 4. Redisson 延时队列
//        delayQueue(reservationId, reservationDTO);

        return Result.ok("预约成功");
    }

    /** 预约时段统计
     * */
    @Override
    public Result countByTime(String time) {
        // 前端传毫秒时间戳，转为 yyyy-MM-dd 格式
        long timestamp = Long.parseLong(time);
        String dateStr = LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        ArrayList<TimeSlotVO> timeSlotVOS = reservationMapper.countByTime(dateStr);
        return Result.ok(timeSlotVOS);
    }

    /** 预约列表
     * */
    @Override
    public Result page(Integer pageNum, Integer pageSize, Integer status) {
        // 1. 查询预约列表
        PageHelper.startPage(pageNum, pageSize);
        ArrayList<ReservationVO> reservationVOS = reservationMapper.page(status);

        // 2. 判断预约列表
        if (reservationVOS == null || reservationVOS.size() == 0) {
            return Result.fail("没有预约记录");
        }

        return Result.ok(reservationVOS);
    }

    /** 取消预约
     * */
    @Override
    public Result cancelReservation(Long id) {
        // 1. 查询预约
        Reservation reservation = reservationMapper.selectById(id);

        // 2. 判断预约是否存在
        if (reservation == null) {
            return Result.fail("预约不存在");
        }

        // 3. 判断预约状态
        if (reservation.getStatus() != 0) {
            return Result.fail("预约已取消");
        }

        // 4. 查询取消次数
        String userId = UserHolder.getId();
        String key = RedisKeyProperties.COUNT_RESERVATION + userId;
        Integer count = cancell(key);

        // 5. 判断取消次数是否超过限制
        if( count > 3) {
            return Result.fail("取消次数超过限制");
        }

        // 6. 取消预约
        reservation.setStatus(2);
        reservationMapper.updateById(reservation);

        // 7. 更新取消次数
        if(count == 0) {
            // 插入数据
            redisTemplate.opsForValue().set(key, 1);
        }
        // 更新数据
        redisTemplate.opsForValue().increment(key, 1);
        // 设置过期时间为1天
        redisTemplate.expire(key, 1, TimeUnit.DAYS);

        // 8. 返回结果
        return Result.ok("取消成功");
    }

    /** 已取消次数查询
     * */
    @Override
    public Result cancelledCount() {
        // 查询取消次数
        String userId = UserHolder.getId();
        String key = RedisKeyProperties.COUNT_RESERVATION + userId;
        Integer count = cancell(key);

        // 返回结果
        return Result.ok(count);
    }

    /** Redis查询取消次数
     */
    private Integer cancell(String key) {
        Object num = redisTemplate.opsForValue().get(key);
        return num == null ? 0 : (Integer) num;
    }
}
