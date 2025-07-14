# 饿了么微服务监控和日志系统指南

## 概述

本指南详细介绍了饿了么微服务项目的监控和日志系统的配置和使用方法。我们使用Spring Boot Admin进行应用监控，ELK Stack进行日志收集和分析。

## 系统架构

### 监控系统
- **Spring Boot Admin Server**: 微服务监控中心
- **Prometheus**: 指标收集
- **Micrometer**: 指标生成和导出
- **Grafana**: 可视化展示（可选）

### 日志系统
- **Elasticsearch**: 日志存储和搜索
- **Logstash**: 日志处理和转换
- **Kibana**: 日志可视化和分析
- **Filebeat**: 日志收集

## 快速开始

### 1. 启动监控服务

```bash
# 进入监控服务目录
cd elemeSpringCloud/eleme-monitor

# 启动监控服务
mvn spring-boot:run

# 或者使用Docker
docker-compose up -d
```

监控服务将在 http://localhost:8009 启动，使用用户名 `admin` 和密码 `admin` 登录。

### 2. 启动ELK Stack

```bash
# 进入ELK配置目录
cd elemeSpringCloud/infrastructure/elk

# 启动ELK Stack
docker-compose up -d

# 检查服务状态
docker-compose ps
```

各服务访问地址：
- **Elasticsearch**: http://localhost:9200
- **Kibana**: http://localhost:5601
- **Logstash**: http://localhost:5000 (TCP), http://localhost:5044 (Beats)

### 3. 配置微服务

为每个微服务添加监控和日志配置：

#### 3.1 添加依赖

```xml
<!-- Spring Boot Admin Client -->
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-client</artifactId>
</dependency>

<!-- Micrometer Prometheus -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<!-- Logstash Encoder -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
</dependency>
```

#### 3.2 配置application.yml

```yaml
spring:
  boot:
    admin:
      client:
        url: http://localhost:8009
        instance:
          prefer-ip: true
        username: admin
        password: admin

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    root: INFO
    org.example: DEBUG
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%logger{50}] - %msg%n"
  file:
    name: logs/${spring.application.name}.log
    max-size: 100MB
    max-history: 30
```

#### 3.3 配置Logback

创建 `src/main/resources/logback-spring.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <!-- 控制台输出 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>
    
    <!-- 文件输出 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/${spring.application.name}.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%logger{50}] - %msg%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/${spring.application.name}.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
    </appender>
    
    <!-- Logstash输出 -->
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>localhost:5000</destination>
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <loggerName/>
                <message/>
                <mdc/>
                <arguments/>
                <stackTrace/>
                <pattern>
                    <pattern>
                        {
                            "service": "${spring.application.name:-unknown}",
                            "environment": "${spring.profiles.active:-dev}"
                        }
                    </pattern>
                </pattern>
            </providers>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
        <appender-ref ref="LOGSTASH"/>
    </root>
</configuration>
```

## 监控功能

### 1. 应用状态监控

Spring Boot Admin提供以下监控功能：

- **应用状态**: 运行状态、启动时间、版本信息
- **健康检查**: 数据库连接、Redis连接、磁盘空间等
- **性能指标**: CPU使用率、内存使用率、GC信息
- **HTTP追踪**: 请求响应时间、错误率
- **日志级别**: 动态修改日志级别

### 2. 自定义指标

在代码中添加自定义指标：

```java
@Component
public class CustomMetrics {
    
    private final Counter orderCounter;
    private final Timer orderProcessingTimer;
    private final Gauge onlineUsers;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.orderCounter = Counter.builder("eleme.orders.total")
                .description("Total orders")
                .register(meterRegistry);
        
        this.orderProcessingTimer = Timer.builder("eleme.orders.processing.time")
                .description("Order processing time")
                .register(meterRegistry);
        
        this.onlineUsers = Gauge.builder("eleme.users.online")
                .description("Online users")
                .register(meterRegistry, this, CustomMetrics::getOnlineUserCount);
    }
    
    public void incrementOrderCount() {
        orderCounter.increment();
    }
    
    public void recordOrderProcessingTime(long timeInMs) {
        orderProcessingTimer.record(timeInMs, TimeUnit.MILLISECONDS);
    }
    
    private double getOnlineUserCount() {
        // 实际业务逻辑
        return 100.0;
    }
}
```

### 3. 告警配置

Spring Boot Admin支持多种告警方式：

```yaml
spring:
  boot:
    admin:
      notify:
        mail:
          enabled: true
          from: admin@eleme.com
          to: dev-team@eleme.com
          subject: "【警告】${spring.application.name} 服务异常"
```

