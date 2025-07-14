package org.example.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的分布式锁
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLock {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String LOCK_PREFIX = "lock:";
    private static final String UNLOCK_SCRIPT = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "return redis.call('del', KEYS[1]) " +
            "else " +
            "return 0 " +
            "end";
    
    /**
     * 尝试获取锁
     * 
     * @param key 锁的key
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 锁信息，获取失败返回null
     */
    public LockInfo tryLock(String key, long timeout, TimeUnit unit) {
        String lockKey = LOCK_PREFIX + key;
        String lockValue = generateLockValue();
        
        try {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, timeout, unit);
            if (success != null && success) {
                log.debug("Acquired lock: key={}, value={}", lockKey, lockValue);
                return new LockInfo(lockKey, lockValue);
            } else {
                log.debug("Failed to acquire lock: key={}", lockKey);
                return null;
            }
        } catch (Exception e) {
            log.error("Error acquiring lock: key={}", lockKey, e);
            return null;
        }
    }
    
    /**
     * 释放锁
     * 
     * @param lockInfo 锁信息
     * @return 是否释放成功
     */
    public boolean unlock(LockInfo lockInfo) {
        if (lockInfo == null) {
            return false;
        }
        
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
            Long result = redisTemplate.execute(script, 
                    Collections.singletonList(lockInfo.getKey()), 
                    lockInfo.getValue());
            
            boolean success = result != null && result == 1L;
            if (success) {
                log.debug("Released lock: key={}, value={}", lockInfo.getKey(), lockInfo.getValue());
            } else {
                log.warn("Failed to release lock: key={}, value={}", lockInfo.getKey(), lockInfo.getValue());
            }
            return success;
        } catch (Exception e) {
            log.error("Error releasing lock: key={}, value={}", lockInfo.getKey(), lockInfo.getValue(), e);
            return false;
        }
    }
    
    /**
     * 执行带锁的操作
     * 
     * @param key 锁的key
     * @param timeout 超时时间
     * @param unit 时间单位
     * @param task 要执行的任务
     * @return 执行结果
     */
    public <T> T executeWithLock(String key, long timeout, TimeUnit unit, LockTask<T> task) {
        LockInfo lockInfo = tryLock(key, timeout, unit);
        if (lockInfo == null) {
            throw new RuntimeException("Failed to acquire lock: " + key);
        }
        
        try {
            return task.execute();
        } finally {
            unlock(lockInfo);
        }
    }
    
    /**
     * 执行带锁的操作（无返回值）
     * 
     * @param key 锁的key
     * @param timeout 超时时间
     * @param unit 时间单位
     * @param task 要执行的任务
     */
    public void executeWithLock(String key, long timeout, TimeUnit unit, VoidLockTask task) {
        LockInfo lockInfo = tryLock(key, timeout, unit);
        if (lockInfo == null) {
            throw new RuntimeException("Failed to acquire lock: " + key);
        }
        
        try {
            task.execute();
        } finally {
            unlock(lockInfo);
        }
    }
    
    /**
     * 检查锁是否存在
     * 
     * @param key 锁的key
     * @return 是否存在
     */
    public boolean isLocked(String key) {
        String lockKey = LOCK_PREFIX + key;
        try {
            Boolean exists = redisTemplate.hasKey(lockKey);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Error checking lock existence: key={}", lockKey, e);
            return false;
        }
    }
    
    /**
     * 强制释放锁
     * 
     * @param key 锁的key
     * @return 是否释放成功
     */
    public boolean forceUnlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        try {
            Boolean result = redisTemplate.delete(lockKey);
            boolean success = result != null && result;
            if (success) {
                log.warn("Force unlocked: key={}", lockKey);
            }
            return success;
        } catch (Exception e) {
            log.error("Error force unlocking: key={}", lockKey, e);
            return false;
        }
    }
    
    /**
     * 生成锁的值
     */
    private String generateLockValue() {
        return Thread.currentThread().getName() + "-" + UUID.randomUUID().toString();
    }
    
    /**
     * 锁信息
     */
    public static class LockInfo {
        private final String key;
        private final String value;
        
        public LockInfo(String key, String value) {
            this.key = key;
            this.value = value;
        }
        
        public String getKey() {
            return key;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    /**
     * 带返回值的锁任务
     */
    @FunctionalInterface
    public interface LockTask<T> {
        T execute();
    }
    
    /**
     * 无返回值的锁任务
     */
    @FunctionalInterface
    public interface VoidLockTask {
        void execute();
    }
} 