# 饿了么外卖平台 - 基于Spring Cloud微服务架构

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.0-blue.svg)](https://spring.io/projects/spring-cloud)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.x-brightgreen.svg)](https://vuejs.org/)
[![Nacos](https://img.shields.io/badge/Nacos-2.x-orange.svg)](https://nacos.io/)
[![Sentinel](https://img.shields.io/badge/Sentinel-1.8.x-red.svg)](https://github.com/alibaba/Sentinel)
[![Docker](https://img.shields.io/badge/Docker-Container-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Author](https://img.shields.io/badge/Author-ORG233-lightgrey.svg)](https://github.com/jumx0202)

## 1. 项目简介

本项目是一个仿“饿了么”外卖平台的**全栈**项目，旨在完整地实践和展示一套基于`Spring Cloud`生态的现代化微服务架构。项目采用前后端分离的开发模式，后端基于`Spring Boot` + `Spring Cloud Alibaba`构建，前端则由`Vue3`全家桶驱动。

该项目作为**云南大学软件学院《软件服务工程》课程**的期末大作业，涵盖了从系统设计、技术选型、开发实现到容器化部署的全过程，实现了一个高可用、高内聚、低耦合的分布式在线点餐系统。

## 2. 核心功能

- **用户端核心功能**:
    - **用户模块**: 支持邮箱验证码注册、账号密码登录（含图形验证码防刷）、个人信息管理等。
    - **商家模块**: 实现商家列表展示、按分类筛选、关键词搜索、查看商家详情等功能。
    - **商品模块**: 支持商品浏览、加购、数量增减等操作。
    - **订单模块**: 完整的下单、支付、订单状态跟踪、历史订单查询等闭环流程。
    - **支付模块**: 模拟多种支付方式，并集成支付倒计时功能。
- **商家端核心功能**:
    - **仪表盘**: 通过 Echarts 展示商品销量排行等核心数据。
    - **商品管理**: 对店铺内商品进行增、删、改、查以及上/下架操作。
    - **店铺管理**: 修改店铺公告、营业状态等信息。
- **系统级特性**:
    - **服务治理**: 基于 Nacos 的服务动态注册与发现、统一配置管理。
    - **高可用保障**: 基于 Sentinel 的流量控制、熔断降级和系统负载保护。
    - **统一入口**: 基于 Spring Cloud Gateway 的 API 网关，聚合所有服务，并实现统一认证、跨域和日志记录。
    - **集中式日志**: 集成 ELK + Filebeat，实现所有微服务日志的统一收集、分析与可视化。

## 3. 系统架构

### 3.1 整体架构图

系统采用分层架构，清晰地划分了前端、网关、业务服务和基础设施层。
![图片 1.png](图片%201.png)

### 3.2 微服务拆分

项目遵循单一职责和领域驱动设计原则，将后端业务拆分为9个核心微服务。

| 服务名称 | 端口 | 核心职责 |
| --- | --- | --- |
| `eleme-gateway` | `8888` | API 网关、路由转发、认证鉴权 |
| `eleme-user-service` | `8001` | 用户管理、认证授权 |
| `eleme-business-service` | `8002` | 商家信息管理 |
| `eleme-food-service` | `8003` | 商品信息管理 |
| `eleme-order-service` | `8004` | 订单生命周期管理 |
| `eleme-payment-service` | `8005` | 支付处理 |
| `eleme-notification-service` | `8006` | 消息通知 (邮件) |
| `eleme-captcha-service` | `8007` | 验证码管理 |
| `eleme-monitor` | `8009` | 集成 Spring Boot Admin 进行服务监控 |

### 3.3 服务间通信关系

服务间通过 Spring Cloud Gateway 统一路由，业务服务之间使用 OpenFeign 进行同步调用。
![图片 2.png](%E5%9B%BE%E7%89%87%202.png)

## 4. 技术栈

<details>
<summary><b>后端技术栈 (Backend)</b></summary>

| 技术 | 版本/说明 |
| --- | --- |
| **核心框架** | |
| Spring Boot | `3.2.0` - 提供快速开发和自动配置能力 |
| Spring Cloud | `2023.0.0` - 微服务生态系统 |
| Spring Cloud Alibaba | `2023.0.1.0` - 集成 Nacos、Sentinel 等 |
| **微服务组件** | |
| Nacos | 服务注册发现、统一配置中心 |
| Sentinel | 流量控制、熔断降级 |
| Spring Cloud Gateway | 响应式 API 网关 |
| OpenFeign | 声明式 HTTP 客户端，用于服务间调用 |
| **数据与存储** | |
| MySQL | `8.0` - 关系型数据库，用于业务数据持久化 |
| Redis | `7.0` - 分布式缓存，提升性能 |
| MyBatis Plus | `3.5.5` - ORM 框架，简化数据库操作 |
| **安全与监控** | |
| Spring Security | JWT Token 统一认证机制 |
| Spring Boot Admin | 微服务监控面板 |
| Micrometer + Sleuth | 应用指标采集与分布式链路追踪 |

</details>

<details>
<summary><b>前端技术栈 (Frontend)</b></summary>

| 技术 | 版本/说明 |
| --- | --- |
| **核心框架** | |
| Vue.js | `3.x` - 渐进式前端框架 |
| TypeScript | `5.x` - 提供静态类型检查，提升代码质量 |
| Vite | `6.x` - 新一代前端构建工具，提供极速开发体验 |
| **状态管理** | |
| Pinia | 新一代 Vue 状态管理库 |
| **路由** | |
| Vue Router | `4.x` - 官方路由管理器 |
| **UI 组件库** | |
| Element Plus | PC 端 UI 组件库 (用于商家管理后台) |
| Vant | 移动端 UI 组件库 (用于用户端 App) |
| **HTTP 通信** | |
| Axios | 用于与后端 API 通信 |

</details>

<details>
<summary><b>基础设施与 DevOps</b></summary>

| 技术 | 说明 |
| --- | --- |
| Docker / Docker Compose | 容器化部署与编排所有服务和中间件 |
| ELK Stack + Filebeat | 集中式日志收集、存储、分析与可视化 |
| Maven | 后端项目构建与依赖管理 |

</details>

## 5. 项目启动与运行

请确保您的本地环境已安装以下软件：

- JDK 17+
- Maven 3.8+
- Node.js 18+
- Docker 和 Docker Compose

### 步骤 1: 克隆项目

```bash
git clone https://github.com/jumx0202/elemeSpringCloud.git
cd elemeSpringCloud
```

### 步骤 2: 启动基础设施

本项目所有中间件（MySQL, Redis, Nacos, Sentinel, ELK）均通过 Docker Compose 管理。在项目根目录下执行：

```bash
docker-compose up -d
```

该命令会一次性启动所有依赖服务。首次启动会下载镜像，请耐心等待。
- **数据库初始化**: MySQL 容器启动时会自动执行 `./mysql/init` 目录下的 SQL 脚本，创建 `eleme_db` 和 `nacos_config` 数据库及所需用户。

### 步骤 3: 配置 Nacos

1.  访问 Nacos 控制台：`http://localhost:8848/nacos`
2.  使用默认账户登录：`nacos` / `nacos`
3.  进入 **配置管理 -> 配置列表**，手动为每个微服务创建配置文件。请参考各微服务模块 `resources` 目录下的 `bootstrap.yml` 文件和 Nacos 相关配置。
    -   **Data ID**: 格式通常为 `[service-name].yml` (例如 `eleme-user-service.yml`)
    -   **Group**: `DEFAULT_GROUP`
    -   **配置内容**: 将对应服务的 `application.yml` 内容复制进去

### 步骤 4: 启动后端微服务

使用 IntelliJ IDEA 或其他 IDE 打开项目根目录。IDE 会自动识别为 Maven 项目。请逐一运行以下9个微服务的 `main` 方法启动类：
- `GatewayApplication`
- `UserServiceApplication`
- `BusinessServiceApplication`
- `FoodServiceApplication`
- `OrderServiceApplication`
- `PaymentServiceApplication`
- `NotificationServiceApplication`
- `CaptchaServiceApplication`
- `MonitorApplication`

启动后，它们会自动注册到 Nacos。

### 步骤 5: 启动前端应用

```bash
# 进入前端项目目录 (请根据您的项目结构调整)
cd frontend/eleme-vue

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

启动成功后，即可访问前端应用。

### 步骤 6: 访问系统

- **前端用户端**: `http://localhost:5173`
- **Nacos 控制台**: `http://localhost:8848/nacos`
- **Sentinel 控制台**: `http://localhost:8080`
- **Kibana 日志面板**: `http://localhost:5601`

## 6. 界面截图

<details>
<summary><b>点击查看部分界面截图</b></summary>

| 登录/注册 | 首页 | 点餐页 |
| --- | --- | --- |
| ![图片 3.jpg](%E5%9B%BE%E7%89%87%203.jpg) | ![图片 4.png](%E5%9B%BE%E7%89%87%204.png) | ![图片 5.png](%E5%9B%BE%E7%89%87%205.png)|
| **订单确认** | **订单列表** | **商家后台** |
| ![图片 6.png](%E5%9B%BE%E7%89%87%206.png)| ![图片 7.png](%E5%9B%BE%E7%89%87%207.png) |
| **基础设施监控** | | |
| ![图片 8.png](%E5%9B%BE%E7%89%87%208.png) | ![图片 9.png](%E5%9B%BE%E7%89%87%209.png) | 

</details>

## 7. 作者

- **橘子ORG🍊**
- 云南大学 软件学院

## 8. 许可证

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
