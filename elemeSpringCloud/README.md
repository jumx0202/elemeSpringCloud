# 饿了么微服务项目总结

## 项目概述

本项目是一个基于Spring Cloud微服务架构的饿了么类外卖平台，实现了从单体SpringBoot应用到分布式微服务架构的完整迁移。项目采用了主流的微服务技术栈，包括服务注册与发现、API网关、服务治理、分布式缓存、监控与日志等完整的微服务解决方案。

### 项目特点

- 🚀 **完整的微服务架构**: 从单体应用拆分为9个独立的微服务
- 🔧 **企业级技术栈**: 使用Spring Cloud生态系统构建
- 🛡️ **高可用性**: 集成Sentinel熔断降级、负载均衡、分布式缓存
- 📊 **完善的监控体系**: Spring Boot Admin + ELK Stack
- 🔐 **安全认证**: JWT令牌认证、验证码服务
- 📱 **现代化前端**: Vue3 + TypeScript + Vant UI
- 🐳 **容器化部署**: Docker + Docker Compose
- 📖 **详细文档**: 完整的配置指南和API文档

## 技术架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                          前端应用                                │
│                      Vue3 + TypeScript                        │
└─────────────────────┬───────────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────────┐
│                     API网关                                     │
│               Spring Cloud Gateway                             │
│                    (端口: 8888)                                │
└─────────────────────┬───────────────────────────────────────────┘
                      │
      ┌───────────────┼───────────────┐
      │               │               │
┌─────▼─────┐  ┌─────▼─────┐  ┌─────▼─────┐
│  用户服务  │  │  商家服务  │  │  食物服务  │
│  (8001)   │  │  (8002)   │  │  (8003)   │
└───────────┘  └───────────┘  └───────────┘
      │               │               │
      └───────────────┼───────────────┘
                      │
┌─────▼─────┐  ┌─────▼─────┐  ┌─────▼─────┐
│  订单服务  │  │  支付服务  │  │  通知服务  │
│  (8004)   │  │  (8005)   │  │  (8006)   │
└───────────┘  └───────────┘  └───────────┘
      │               │               │
      └───────────────┼───────────────┘
                      │
┌─────▼─────┐  ┌─────▼─────┐
│ 验证码服务 │  │  监控服务  │
│  (8007)   │  │  (8009)   │
└───────────┘  └───────────┘
      │               │
      └───────────────┼───────────────┐
                      │               │
┌─────▼─────────────────────────────────▼─────┐
│              基础设施服务                     │
│  Nacos | MySQL | Redis | ELK | Sentinel   │
└─────────────────────────────────────────────┘
```

### 技术栈详情

#### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.0 | 微服务框架 |
| Spring Cloud | 2023.0.0 | 微服务生态 |
| Spring Cloud Alibaba | 2023.0.1.0 | 阿里云微服务组件 |
| Nacos | 2.3.0 | 服务注册与发现、配置中心 |
| Spring Cloud Gateway | 3.1.0 | API网关 |
| Spring Cloud LoadBalancer | 4.0.0 | 客户端负载均衡 |
| Sentinel | 1.8.6 | 熔断降级、限流 |
| MyBatis Plus | 3.5.5 | ORM框架 |
| MySQL | 8.0.33 | 关系型数据库 |
| Redis | 7.0 | 分布式缓存 |
| Spring Boot Admin | 3.2.0 | 监控中心 |
| ELK Stack | 7.17.0 | 日志收集与分析 |
| Docker | 24.0+ | 容器化部署 |

#### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.3.4 | 前端框架 |
| TypeScript | 5.0.2 | 类型支持 |
| Vant | 4.8.1 | 移动端UI组件库 |
| Element Plus | 2.5.6 | PC端UI组件库 |
| Axios | 1.6.0 | HTTP客户端 |
| Pinia | 2.1.7 | 状态管理 |
| Vue Router | 4.2.5 | 路由管理 |
| Vite | 5.0.8 | 构建工具 |

## 微服务详细说明

### 1. 公共模块 (eleme-common)

**职责**: 提供通用配置、工具类和基础设施支持

**主要功能**:
- Sentinel熔断降级配置
- 负载均衡策略配置
- Redis分布式缓存配置
- 分布式锁实现
- 通用异常处理

**关键配置**:
```yaml
# Sentinel配置
sentinel:
  transport:
    dashboard: localhost:8080
  datasource:
    rules:
      file:
        file: sentinel-rules.json
        rule-type: flow
