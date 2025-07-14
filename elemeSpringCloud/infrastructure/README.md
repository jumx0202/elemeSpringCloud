# 饿了么SpringCloud基础设施部署指南

本文档介绍如何部署饿了么SpringCloud系统的基础设施，包括Nacos注册中心、MySQL高可用集群、Redis高可用集群和Sentinel熔断降级组件。

## 架构概述

### 基础组件
- **Nacos**: 注册中心和配置中心
- **MySQL**: 主从复制高可用集群（1主2从）
- **Redis**: 主从复制 + Sentinel高可用集群
- **Sentinel**: 熔断降级和流量控制

### 服务端口分配
- Nacos: 8848, 9848
- MySQL Master: 3306
- MySQL Slave1: 3307
- MySQL Slave2: 3308
- Redis Master: 6379
- Redis Slave1: 6380
- Redis Slave2: 6381
- Redis Sentinel1: 26379
- Redis Sentinel2: 26380
- Redis Sentinel3: 26381
- Sentinel Dashboard: 8080

## 部署步骤

### 1. 准备工作

确保已安装以下软件：
- Docker
- Docker Compose

### 2. 创建目录结构

```bash
mkdir -p elemeSpringCloud/infrastructure
cd elemeSpringCloud/infrastructure

# 创建各组件的数据目录
mkdir -p nacos/{logs,data}
mkdir -p mysql/{master,slave1,slave2}/{data,conf,logs}
mkdir -p mysql/init
mkdir -p redis/{master,slave1,slave2}/{data,conf}
mkdir -p redis/{sentinel1,sentinel2,sentinel3}/conf
```

### 3. 启动基础设施

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f [service_name]
```

### 4. 配置MySQL主从复制

#### 4.1 配置主节点

```bash
# 进入MySQL主节点
docker exec -it mysql-master mysql -uroot -proot123

# 创建复制用户
CREATE USER 'replication'@'%' IDENTIFIED BY 'replication123';
GRANT REPLICATION SLAVE ON *.* TO 'replication'@'%';
FLUSH PRIVILEGES;

# 查看主节点状态
SHOW MASTER STATUS;
```

#### 4.2 配置从节点

```bash
# 进入MySQL从节点1
docker exec -it mysql-slave1 mysql -uroot -proot123

# 配置主从复制
CHANGE MASTER TO
  MASTER_HOST='mysql-master',
  MASTER_USER='replication',
  MASTER_PASSWORD='replication123',
  MASTER_AUTO_POSITION=1;

# 启动复制
START SLAVE;

# 查看从节点状态
SHOW SLAVE STATUS\G;
```

对slave2重复相同操作。

### 5. 验证Redis高可用

```bash
# 连接Redis主节点
docker exec -it redis-master redis-cli -a redis123

# 设置测试数据
SET test_key "test_value"

# 连接Redis从节点验证数据同步
docker exec -it redis-slave1 redis-cli -a redis123
GET test_key

# 查看Sentinel状态
docker exec -it redis-sentinel1 redis-cli -p 26379
SENTINEL masters
SENTINEL slaves mymaster
```

### 6. 访问管理界面

- **Nacos控制台**: http://localhost:8848/nacos
  - 用户名: nacos
  - 密码: nacos
  
- **Sentinel Dashboard**: http://localhost:8080
  - 用户名: sentinel
  - 密码: sentinel

## 配置说明

### Nacos配置

Nacos使用MySQL作为数据存储，配置信息如下：
- 数据库: nacos_config
- 用户名: nacos
- 密码: nacos123

### MySQL配置

- **主节点**: server-id=1, 开启binlog
- **从节点**: server-id=2/3, 开启relay-log, 只读模式
- **数据库**: eleme_db
- **用户**: eleme/eleme123

### Redis配置

- **主节点**: 端口6379, 密码redis123
- **从节点**: 端口6380/6381, 从master复制数据
- **Sentinel**: 监控主节点, 自动故障转移

## 数据备份

### MySQL备份

```bash
# 备份所有数据库
docker exec mysql-master mysqldump -uroot -proot123 --all-databases > backup_$(date +%Y%m%d).sql

# 备份指定数据库
docker exec mysql-master mysqldump -uroot -proot123 eleme_db > eleme_db_backup_$(date +%Y%m%d).sql
```

### Redis备份

```bash
# 备份Redis数据
docker exec redis-master redis-cli -a redis123 BGSAVE

# 复制RDB文件
docker cp redis-master:/data/dump.rdb ./redis_backup_$(date +%Y%m%d).rdb
```

## 监控和日志

### 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看指定服务日志
docker-compose logs -f nacos
docker-compose logs -f mysql-master
docker-compose logs -f redis-master
```

### 监控指标

- **MySQL**: 主从延迟、连接数、慢查询
- **Redis**: 内存使用率、命中率、连接数
- **Nacos**: 服务注册数量、配置变更频率

## 故障处理

### MySQL故障

```bash
# 检查主从复制状态
docker exec mysql-slave1 mysql -uroot -proot123 -e "SHOW SLAVE STATUS\G"

# 重启复制
docker exec mysql-slave1 mysql -uroot -proot123 -e "STOP SLAVE; START SLAVE;"
```

### Redis故障

```bash
# 查看Sentinel状态
docker exec redis-sentinel1 redis-cli -p 26379 SENTINEL masters

# 手动故障转移
docker exec redis-sentinel1 redis-cli -p 26379 SENTINEL failover mymaster
```

## 扩容说明

### 添加MySQL从节点

1. 修改docker-compose.yml，添加新的从节点配置
2. 启动新节点
3. 配置主从复制

### 添加Redis从节点

1. 修改docker-compose.yml，添加新的从节点配置
2. 启动新节点，自动从主节点同步数据

## 安全配置

### 密码策略

所有组件都配置了密码验证：
- MySQL root密码: root123
- MySQL业务用户密码: eleme123
- Redis密码: redis123
- Nacos密码: nacos123

### 网络隔离

使用Docker自定义网络eleme-network，确保服务间通信安全。

### 防火墙配置

生产环境建议配置防火墙规则：
```bash
# 仅允许内网访问数据库端口
sudo ufw allow from 10.0.0.0/8 to any port 3306
sudo ufw allow from 10.0.0.0/8 to any port 6379

# 允许外网访问管理端口
sudo ufw allow 8848
sudo ufw allow 8080
```

## 性能优化

### MySQL优化

```ini
# mysql/master/conf/my.cnf
[mysqld]
innodb_buffer_pool_size = 1G
innodb_log_file_size = 256M
max_connections = 1000
slow_query_log = 1
long_query_time = 2
```

### Redis优化

```conf
# redis/master/conf/redis.conf
maxmemory 512mb
maxmemory-policy allkeys-lru
save 900 1
save 300 10
save 60 10000
```

## 维护建议

1. **定期备份**: 每天备份MySQL和Redis数据
2. **日志轮转**: 配置日志轮转，防止磁盘空间不足
3. **监控告警**: 设置关键指标的监控告警
4. **定期更新**: 定期更新组件版本，修复安全漏洞
5. **压力测试**: 定期进行压力测试，验证系统稳定性

## 联系方式

如有问题，请联系运维团队或查看相关文档。 