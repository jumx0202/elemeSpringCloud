package org.example.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务工具类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 设置缓存
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Set cache: key={}", key);
        } catch (Exception e) {
            log.error("Failed to set cache: key={}", key, e);
        }
    }
    
    /**
     * 设置缓存带过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.debug("Set cache with TTL: key={}, timeout={} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Failed to set cache with TTL: key={}", key, e);
        }
    }
    
    /**
     * 获取缓存
     */
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("Get cache: key={}, exists={}", key, value != null);
            return value;
        } catch (Exception e) {
            log.error("Failed to get cache: key={}", key, e);
            return null;
        }
    }
    
    /**
     * 获取缓存并转换类型
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        try {
            Object value = get(key);
            if (value != null && type.isAssignableFrom(value.getClass())) {
                return (T) value;
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to get cache with type: key={}, type={}", key, type, e);
            return null;
        }
    }
    
    /**
     * 删除缓存
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Delete cache: key={}", key);
        } catch (Exception e) {
            log.error("Failed to delete cache: key={}", key, e);
        }
    }
    
    /**
     * 批量删除缓存
     */
    public void delete(Collection<String> keys) {
        try {
            redisTemplate.delete(keys);
            log.debug("Delete cache batch: keys={}", keys);
        } catch (Exception e) {
            log.error("Failed to delete cache batch: keys={}", keys, e);
        }
    }
    
    /**
     * 检查缓存是否存在
     */
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Failed to check cache exists: key={}", key, e);
            return false;
        }
    }
    
    /**
     * 设置过期时间
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        try {
            redisTemplate.expire(key, timeout, unit);
            log.debug("Set expire: key={}, timeout={} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Failed to set expire: key={}", key, e);
        }
    }
    
    /**
     * 获取过期时间
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key);
            return expire != null ? expire : -1;
        } catch (Exception e) {
            log.error("Failed to get expire: key={}", key, e);
            return -1;
        }
    }
    
    /**
     * 递增
     */
    public long increment(String key) {
        try {
            Long result = redisTemplate.opsForValue().increment(key);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Failed to increment: key={}", key, e);
            return 0;
        }
    }
    
    /**
     * 递增指定步长
     */
    public long increment(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Failed to increment by delta: key={}, delta={}", key, delta, e);
            return 0;
        }
    }
    
    /**
     * 递减
     */
    public long decrement(String key) {
        try {
            Long result = redisTemplate.opsForValue().decrement(key);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Failed to decrement: key={}", key, e);
            return 0;
        }
    }
    
    /**
     * 设置Hash缓存
     */
    public void hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
            log.debug("Set hash cache: key={}, field={}", key, field);
        } catch (Exception e) {
            log.error("Failed to set hash cache: key={}, field={}", key, field, e);
        }
    }
    
    /**
     * 获取Hash缓存
     */
    public Object hGet(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("Failed to get hash cache: key={}, field={}", key, field, e);
            return null;
        }
    }
    
    /**
     * 删除Hash缓存
     */
    public void hDelete(String key, Object... fields) {
        try {
            redisTemplate.opsForHash().delete(key, fields);
            log.debug("Delete hash cache: key={}, fields={}", key, fields);
        } catch (Exception e) {
            log.error("Failed to delete hash cache: key={}, fields={}", key, fields, e);
        }
    }
    
    /**
     * 获取Hash的所有字段和值
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("Failed to get all hash cache: key={}", key, e);
            return null;
        }
    }
    
    /**
     * 设置List缓存
     */
    public void lPush(String key, Object value) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
            log.debug("Push to list cache: key={}", key);
        } catch (Exception e) {
            log.error("Failed to push to list cache: key={}", key, e);
        }
    }
    
    /**
     * 获取List缓存
     */
    public Object lPop(String key) {
        try {
            return redisTemplate.opsForList().leftPop(key);
        } catch (Exception e) {
            log.error("Failed to pop from list cache: key={}", key, e);
            return null;
        }
    }
    
    /**
     * 获取List范围缓存
     */
    public List<Object> lRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("Failed to get list range cache: key={}, start={}, end={}", key, start, end, e);
            return null;
        }
    }
    
    /**
     * 设置Set缓存
     */
    public void sAdd(String key, Object... values) {
        try {
            redisTemplate.opsForSet().add(key, values);
            log.debug("Add to set cache: key={}", key);
        } catch (Exception e) {
            log.error("Failed to add to set cache: key={}", key, e);
        }
    }
    
    /**
     * 获取Set缓存
     */
    public Set<Object> sMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("Failed to get set cache: key={}", key, e);
            return null;
        }
    }
    
    /**
     * 检查Set中是否存在值
     */
    public boolean sIsMember(String key, Object value) {
        try {
            Boolean isMember = redisTemplate.opsForSet().isMember(key, value);
            return isMember != null && isMember;
        } catch (Exception e) {
            log.error("Failed to check set member: key={}, value={}", key, value, e);
            return false;
        }
    }
    
    /**
     * 获取匹配的键
     */
    public Set<String> keys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            log.error("Failed to get keys by pattern: pattern={}", pattern, e);
            return null;
        }
    }
    
    /**
     * 清空所有缓存
     */
    public void flushAll() {
        try {
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            log.warn("Flush all cache");
        } catch (Exception e) {
            log.error("Failed to flush all cache", e);
        }
    }
} 