```

### 2. API网关 (eleme-gateway) - 端口8888

**职责**: 统一入口，路由转发，认证授权，限流熔断

**主要功能**:
- 路由配置与转发
- 跨域处理
- 请求日志记录
- 统一异常处理
- 接口文档聚合

**路由配置**:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://eleme-user-service
          predicates:
            - Path=/api/user/**
        - id: business-service
          uri: lb://eleme-business-service
          predicates:
            - Path=/api/business/**
        # ... 其他路由
```

### 3. 用户服务 (eleme-user-service) - 端口8001

**职责**: 用户管理，认证授权

**主要功能**:
- 用户注册与登录
- JWT令牌管理
- 用户信息维护
- 邮箱验证码发送
- 密码加密与验证

**核心API**:
```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterDTO dto);
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO dto);
    
    @PostMapping("/sendVerifyCode")
    public ResponseEntity<?> sendVerifyCode(@RequestBody EmailDTO dto);
}
```

### 4. 商家服务 (eleme-business-service) - 端口8002

**职责**: 商家信息管理

**主要功能**:
- 商家信息查询
- 商家列表获取
- 商家搜索功能
- 商家分类管理
- 营业状态管理

**数据模型**:
```java
@Entity
@Table(name = "business")
public class Business {
    private Integer id;
    private String businessName;
    private String businessAddress;
    private String businessExplain;
    private String businessImg;
    private Integer orderTypeId;
    private Double starPrice;
    private Double deliveryPrice;
    private String remarks;
    // ... 其他字段
}
```

### 5. 食物服务 (eleme-food-service) - 端口8003

**职责**: 食物信息管理

**主要功能**:
- 食物信息查询
- 按商家查询食物
- 食物分类管理
- 食物搜索功能
- 食物库存管理

**关键接口**:
```java
@RestController
@RequestMapping("/api/food")
public class FoodController {
    @GetMapping("/business/{businessId}")
    public ResponseEntity<?> getFoodByBusinessId(@PathVariable Integer businessId);
    
    @PostMapping("/getFoodById")
    public ResponseEntity<?> getFoodById(@RequestBody Map<String, Object> request);
}
```

### 6. 订单服务 (eleme-order-service) - 端口8004

**职责**: 订单管理，订单生命周期

**主要功能**:
- 订单创建与提交
- 订单状态管理
- 订单查询与列表
- 订单支付状态更新
- 订单历史记录

**订单状态流转**:
```
待支付(0) -> 已支付(1) -> 配送中(2) -> 已完成(3)
          -> 已取消(4) -> 已退款(5)
```

### 7. 支付服务 (eleme-payment-service) - 端口8005

**职责**: 支付处理，支付状态管理

**主要功能**:
- 支付订单创建
- 支付状态查询
- 支付成功回调
- 退款处理
- 支付记录管理

**支付流程**:
```java
@Service
public class PaymentService {
    public PaymentResult createPayment(PaymentRequest request);
    public PaymentStatus queryPaymentStatus(String paymentId);
    public void handlePaymentCallback(PaymentCallback callback);
}
```

### 8. 通知服务 (eleme-notification-service) - 端口8006

**职责**: 消息通知，邮件发送

**主要功能**:
- 邮件发送服务
- 短信验证码发送
- 系统通知管理
- 消息模板管理
- 通知历史记录

