# Redis分布式缓存配置指南

## 概述

本指南说明如何在饿了么Spring Cloud微服务项目中配置和使用Redis实现分布式缓存，提高系统性能和数据访问效率。

## 架构说明

### 1. Redis集群架构

- **Redis Master**: 主节点，处理读写操作
- **Redis Slave**: 从节点，处理读操作，提供高可用性
- **Redis Sentinel**: 哨兵节点，监控主从节点，实现故障转移

### 2. 缓存层次结构

```
应用层
    ↓
本地缓存 (L1 Cache)
    ↓
分布式缓存 (L2 Cache - Redis)
    ↓
数据库 (MySQL)
```

## 缓存策略

### 1. 缓存分类和TTL配置

| 缓存类型 | 过期时间 | 使用场景 | 更新策略 |
|----------|----------|----------|----------|
| 用户缓存 | 1小时 | 用户信息、权限 | 用户更新时删除 |
| 商家缓存 | 2小时 | 商家信息、分类 | 商家更新时删除 |
| 食物缓存 | 30分钟 | 食物信息、价格 | 食物更新时删除 |
| 订单缓存 | 10分钟 | 订单详情 | 订单状态变更时删除 |
| 支付缓存 | 5分钟 | 支付状态 | 支付完成时删除 |
| 验证码缓存 | 5分钟 | 验证码 | 验证后删除 |
| 热点数据 | 1天 | 热门商家、推荐食物 | 定时更新 |
| 统计数据 | 1小时 | 访问统计、销量统计 | 定时更新 |
| 配置数据 | 2小时 | 系统配置、字典数据 | 配置更新时删除 |

### 2. 缓存模式

#### Cache-Aside（旁路缓存）
```java
// 读操作
public User getUserById(Integer userId) {
    // 1. 先从缓存查询
    User user = cacheService.get("user:" + userId, User.class);
    if (user != null) {
        return user;
    }
    
    // 2. 缓存未命中，从数据库查询
    user = userMapper.selectById(userId);
    if (user != null) {
        // 3. 写入缓存
        cacheService.set("user:" + userId, user, 1, TimeUnit.HOURS);
    }
    
    return user;
}

// 写操作
public void updateUser(User user) {
    // 1. 更新数据库
    userMapper.updateById(user);
    
    // 2. 删除缓存
    cacheService.delete("user:" + user.getId());
}
```

#### Write-Through（写透缓存）
```java
@Cacheable(value = "user", key = "#userId")
public User getUserById(Integer userId) {
    return userMapper.selectById(userId);
}

@CacheEvict(value = "user", key = "#user.id")
public void updateUser(User user) {
    userMapper.updateById(user);
}
```

#### Write-Behind（写回缓存）
```java
@Async
@CacheEvict(value = "user", key = "#user.id")
public void updateUserAsync(User user) {
    // 异步更新数据库
    userMapper.updateById(user);
}
```

## 缓存实现

### 1. Spring Cache注解

#### @Cacheable - 缓存查询结果
```java
@Service
public class BusinessService {
    
    @Cacheable(value = "business", key = "#businessId")
    public Business getBusinessById(Integer businessId) {
        return businessMapper.selectById(businessId);
    }
    
    @Cacheable(value = "business", key = "'list:' + #page + ':' + #size")
    public List<Business> getBusinessList(int page, int size) {
        return businessMapper.selectPage(page, size);
    }
}
```

#### @CachePut - 更新缓存
```java
@CachePut(value = "business", key = "#business.id")
public Business updateBusiness(Business business) {
    businessMapper.updateById(business);
    return business;
}
```

#### @CacheEvict - 删除缓存
```java
@CacheEvict(value = "business", key = "#businessId")
public void deleteBusiness(Integer businessId) {
    businessMapper.deleteById(businessId);
}

// 删除所有商家缓存
@CacheEvict(value = "business", allEntries = true)
public void clearAllBusinessCache() {
    // 清空所有商家缓存
}
```

#### @Caching - 组合操作
```java
@Caching(
    evict = {
        @CacheEvict(value = "business", key = "#business.id"),
        @CacheEvict(value = "business", key = "'list:*'")
    }
)
public void updateBusinessWithClearList(Business business) {
    businessMapper.updateById(business);
}
```

### 2. 手动缓存操作

