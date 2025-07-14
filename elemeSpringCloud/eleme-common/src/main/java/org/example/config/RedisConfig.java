package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis分布式缓存配置
 */
@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {
    
    /**
     * 配置RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 创建支持JSR310的ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        // 使用配置了JSR310的GenericJackson2JsonRedisSerializer
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        
        // 设置hash key 和value序列化模式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * 配置缓存管理器
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 创建支持JSR310的ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        // 配置序列化
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 默认缓存时间30分钟
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)))
                .disableCachingNullValues(); // 不缓存空值
        
        // 配置不同缓存的过期时间
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 用户缓存 - 1小时
        cacheConfigurations.put("user", config.entryTtl(Duration.ofHours(1)));
        
        // 商家缓存 - 2小时
        cacheConfigurations.put("business", config.entryTtl(Duration.ofHours(2)));
        
        // 食物缓存 - 30分钟
        cacheConfigurations.put("food", config.entryTtl(Duration.ofMinutes(30)));
        
        // 订单缓存 - 10分钟
        cacheConfigurations.put("order", config.entryTtl(Duration.ofMinutes(10)));
        
        // 支付缓存 - 5分钟
        cacheConfigurations.put("payment", config.entryTtl(Duration.ofMinutes(5)));
        
        // 验证码缓存 - 5分钟
        cacheConfigurations.put("captcha", config.entryTtl(Duration.ofMinutes(5)));
        
        // 热点数据缓存 - 1天
        cacheConfigurations.put("hotdata", config.entryTtl(Duration.ofDays(1)));
        
        // 统计缓存 - 1小时
        cacheConfigurations.put("statistics", config.entryTtl(Duration.ofHours(1)));
        
        // 配置缓存缓存 - 2小时
        cacheConfigurations.put("config", config.entryTtl(Duration.ofHours(2)));
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
    
    /**
     * 配置缓存键生成策略
     */
    @Bean
    public org.springframework.cache.interceptor.KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            for (Object obj : params) {
                sb.append(obj.toString());
            }
            return sb.toString();
        };
    }
} 