**通知类型**:
- 邮件通知（简单邮件、HTML邮件、带附件邮件）
- 短信通知（验证码、营销短信）
- 系统通知（站内信、推送通知）

### 9. 验证码服务 (eleme-captcha-service) - 端口8007

**职责**: 验证码生成与验证

**主要功能**:
- 图形验证码生成
- 短信验证码生成
- 邮箱验证码生成
- 滑动验证码
- 点选验证码

**验证码类型**:
```java
public enum CaptchaType {
    IMAGE,      // 图形验证码
    SMS,        // 短信验证码
    EMAIL,      // 邮箱验证码
    SLIDE,      // 滑动验证码
    CLICK       // 点选验证码
}
```

### 10. 监控服务 (eleme-monitor) - 端口8009

**职责**: 系统监控，性能指标收集

**主要功能**:
- 应用健康检查
- 性能指标收集
- 自定义业务指标
- 告警通知
- 监控面板

**监控指标**:
- JVM指标（内存、GC、线程）
- 系统指标（CPU、磁盘、网络）
- 业务指标（订单数量、用户数量、错误率）
- 自定义指标

## 服务治理

### 1. 服务注册与发现

使用Nacos作为注册中心，所有微服务在启动时自动注册到Nacos：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: eleme-dev
        group: DEFAULT_GROUP
```

### 2. 负载均衡

基于Spring Cloud LoadBalancer实现客户端负载均衡：

**支持的负载均衡策略**:
- 轮询（Round Robin）
- 随机（Random）
- 权重（Weighted）
- 健康检查（Health Check）

### 3. 熔断降级

使用Sentinel实现熔断降级：

**熔断规则**:
```json
{
  "resource": "eleme-user-service",
  "grade": 1,
  "count": 10,
  "timeWindow": 10,
  "minRequestAmount": 5,
  "statIntervalMs": 1000,
  "slowRatioThreshold": 0.6
}
```

### 4. 分布式缓存

使用Redis实现分布式缓存：

**缓存策略**:
- 用户信息缓存：30分钟
- 商家信息缓存：1小时
- 食物信息缓存：30分钟
- 验证码缓存：5分钟

**分布式锁**:
```java
@Component
public class DistributedLock {
    public boolean tryLock(String key, long expireTime);
    public void unlock(String key);
}
```

## 监控与日志

### 1. 应用监控

使用Spring Boot Admin进行应用监控：

**监控功能**:
- 应用状态监控
- 健康检查
- 性能指标
- 日志级别动态调整
- 内存和线程监控

**访问地址**: http://localhost:8009
**登录凭证**: admin/admin

### 2. 日志系统

使用ELK Stack进行日志收集和分析：

**组件说明**:
- **Elasticsearch**: 日志存储和搜索
- **Logstash**: 日志收集和处理
- **Kibana**: 日志可视化分析
- **Filebeat**: 日志文件收集

**日志格式**:
```json
{
  "@timestamp": "2024-01-15T10:30:45.123Z",
  "level": "INFO",
  "logger": "org.example.controller.UserController",
  "message": "用户登录成功: user123",
  "service": "eleme-user-service",
  "environment": "dev"
}
```

### 3. 链路追踪

集成Zipkin进行分布式链路追踪：

```yaml
zipkin:
  base-url: http://localhost:9411
sleuth:
  sampler:
    probability: 1.0
```

## 部署与运行

### 1. 环境要求

- **Java**: JDK 17+
- **Maven**: 3.6+
- **Docker**: 20.0+
- **Docker Compose**: 1.29+
- **Node.js**: 16+ (前端)
- **npm**: 8+ (前端)

### 2. 快速启动

#### 方法一：使用启动脚本（推荐）

```bash
# 进入项目目录
cd elemeSpringCloud

# 启动所有服务
./scripts/start-all.sh

# 停止所有服务
./scripts/stop-all.sh
```

#### 方法二：手动启动

```bash
# 1. 启动基础设施
docker-compose -f infrastructure/docker-compose.yml up -d

