#!/bin/bash

# 饿了么微服务启动脚本
# 使用说明：./start-all.sh

echo "=========================================="
echo "       饿了么微服务启动脚本"
echo "=========================================="

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "错误：Docker未运行，请启动Docker服务"
    exit 1
fi

# 检查Maven是否可用
if ! mvn --version > /dev/null 2>&1; then
    echo "错误：Maven未安装或未配置环境变量"
    exit 1
fi

# 切换到项目根目录
cd "$(dirname "$0")/.."

echo "1. 启动基础设施服务..."
echo "   - 启动Nacos..."
docker-compose -f infrastructure/docker-compose.yml up -d nacos

echo "   - 启动MySQL..."
docker-compose -f infrastructure/docker-compose.yml up -d mysql-master

echo "   - 启动Redis..."
docker-compose -f infrastructure/docker-compose.yml up -d redis

echo "   - 启动Sentinel Dashboard..."
docker-compose -f infrastructure/docker-compose.yml up -d sentinel-dashboard

echo "   - 启动ELK Stack..."
docker-compose -f infrastructure/elk/docker-compose.yml up -d elasticsearch
sleep 10
docker-compose -f infrastructure/elk/docker-compose.yml up -d logstash
sleep 5
docker-compose -f infrastructure/elk/docker-compose.yml up -d kibana

echo "   等待基础设施服务启动完成..."
sleep 30

echo "2. 编译项目..."
mvn clean compile -DskipTests

echo "3. 启动微服务..."

# 启动顺序：公共模块 -> 网关 -> 各个服务 -> 监控
services=(
    "eleme-gateway:8888"
    "eleme-user-service:8001"
    "eleme-business-service:8002"
    "eleme-food-service:8003"
    "eleme-order-service:8004"
    "eleme-payment-service:8005"
    "eleme-notification-service:8006"
    "eleme-captcha-service:8007"
    "eleme-monitor:8009"
)

for service_port in "${services[@]}"; do
    service="${service_port%:*}"
    port="${service_port#*:}"
    
    echo "   启动 $service (端口: $port)..."
    cd $service
    
    # 后台启动服务
    nohup mvn spring-boot:run > ../logs/${service}.log 2>&1 &
    
    # 记录进程ID
    echo $! > ../logs/${service}.pid
    
    cd ..
    
    # 等待服务启动
    echo "   等待 $service 启动完成..."
    sleep 15
    
    # 检查服务健康状态
    if curl -f http://localhost:$port/actuator/health > /dev/null 2>&1; then
        echo "   ✓ $service 启动成功"
    else
        echo "   ✗ $service 启动失败或未完全启动"
    fi
done

echo "4. 服务启动完成！"
echo ""
echo "=========================================="
echo "           服务访问地址"
echo "=========================================="
echo "网关服务:           http://localhost:8888"
echo "用户服务:           http://localhost:8001"
echo "商家服务:           http://localhost:8002"
echo "食物服务:           http://localhost:8003"
echo "订单服务:           http://localhost:8004"
echo "支付服务:           http://localhost:8005"
echo "通知服务:           http://localhost:8006"
echo "验证码服务:         http://localhost:8007"
echo "监控服务:           http://localhost:8009"
echo ""
echo "=========================================="
echo "           基础设施服务"
echo "=========================================="
echo "Nacos控制台:        http://localhost:8848/"
echo "MySQL数据库:        localhost:3306"
echo "Redis缓存:          localhost:6379"
echo "Sentinel控制台:     http://localhost:8080"
echo "Elasticsearch:      http://localhost:9200"
echo "Kibana:             http://localhost:5601"
echo ""
echo "=========================================="
echo "           前端应用"
echo "=========================================="
echo "请手动启动前端应用："
echo "cd ../elemeVue"
echo "npm install"
echo "npm run dev"
echo ""
echo "默认用户名/密码："
echo "  - Nacos:     nacos/nacos"
echo "  - 监控中心:   admin/admin"
echo "=========================================="

echo "所有服务已启动！请检查各服务状态。"
echo "查看日志: tail -f logs/服务名.log"
echo "停止服务: ./stop-all.sh" 