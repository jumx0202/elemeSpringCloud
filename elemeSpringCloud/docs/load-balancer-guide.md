# Spring Cloud LoadBalancer 配置指南

## 概述

本指南说明如何在饿了么Spring Cloud微服务项目中配置和使用Spring Cloud LoadBalancer实现客户端负载均衡。

## 架构说明

### 1. 负载均衡器类型

- **RoundRobinLoadBalancer**: 轮询策略，按顺序分配请求
- **RandomLoadBalancer**: 随机策略，随机选择实例
- **WeightedLoadBalancer**: 权重策略，根据实例权重分配请求
- **HealthCheckLoadBalancer**: 健康检查策略，过滤不健康实例

### 2. 负载均衡策略分配

| 服务 | 负载均衡策略 | 原因 |
|------|-------------|------|
| 用户服务 | RoundRobin | 请求分布均匀，适合认证场景 |
| 商家服务 | RoundRobin | 查询负载均匀分布 |
| 食物服务 | RoundRobin | 查询负载均匀分布 |
| 订单服务 | Random | 避免热点，分散创建订单压力 |
| 支付服务 | RoundRobin | 重要服务，确保稳定性 |
| 通知服务 | Random | 分散通知发送压力 |
| 验证码服务 | RoundRobin | 高频访问，均匀分布 |

## 配置详情

### 1. 基础配置

在`application.yml`中配置负载均衡：

```yaml
spring:
  cloud:
    loadbalancer:
      ribbon:
        enabled: false  # 禁用Ribbon
      cache:
        enabled: true   # 启用缓存
        ttl: 35s       # 缓存TTL
        capacity: 256  # 缓存容量
```

### 2. 服务级别配置

#### 针对特定服务配置负载均衡策略：

```yaml
# 为特定服务配置负载均衡策略
eleme-user-service:
  ribbon:
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RoundRobinRule
    
eleme-order-service:
  ribbon:
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule
```

### 3. 自定义负载均衡配置

```java
@Configuration
public class LoadBalancerConfig {
    
    @Bean
    public ReactorLoadBalancer<ServiceInstance> userServiceLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {
        
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        if ("eleme-user-service".equals(name)) {
            return new RoundRobinLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), 
                name);
        }
        return null;
    }
}
```

## 负载均衡策略详解

### 1. 轮询策略 (RoundRobin)

**使用场景**：
- 各实例性能相当
- 需要均匀分配请求
- 适合无状态服务

**配置示例**：
```java
@Bean
public ReactorLoadBalancer<ServiceInstance> roundRobinLoadBalancer(
        Environment environment,
        LoadBalancerClientFactory loadBalancerClientFactory) {
    return new RoundRobinLoadBalancer(
        loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), 
        name);
}
```

### 2. 随机策略 (Random)

**使用场景**：
- 避免热点问题
- 实例性能差异不大
- 简单的负载分散

**配置示例**：
```java
@Bean
public ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(
        Environment environment,
        LoadBalancerClientFactory loadBalancerClientFactory) {
    return new RandomLoadBalancer(
        loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), 
        name);
}
```

### 3. 权重策略 (Weighted)

**使用场景**：
- 实例性能差异较大
- 需要根据服务器能力分配负载
- 灰度发布场景

**配置示例**：
```java
@Bean
public ReactorLoadBalancer<ServiceInstance> weightedLoadBalancer(
        Environment environment,
        LoadBalancerClientFactory loadBalancerClientFactory) {
    return new WeightedLoadBalancer(
        loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), 
        name);
}
```

**权重设置**：
```yaml
# 在Nacos中设置实例权重
spring:
  cloud:
    nacos:
      discovery:
        metadata:
          weight: 3  # 设置当前实例权重
```

### 4. 健康检查策略 (HealthCheck)

**使用场景**：
- 需要过滤不健康实例
- 服务可用性要求高
- 自动故障转移