# 2. 启动ELK Stack
docker-compose -f infrastructure/elk/docker-compose.yml up -d

# 3. 编译项目
mvn clean compile -DskipTests

# 4. 启动各个服务
cd eleme-gateway && mvn spring-boot:run &
cd eleme-user-service && mvn spring-boot:run &
# ... 启动其他服务
```

### 3. 前端启动

```bash
# 进入前端目录
cd elemeVue

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

### 4. 服务访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端应用 | http://localhost:5173 | Vue前端应用 |
| API网关 | http://localhost:8888 | 统一API入口 |
| 用户服务 | http://localhost:8001 | 用户管理 |
| 商家服务 | http://localhost:8002 | 商家管理 |
| 食物服务 | http://localhost:8003 | 食物管理 |
| 订单服务 | http://localhost:8004 | 订单管理 |
| 支付服务 | http://localhost:8005 | 支付处理 |
| 通知服务 | http://localhost:8006 | 消息通知 |
| 验证码服务 | http://localhost:8007 | 验证码服务 |
| 监控服务 | http://localhost:8009 | 监控中心 |
| Nacos控制台 | http://localhost:8848/nacos | 服务注册中心 |
| Sentinel控制台 | http://localhost:8080 | 熔断降级控制台 |
| Kibana | http://localhost:5601 | 日志分析 |

## 项目亮点

### 1. 架构设计

- **微服务拆分合理**: 按业务域进行服务拆分，职责清晰
- **技术选型先进**: 使用最新版本的Spring Cloud生态
- **扩展性良好**: 服务间松耦合，易于扩展和维护

### 2. 服务治理

- **完善的服务治理**: 包含注册发现、负载均衡、熔断降级
- **分布式缓存**: Redis集群支持，提高系统性能
- **分布式锁**: 解决并发问题，保证数据一致性

### 3. 监控体系

- **全方位监控**: 应用监控、日志监控、链路追踪
- **实时告警**: 支持多种告警方式，及时发现问题
- **可视化展示**: 丰富的监控面板和日志分析

### 4. 开发体验

- **统一配置**: 配置集中管理，环境隔离
- **自动化部署**: 一键启动脚本，简化部署流程
- **详细文档**: 完整的配置指南和使用说明

### 5. 安全性

- **JWT认证**: 无状态认证，支持分布式
- **多重验证**: 图形验证码、短信验证码、邮箱验证
- **接口限流**: 防止恶意请求，保护系统稳定

## 性能优化

### 1. 缓存优化

- **多级缓存**: 本地缓存 + Redis分布式缓存
- **缓存预热**: 系统启动时预加载热点数据
- **缓存穿透**: 布隆过滤器防止缓存穿透

### 2. 数据库优化

- **读写分离**: 主从复制，读写分离
- **分库分表**: 按业务域分库，按时间分表
- **索引优化**: 合理创建索引，提高查询效率

### 3. 接口优化

- **异步处理**: 耗时操作异步处理，提高响应速度
- **批量操作**: 减少数据库交互次数
- **分页查询**: 大数据量分页查询，避免内存溢出

## 测试策略

### 1. 单元测试

- **JUnit 5**: 单元测试框架
- **Mockito**: Mock框架
- **TestContainers**: 集成测试容器

### 2. 集成测试

- **Spring Boot Test**: 集成测试支持
- **WireMock**: HTTP服务Mock
- **TestRestTemplate**: REST API测试

### 3. 性能测试

- **JMeter**: 性能测试工具
- **压力测试**: 系统负载测试
- **监控分析**: 性能瓶颈分析

## 部署架构

### 1. 开发环境

- **本地部署**: Docker Compose单机部署
- **快速启动**: 一键启动脚本
- **热部署**: 支持代码热更新

### 2. 测试环境

- **容器化部署**: Docker + Kubernetes
- **持续集成**: Jenkins + GitLab CI
- **自动化测试**: 单元测试 + 集成测试