```java
@Service
@RequiredArgsConstructor
public class FoodService {
    
    private final CacheService cacheService;
    private final FoodMapper foodMapper;
    
    public List<Food> getHotFoods() {
        String key = "hotfoods";
        
        // 尝试从缓存获取
        List<Food> foods = cacheService.get(key, List.class);
        if (foods != null) {
            return foods;
        }
        
        // 从数据库查询
        foods = foodMapper.selectHotFoods();
        
        // 写入缓存
        cacheService.set(key, foods, 30, TimeUnit.MINUTES);
        
        return foods;
    }
}
```

### 3. 分布式锁防止缓存击穿

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final DistributedLock distributedLock;
    private final CacheService cacheService;
    private final OrderMapper orderMapper;
    
    public Order getOrderById(Long orderId) {
        String cacheKey = "order:" + orderId;
        
        // 先尝试从缓存获取
        Order order = cacheService.get(cacheKey, Order.class);
        if (order != null) {
            return order;
        }
        
        // 使用分布式锁防止缓存击穿
        String lockKey = "lock:order:" + orderId;
        return distributedLock.executeWithLock(lockKey, 10, TimeUnit.SECONDS, () -> {
            // 双重检查
            Order cachedOrder = cacheService.get(cacheKey, Order.class);
            if (cachedOrder != null) {
                return cachedOrder;
            }
            
            // 从数据库查询
            Order dbOrder = orderMapper.selectById(orderId);
            if (dbOrder != null) {
                cacheService.set(cacheKey, dbOrder, 10, TimeUnit.MINUTES);
            }
            
            return dbOrder;
        });
    }
}
```

## 缓存预热和更新

### 1. 系统启动时预热

```java
@Component
@RequiredArgsConstructor
public class CacheWarmup {
    
    private final BusinessService businessService;
    private final FoodService foodService;
    
    @EventListener(ApplicationReadyEvent.class)
    public void warmupCache() {
        log.info("开始缓存预热...");
        
        // 预热热门商家
        businessService.getHotBusinesses();
        
        // 预热热门食物
        foodService.getHotFoods();
        
        // 预热商家分类
        businessService.getBusinessCategories();
        
        log.info("缓存预热完成");
    }
}
```

### 2. 定时更新热点数据

```java
@Component
@RequiredArgsConstructor
public class CacheUpdateScheduler {
    
    private final CacheService cacheService;
    private final StatisticsService statisticsService;
    
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void updateHotData() {
        log.info("开始更新热点数据缓存...");
        
        // 更新热门商家
        List<Business> hotBusinesses = statisticsService.getHotBusinesses();
        cacheService.set("hotBusinesses", hotBusinesses, 1, TimeUnit.DAYS);
        
        // 更新热门食物
        List<Food> hotFoods = statisticsService.getHotFoods();
        cacheService.set("hotFoods", hotFoods, 1, TimeUnit.DAYS);
        
        log.info("热点数据缓存更新完成");
    }
}
```

## 缓存监控和指标

### 1. 缓存指标

```java
@Component
@RequiredArgsConstructor
public class CacheMetrics {
    
    private final MeterRegistry meterRegistry;
    private final CacheManager cacheManager;
    
    @EventListener
    public void handleCacheGetEvent(CacheGetEvent event) {
        Counter.builder("cache.get")
            .tag("cache", event.getCacheName())
            .tag("result", event.isHit() ? "hit" : "miss")
            .register(meterRegistry)
            .increment();
    }
    
    @Scheduled(fixedRate = 60000) // 每分钟统计一次
    public void recordCacheSize() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof RedisCache) {
                // 记录缓存大小
                Gauge.builder("cache.size")
                    .tag("cache", cacheName)
                    .register(meterRegistry, cache, this::getCacheSize);
            }
        });
    }
    
    private double getCacheSize(Cache cache) {
        // 获取缓存大小的逻辑
        return 0.0;
    }
}
```

### 2. 缓存健康检查

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,caches
  endpoint:
    health:
      show-details: always
  health:
    redis:
      enabled: true
```

```java
@Component
public class CacheHealthIndicator implements HealthIndicator {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public Health health() {
        try {
            // 测试Redis连接
            redisTemplate.opsForValue().set("health:check", "OK", 1, TimeUnit.SECONDS);
            String result = (String) redisTemplate.opsForValue().get("health:check");
            
            if ("OK".equals(result)) {
                return Health.up()
                    .withDetail("redis", "Available")
                    .build();
            } else {
                return Health.down()
                    .withDetail("redis", "Unavailable")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("redis", "Error: " + e.getMessage())
                .build();
        }
    }
}
```

## 缓存最佳实践

### 1. 缓存键设计