## 日志管理

### 1. 日志级别

- **ERROR**: 系统错误，需要立即处理
- **WARN**: 警告信息，可能影响系统运行
- **INFO**: 一般信息，业务流程记录
- **DEBUG**: 调试信息，开发环境使用

### 2. 日志格式

标准日志格式：
```
2024-01-15 10:30:45 [http-nio-8001-exec-1] INFO  [o.e.c.UserController] - 用户登录成功: user123
```

JSON格式日志（发送到Logstash）：
```json
{
  "@timestamp": "2024-01-15T10:30:45.123Z",
  "level": "INFO",
  "logger": "org.example.controller.UserController",
  "message": "用户登录成功: user123",
  "service": "eleme-user-service",
  "environment": "dev",
  "thread": "http-nio-8001-exec-1"
}
```

### 3. 日志查询

在Kibana中可以使用以下查询：

```
# 查询错误日志
level: ERROR

# 查询特定服务日志
service: "eleme-user-service"

# 查询包含异常的日志
has_exception: true

# 查询HTTP请求日志
http_method: POST AND http_path: "/api/user/login"

# 时间范围查询
@timestamp: [2024-01-15T00:00:00 TO 2024-01-15T23:59:59]
```

## 性能优化

### 1. 日志性能优化

```yaml
logging:
  level:
    # 减少不必要的DEBUG日志
    org.springframework.web: INFO
    org.hibernate.SQL: WARN
    
  # 异步日志
  async:
    enabled: true
    queue-size: 1024
    discarding-threshold: 256
```

### 2. 监控性能优化

```yaml
management:
  metrics:
    export:
      prometheus:
        step: 10s  # 降低采集频率
  endpoint:
    health:
      cache:
        time-to-live: 10s  # 缓存健康检查结果
```

## 故障排查

### 1. 常见问题

#### 问题1: 服务无法注册到监控中心
**解决方案**:
1. 检查网络连接
2. 确认监控服务地址配置正确
3. 检查用户名密码是否正确

#### 问题2: 日志无法发送到Logstash
**解决方案**:
1. 检查Logstash服务状态
2. 确认端口配置正确
3. 查看应用日志中的错误信息

#### 问题3: Kibana无法显示日志
**解决方案**:
1. 检查索引模式是否正确
2. 确认时间范围设置
3. 检查Elasticsearch中是否有数据

### 2. 健康检查

```bash
# 检查Elasticsearch健康状态
curl -X GET "localhost:9200/_cluster/health?pretty"

# 检查Logstash状态
curl -X GET "localhost:9600/_node/stats?pretty"

# 检查应用健康状态
curl -X GET "localhost:8001/actuator/health"
```

## 最佳实践

### 1. 日志记录规范

```java
@RestController
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @PostMapping("/login")
    public ResponseEntity<UserVO> login(@RequestBody UserLoginDTO loginDto) {
        logger.info("用户登录请求: {}", loginDto.getUsername());
        
        try {
            UserVO user = userService.login(loginDto);
            logger.info("用户登录成功: {}", loginDto.getUsername());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("用户登录失败: {}, 错误: {}", loginDto.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
```

### 2. 监控指标规范

- 使用有意义的指标名称
- 添加适当的标签
- 避免高基数标签
- 定期清理无用指标

### 3. 告警策略

- 设置合适的告警阈值
- 避免告警风暴
- 根据业务重要性分级告警
- 建立告警处理流程

## 扩展功能

### 1. 集成APM工具

可以集成SkyWalking或Zipkin进行分布式追踪：

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```

### 2. 集成Grafana

使用Grafana创建更丰富的监控仪表板：

```yaml
# docker-compose.yml
grafana:
  image: grafana/grafana
  ports:
    - "3000:3000"
  environment:
    - GF_SECURITY_ADMIN_PASSWORD=admin
```

### 3. 集成告警系统

集成钉钉、微信等告警通知：

```java
@Component
public class CustomNotifier extends AbstractStatusChangeNotifier {
    
    @Override
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        return Mono.fromRunnable(() -> {
            // 发送钉钉通知
            sendDingTalkNotification(event, instance);
        });
    }
}
```

## 总结

通过本指南，您可以：

1. 搭建完整的监控和日志系统
2. 实现微服务的统一监控
3. 集中化日志管理和分析
4. 快速定位和解决问题
5. 建立完善的告警机制

监控和日志系统是微服务架构中不可或缺的部分，能够帮助开发团队更好地了解系统运行状态，及时发现和解决问题，提高系统的可靠性和稳定性。 