**配置示例**：
```java
@Bean
public ReactorLoadBalancer<ServiceInstance> healthCheckLoadBalancer(
        Environment environment,
        LoadBalancerClientFactory loadBalancerClientFactory) {
    return new HealthCheckLoadBalancer(
        loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), 
        name);
}
```

**健康状态设置**：
```yaml
# 在实例metadata中设置健康状态
spring:
  cloud:
    nacos:
      discovery:
        metadata:
          health.status: UP    # 健康状态：UP/DOWN
          enabled: true        # 是否启用：true/false
```

## OpenFeign 集成

### 1. 基础配置

```java
@FeignClient(name = "eleme-user-service", 
             fallback = UserClientFallback.class,
             configuration = FeignConfig.class)
public interface UserClient {
    
    @GetMapping("/api/user/{userId}")
    User getUserById(@PathVariable("userId") Integer userId);
}
```

### 2. Feign配置

```java
@Configuration
public class FeignConfig {
    
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 1000, 3);
    }
    
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(5000, 10000);
    }
}
```

### 3. 超时配置

```yaml
# Feign超时配置
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 10000
        loggerLevel: FULL
      eleme-user-service:
        connectTimeout: 3000
        readTimeout: 8000
```

## 监控和指标

### 1. 负载均衡指标

```yaml
# 启用负载均衡指标
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,loadbalancer
  endpoint:
    health:
      show-details: always
```

### 2. 关键指标

- **请求分布**: 各实例请求数量分布
- **响应时间**: 各实例平均响应时间
- **错误率**: 各实例错误率统计
- **健康检查**: 实例健康状态变化

### 3. 监控示例

```java
@Component
public class LoadBalancerMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public LoadBalancerMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    public void recordRequest(String serviceName, String instanceId) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("loadbalancer.request")
                .tag("service", serviceName)
                .tag("instance", instanceId)
                .register(meterRegistry));
    }
}
```

## 最佳实践

### 1. 策略选择

- **性能均衡**: 选择RoundRobin
- **避免热点**: 选择Random
- **性能差异**: 选择Weighted
- **高可用性**: 选择HealthCheck

### 2. 配置优化

- **缓存配置**: 合理设置缓存TTL
- **超时设置**: 根据服务特性设置超时
- **重试策略**: 配置合理的重试次数

### 3. 故障处理

- **健康检查**: 定期检查实例健康状态
- **自动摘除**: 自动摘除不健康实例
- **故障转移**: 快速切换到健康实例

## 部署配置

### 1. 多实例部署

```yaml
# 服务实例配置
spring:
  application:
    name: eleme-user-service
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        metadata:
          weight: 2
          zone: zone1
          version: v1.0
```

### 2. 跨区域部署

```yaml
# 跨区域负载均衡
spring:
  cloud:
    loadbalancer:
      zone: zone1
      cross-zone-enabled: true
```

### 3. 灰度发布

```yaml
# 灰度发布配置
spring:
  cloud:
    nacos:
      discovery:
        metadata:
          version: v1.1     # 新版本
          weight: 1         # 较低权重
          gray: true        # 灰度标识
```

## 故障排除

### 1. 常见问题

- **负载不均**: 检查负载均衡策略配置
- **实例不可用**: 检查健康检查配置
- **请求超时**: 调整超时配置
- **服务发现失败**: 检查注册中心连接

### 2. 调试方法

```bash
# 查看负载均衡状态
curl http://localhost:8080/actuator/loadbalancer

# 查看服务实例列表
curl http://localhost:8080/actuator/services

# 查看健康状态
curl http://localhost:8080/actuator/health
```

### 3. 日志分析

```yaml
# 启用负载均衡日志
logging:
  level:
    org.springframework.cloud.loadbalancer: DEBUG
    org.springframework.cloud.openfeign: DEBUG
```

## 总结

通过合理配置Spring Cloud LoadBalancer，可以实现高效、稳定的客户端负载均衡。选择合适的负载均衡策略，配置合理的参数，并做好监控和故障处理，是保障微服务系统高可用性的关键。 