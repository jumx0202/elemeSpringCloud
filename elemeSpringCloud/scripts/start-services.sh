#!/bin/bash

# 饿了么微服务启动脚本（改进版）
# 使用说明：./start-services.sh

echo "=========================================="
echo "       饿了么微服务启动脚本 (改进版)"
echo "=========================================="

# 切换到项目根目录
cd "$(dirname "$0")/.."

# 创建logs目录
mkdir -p logs

# 检查并停止已有的服务
echo "1. 停止已有的服务..."
pkill -f "spring-boot:run" || true

# 等待服务完全停止
sleep 3

# 清理旧的pid文件
rm -f logs/*.pid

echo "2. 检查基础设施服务状态..."

# 检查Nacos
if curl -f http://localhost:8848/ > /dev/null 2>&1; then
    echo "   ✓ Nacos 正常运行"
else
    echo "   ✗ Nacos 未运行，请先启动基础设施服务"
    exit 1
fi

# 检查MySQL
if nc -z localhost 3306 > /dev/null 2>&1; then
    echo "   ✓ MySQL 正常运行"
else
    echo "   ✗ MySQL 未运行，请先启动基础设施服务"
    exit 1
fi

# 检查Redis
if nc -z localhost 6379 > /dev/null 2>&1; then
    echo "   ✓ Redis 正常运行"
else
    echo "   ✗ Redis 未运行，请先启动基础设施服务"
    exit 1
fi

echo "3. 启动微服务..."

# 启动顺序：网关 -> 各个服务 -> 监控
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

# 启动函数
start_service() {
    local service_port=$1
    local service="${service_port%:*}"
    local port="${service_port#*:}"
    
    echo "   启动 $service (端口: $port)..."
    
    # 检查端口是否被占用
    if lsof -i :$port > /dev/null 2>&1; then
        echo "   ! 端口 $port 已被占用，跳过 $service"
        return 0
    fi
    
    cd $service
    
    # 后台启动服务
    nohup mvn spring-boot:run > ../logs/${service}.log 2>&1 &
    
    # 记录进程ID
    echo $! > ../logs/${service}.pid
    
    cd ..
    
    # 等待服务启动
    echo "   等待 $service 启动完成..."
    local attempts=0
    local max_attempts=30
    
    while [ $attempts -lt $max_attempts ]; do
        if curl -f http://localhost:$port/actuator/health > /dev/null 2>&1; then
            echo "   ✓ $service 启动成功"
            return 0
        fi
        sleep 2
        attempts=$((attempts + 1))
    done
    
    echo "   ✗ $service 启动失败或超时"
    echo "   查看日志: tail -f logs/${service}.log"
    return 1
}

# 启动所有服务
for service_port in "${services[@]}"; do
    start_service $service_port
done

echo "4. 服务启动完成！"
echo ""
echo "=========================================="
echo "           服务访问地址"
echo "=========================================="
echo "网关服务 (统一入口):    http://localhost:8888"
echo "Swagger文档 (网关):     http://localhost:8888/swagger-ui.html"
echo "用户服务:              http://localhost:8001"
echo "商家服务:              http://localhost:8002"
echo "食物服务:              http://localhost:8003"
echo "订单服务:              http://localhost:8004"
echo "支付服务:              http://localhost:8005"
echo "通知服务:              http://localhost:8006"
echo "验证码服务:            http://localhost:8007"
echo "监控服务:              http://localhost:8009"
echo ""
echo "=========================================="
echo "           基础设施服务"
echo "=========================================="
echo "Nacos控制台:           http://localhost:8848/nacos"
echo "Sentinel控制台:        http://localhost:8080"
echo "Kibana日志:            http://localhost:5601"
echo ""
echo "=========================================="
echo "           管理命令"
echo "=========================================="
echo "查看服务状态: ./scripts/check-services.sh"
echo "查看日志: tail -f logs/服务名.log"
echo "停止服务: ./scripts/stop-services.sh"
echo "重启服务: ./scripts/restart-services.sh"
echo "==========================================" 