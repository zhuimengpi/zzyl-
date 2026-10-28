package com.zhuimeng.util.Lock;

import com.zhuimeng.properties.RedisKeyProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Redis 分布式锁
* */

public class RedisLock implements Lock{
    private RedisTemplate redisTemplate;
    private String lockKey; // 被锁的key

    private String KEY = RedisKeyProperties.LOCK; // KEY + lockKey -> 锁的key
    private String VALUE = UUID.randomUUID().toString(); // 线程标识（区分不同系统的线程）

    /** 构造函数
    * */
    public RedisLock(RedisTemplate redisTemplate, String lockKey) {
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey;
    }

    // 加载释放锁的lua脚本
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    // 初始化静态代码块
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>(); // 创建脚本对象
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua")); // 指定脚本位置
        UNLOCK_SCRIPT.setResultType(Long.class);    // 指定返回类型
    }

    /** 获取锁（尝试）
     * */
    @Override
    public boolean tryGetLock() {
        // 1. 获取线程标识
        String threadId = VALUE + Thread.currentThread().getId();   //（区分同系统，不同线程）

        // 2. 判断并插入Redis
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(
                KEY + lockKey,
                threadId,
                1,
                TimeUnit.SECONDS
        );

        // 3. 判断是否获取成功
        if (flag != null && flag) {
            // 获取成功
            return true;
        }
        // 4. 获取失败
        return false;
    }

    /** 释放锁
     * */
    @Override
    public void releaseLock() {
        // 1. 获取要释放数据的KEY
        List<String> keys = Collections.singletonList(KEY + lockKey);

        // 2. 获取线程标识
        String threadId = VALUE + Thread.currentThread().getId();   //（区分同系统，不同线程）

        // 2. 判断并删除Redis（通过Lua脚本保证原子性）
        redisTemplate.execute(
                UNLOCK_SCRIPT,  // lua脚本
                keys,           // KEY
                threadId        // VALUE
        );
    }
}
