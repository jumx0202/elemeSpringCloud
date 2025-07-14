# Sentinel服务降级和熔断配置指南

## 概述

本指南说明如何在饿了么Spring Cloud微服务项目中配置Sentinel实现服务降级和熔断。

## 架构说明

### 1. 核心组件

- **Sentinel Dashboard**: 可视化的规则配置和监控平台
- **Sentinel Core**: 核心限流熔断库
- **Spring Cloud Alibaba Sentinel**: Spring Cloud集成组件

### 2. 规则类型

- **限流规则**: 控制访问频率，防止系统过载
- **熔断规则**: 在服务异常时自动熔断，保护系统
- **系统规则**: 系统级别的保护规则
- **热点参数规则**: 针对热点参数的限流
- **授权规则**: 访问控制规则

## 配置详情

### 1. 服务配置

每个服务都需要在`application.yml`中添加Sentinel配置：

```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8080
        port: 8719
      eager: true
      web-context-unify: false
      
# Sentinel规则配置
sentinel:
  flow:
    enabled: true
  degrade:
    enabled: true
  system:
    enabled: true
```

### 2. 限流规则

各服务的QPS限制：

| 服务 | QPS限制 | 说明 |
|------|---------|------|
| 用户服务 | 100 | 用户认证和管理 |
| 商家服务 | 150 | 商家信息查询 |
| 食物服务 | 200 | 食物信息查询 |
| 订单服务 | 80 | 订单处理 |
| 支付服务 | 50 | 支付处理 |
| 通知服务 | 120 | 通知发送 |
| 验证码服务 | 300 | 验证码生成 |

### 3. 熔断规则

关键业务的熔断配置：

#### 订单创建熔断
- 资源名：`createOrder`
- 熔断策略：慢调用比例
- 响应时间阈值：100ms
- 慢调用比例：50%
- 熔断时长：10s
- 最小请求数：5

#### 支付处理熔断
- 资源名：`processPayment`
- 熔断策略：慢调用比例
- 响应时间阈值：200ms
- 慢调用比例：60%
- 熔断时长：15s
- 最小请求数：3

#### 邮件发送熔断
- 资源名：`sendEmail`
- 熔断策略：慢调用比例
- 响应时间阈值：500ms
- 慢调用比例：40%
- 熔断时长：5s
- 最小请求数：3

### 4. 系统规则

系统级别保护：

- **系统负载**：最高3.0
- **平均响应时间**：25ms
- **最大线程数**：10
- **QPS**：20
- **CPU使用率**：70%

## 服务降级实现

### 1. 注解方式

```java
@SentinelResource(value = "createOrder", fallback = "createOrderFallback")
public OrderResponse createOrder(OrderRequest request) {
    // 业务逻辑
}

public OrderResponse createOrderFallback(OrderRequest request, Throwable ex) {
    // 降级逻辑
    return new OrderResponse("系统繁忙，请稍后再试");
}
```

### 2. 配置类方式

通过`SentinelConfig`类自动加载规则：

```java
@Configuration
public class SentinelConfig {
    @Bean
    public CommandLineRunner initSentinelRules() {
        return args -> {
            // 初始化限流规则
            initFlowRules();
            // 初始化熔断规则
            initDegradeRules();
            // 初始化系统规则
            initSystemRules();
        };
    }
}
```

## 监控和管理

### 1. Sentinel Dashboard

访问地址：`http://localhost:8080`

功能：
- 实时监控
- 规则配置
- 流量控制
- 熔断管理

### 2. 日志监控

各服务会记录Sentinel相关日志：

```
2024-01-01 10:00:00 [WARN] 触发限流规则: FlowException
2024-01-01 10:00:01 [WARN] 触发熔断规则: DegradeException
```

## 异常处理

### 1. 全局异常处理

`SentinelExceptionHandler`处理所有Sentinel异常：

- **FlowException**: 限流异常
- **DegradeException**: 熔断异常
- **SystemBlockException**: 系统保护异常
- **ParamFlowException**: 热点参数限流异常
- **AuthorityException**: 授权异常

### 2. 响应格式

```json
{
    "success": false,
    "code": 429,
    "message": "请求过于频繁，请稍后再试",
    "type": "FLOW_LIMIT",
    "timestamp": 1640995200000
}
```

## 最佳实践

### 1. 规则配置

- **渐进式配置**: 从宽松到严格
- **业务区分**: 不同业务设置不同规则
- **动态调整**: 根据实际情况调整规则

### 2. 降级策略

- **快速失败**: 立即返回错误
- **默认值**: 返回缓存或默认数据
- **异步处理**: 将请求放入队列异步处理

### 3. 监控告警

- **关键指标**: QPS、响应时间、成功率
- **告警机制**: 异常情况及时通知
- **自动恢复**: 熔断后自动恢复检测

## 部署和运维

### 1. 启动顺序

1. 启动基础设施（MySQL、Redis、Nacos）
2. 启动Sentinel Dashboard
3. 启动各个微服务
4. 验证Sentinel规则生效

### 2. 配置文件

Sentinel规则配置文件：`infrastructure/sentinel/sentinel-rules.json`

### 3. 监控命令

```bash
# 查看Sentinel日志
docker logs sentinel-dashboard

# 查看服务状态
curl http://localhost:8080/sentinel/api/overview
```

## 故障排除

### 1. 常见问题

- **规则不生效**: 检查Dashboard连接
- **熔断误触**: 调整熔断阈值
- **限流过严**: 调整QPS限制

### 2. 调试方法

- 查看Sentinel Dashboard
- 检查应用日志
- 验证规则配置

### 3. 性能优化

- 合理设置规则参数
- 优化业务逻辑
- 使用缓存减少请求

## 总结

通过Sentinel的服务降级和熔断机制，可以有效保护微服务系统的稳定性和可用性。合理配置规则，及时监控和调整，是保障系统高可用的关键。 