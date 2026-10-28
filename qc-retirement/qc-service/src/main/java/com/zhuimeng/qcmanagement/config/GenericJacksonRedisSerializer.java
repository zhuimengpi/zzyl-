package com.zhuimeng.qcmanagement.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;

/**
 * 自定义通用 Jackson Redis 序列化器
 * <p>
 * 替代 Spring Data Redis 3.x 中已移除的 GenericJackson2JsonRedisSerializer。
 * 通过 Jackson ObjectMapper（已配置 DefaultTyping）实现任意对象类型的序列化与反序列化。
 * </p>
 */
public class GenericJacksonRedisSerializer implements RedisSerializer<Object> {

    private final ObjectMapper mapper;

    public GenericJacksonRedisSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public byte[] serialize(Object source) throws SerializationException {
        // 不再需要 null 检查，默认参数非空
        try {
            return mapper.writeValueAsBytes(source);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Could not write JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public Object deserialize(byte[] source) throws SerializationException {
        if (source == null || source.length == 0) {
            return null;
        }
        try {
            return mapper.readValue(source, Object.class);
        } catch (IOException e) {
            throw new SerializationException("Could not read JSON: " + e.getMessage(), e);
        }
    }
}
