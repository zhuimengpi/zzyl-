package com.zhuimeng.qcmanagement.config;

import org.redisson.Redisson;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Redisson 配置
* */

@Configuration
public class RedissonConfig {
    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private int port;

    @Value("${redis.password}")
    private String password;

    @Value("${redis.database}")
    private int database;

    @Bean
    public RedissonClient redissonClient() {
        // 配置类
        Config config = new Config();
        // 添加Redis地址
        // 可用config.useClusterServersConfig()来设置集群地址
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setPassword(password)
                .setDatabase(database);
        // 创建客户端
        return Redisson.create(config);
    }

    /** 布隆过滤器
    */
    @Bean
    public RBloomFilter<String> bloomFilter(RedissonClient redissonClient) {
        // 创建布隆过滤器
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("zzylFilter"); //过滤器名称

        // 初始化布隆过滤器，预计元素为100000000L，期望误差率为3%
        bloomFilter.tryInit(100000000L, 0.03);

        return bloomFilter;
    }
}