```java
// 好的键命名规范
"user:12345"                    // 用户信息
"business:list:page:1:size:10"  // 商家列表
"food:business:12345"           // 商家的食物列表
"order:user:12345:status:paid"  // 用户的已支付订单

// 避免的键命名
"u12345"                        // 不明确
"businesslist"                  // 不够具体
"temp_data_123"                 // 临时数据应该有明确用途
```

### 2. 缓存雪崩防护

```java
@Service
public class CacheProtectionService {
    
    // 使用随机过期时间防止缓存雪崩
    public void setWithRandomExpire(String key, Object value, long baseSeconds) {
        long randomSeconds = (long) (baseSeconds * (0.8 + Math.random() * 0.4));
        cacheService.set(key, value, randomSeconds, TimeUnit.SECONDS);
    }
    
    // 使用互斥锁防止缓存击穿
    public <T> T getWithMutex(String key, Supplier<T> dataLoader) {
        T value = cacheService.get(key, (Class<T>) Object.class);
        if (value != null) {
            return value;
        }
        
        String lockKey = "mutex:" + key;
        return distributedLock.executeWithLock(lockKey, 10, TimeUnit.SECONDS, () -> {
            // 双重检查
            T cachedValue = cacheService.get(key, (Class<T>) Object.class);
            if (cachedValue != null) {
                return cachedValue;
            }
            
            // 从数据源加载
            T newValue = dataLoader.get();
            if (newValue != null) {
                setWithRandomExpire(key, newValue, 300); // 5分钟基础过期时间
            }
            
            return newValue;
        });
    }
}
```

### 3. 缓存穿透防护

```java
@Service
public class BloomFilterService {
    
    private final BloomFilter<String> bloomFilter;
    
    public BloomFilterService() {
        // 初始化布隆过滤器
        this.bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 1000000, 0.01);
    }
    
    public boolean mightContain(String key) {
        return bloomFilter.mightContain(key);
    }
    
    public void add(String key) {
        bloomFilter.put(key);
    }
}

@Service
@RequiredArgsConstructor
public class DataService {
    
    private final BloomFilterService bloomFilterService;
    private final CacheService cacheService;
    
    public Data getDataById(String id) {
        // 布隆过滤器检查
        if (!bloomFilterService.mightContain(id)) {
            return null; // 确定不存在
        }
        
        // 检查缓存
        Data data = cacheService.get("data:" + id, Data.class);
        if (data != null) {
            return data;
        }
        
        // 从数据库查询
        data = dataMapper.selectById(id);
        if (data != null) {
            cacheService.set("data:" + id, data, 30, TimeUnit.MINUTES);
        } else {
            // 缓存空值，防止穿透
            cacheService.set("data:" + id, "NULL", 5, TimeUnit.MINUTES);
        }
        
        return data;
    }
}
```

## 部署和配置

### 1. Redis集群配置

```yaml
# application.yml
spring:
  redis:
    # 单机配置
    host: localhost
    port: 6379
    password: redis123
    timeout: 10000ms
    
    # 连接池配置
    lettuce:
      pool:
        max-active: 20
        max-wait: -1ms
        max-idle: 10
        min-idle: 5
    
    # 集群配置
    cluster:
      nodes:
        - 192.168.1.101:6379
        - 192.168.1.102:6379
        - 192.168.1.103:6379
      max-redirects: 3
    
    # 哨兵配置
    sentinel:
      master: mymaster
      nodes:
        - 192.168.1.101:26379
        - 192.168.1.102:26379
        - 192.168.1.103:26379
```

### 2. 缓存配置

```yaml
# 缓存配置
spring:
  cache:
    type: redis
    redis:
      time-to-live: 30m
      key-prefix: "eleme:"
      use-key-prefix: true
      cache-null-values: false
```

## 故障排除

### 1. 常见问题

- **缓存雪崩**: 大量缓存同时过期
  - 解决方案: 随机过期时间、多级缓存
  
- **缓存击穿**: 热点数据过期，大量请求击穿到数据库
  - 解决方案: 分布式锁、热点数据永不过期
  
- **缓存穿透**: 查询不存在的数据
  - 解决方案: 布隆过滤器、缓存空值

### 2. 性能优化

- **序列化优化**: 使用高效的序列化方式
- **网络优化**: 使用管道、批量操作
- **内存优化**: 合理设置过期时间、压缩数据

### 3. 监控告警

```yaml
# 监控配置
management:
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: eleme-app
```

## 总结

通过合理配置Redis分布式缓存，可以显著提高系统性能和用户体验。关键是要根据业务特点选择合适的缓存策略，做好缓存预热和更新，并实施有效的防护措施防止缓存问题。 