### 3. 生产环境

- **高可用部署**: 多节点集群部署
- **负载均衡**: Nginx + Spring Cloud Gateway
- **监控告警**: Prometheus + Grafana + AlertManager

## 安全考虑

### 1. 认证授权

- **JWT令牌**: 无状态认证
- **权限控制**: RBAC基于角色的访问控制
- **接口鉴权**: 统一鉴权中心

### 2. 数据安全

- **敏感信息加密**: 密码加密存储
- **传输加密**: HTTPS传输
- **数据脱敏**: 日志脱敏处理

### 3. 系统安全

- **防SQL注入**: 参数化查询
- **XSS防护**: 输入验证和输出编码
- **CSRF防护**: Token验证

## 后续扩展计划

### 1. 功能扩展

- **商家入驻**: 商家自主入驻和管理
- **优惠券系统**: 优惠券发放和使用
- **积分系统**: 用户积分和等级管理
- **评价系统**: 用户评价和商家评分

### 2. 技术升级

- **服务网格**: 集成Istio服务网格
- **消息队列**: 集成RabbitMQ或Kafka
- **分布式事务**: 集成Seata分布式事务
- **搜索引擎**: 集成Elasticsearch搜索

### 3. 运维优化

- **自动化运维**: 集成Ansible或Terraform
- **容器编排**: 迁移到Kubernetes
- **CI/CD**: 完善持续集成和部署流程
- **监控告警**: 集成Prometheus + Grafana

## 常见问题

### 1. 服务启动失败

**问题**: 服务无法启动或注册失败
**解决方案**:
1. 检查Nacos是否正常运行
2. 确认端口是否被占用
3. 查看服务日志排查错误

### 2. 前端API调用失败

**问题**: 前端无法调用后端API
**解决方案**:
1. 确认网关服务正常运行
2. 检查API地址配置
3. 确认服务注册状态

### 3. 数据库连接失败

**问题**: 服务无法连接数据库
**解决方案**:
1. 检查MySQL容器状态
2. 确认数据库配置信息
3. 验证网络连接

### 4. 缓存不生效

**问题**: Redis缓存不生效
**解决方案**:
1. 检查Redis服务状态
2. 确认缓存配置
3. 验证缓存注解使用

## 贡献指南

### 1. 代码规范

- **Java编码规范**: 遵循阿里巴巴Java开发手册
- **前端编码规范**: 遵循Vue官方风格指南
- **Git提交规范**: 使用conventional commits

### 2. 开发流程

1. Fork项目到个人仓库
2. 创建功能分支
3. 编写代码和测试
4. 提交Pull Request
5. 代码审查和合并

### 3. 文档维护

- **API文档**: 及时更新Swagger文档
- **架构文档**: 重要变更需要更新架构图
- **部署文档**: 部署步骤变更需要更新文档

## 总结

本项目成功实现了从单体SpringBoot应用到分布式微服务架构的迁移，建立了完整的微服务生态系统。项目具有以下特点：

1. **架构完整**: 包含服务注册发现、API网关、服务治理等完整的微服务组件
2. **技术先进**: 使用最新版本的Spring Cloud技术栈
3. **功能丰富**: 实现了用户管理、商家管理、订单处理等完整的业务功能
4. **监控完善**: 集成监控、日志、链路追踪等完整的可观测性体系
5. **部署简便**: 提供一键启动脚本，支持快速部署

项目为后续的扩展和维护打下了坚实的基础，可以支持更大规模的业务发展和技术演进。

## 联系方式

如有问题或建议，请通过以下方式联系：

- **项目地址**: https://github.com/your-org/eleme-microservice
- **文档地址**: https://your-org.github.io/eleme-microservice-docs
- **邮箱**: dev-team@eleme.com
- **QQ群**: 123456789

---

*最后更新时间: 2024年1月15日*
*版本: v1.0.0* 