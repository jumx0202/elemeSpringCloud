#!/bin/bash

# 饿了么微服务停止脚本
# 使用说明：./stop-services.sh

echo "=========================================="
echo "       饿了么微服务停止脚本"
echo "=========================================="

# 切换到项目根目录
cd "$(dirname "$0")/.."

echo "1. 停止所有微服务..."

# 停止所有Spring Boot服务
pkill -f "spring-boot:run"

# 等待服务完全停止
sleep 3

# 通过PID文件停止服务
if [ -d "logs" ]; then
    for pid_file in logs/*.pid; do
        if [ -f "$pid_file" ]; then
            service_name=$(basename "$pid_file" .pid)
            pid=$(cat "$pid_file")
            
            if kill -0 $pid > /dev/null 2>&1; then
                echo "   停止 $service_name (PID: $pid)..."
                kill $pid
            fi
            
            # 删除PID文件
            rm -f "$pid_file"
        fi
    done
fi

echo "2. 检查服务状态..."

# 检查端口是否还在使用
ports=(8888 8001 8002 8003 8004 8005 8006 8007 8009)
for port in "${ports[@]}"; do
    if lsof -i :$port > /dev/null 2>&1; then
        echo "   ! 端口 $port 仍在使用，强制停止..."
        lsof -ti :$port | xargs kill -9
    fi
done

echo "3. 清理临时文件..."
rm -f logs/*.pid

echo "4. 所有微服务已停止！"
echo ""
echo "=========================================="
echo "           服务状态"
echo "=========================================="

# 显示服务状态
services=("eleme-gateway:8888" "eleme-user-service:8001" "eleme-business-service:8002" 
          "eleme-food-service:8003" "eleme-order-service:8004" "eleme-payment-service:8005" 
          "eleme-notification-service:8006" "eleme-captcha-service:8007" "eleme-monitor:8009")

for service_port in "${services[@]}"; do
    service="${service_port%:*}"
    port="${service_port#*:}"
    
    if lsof -i :$port > /dev/null 2>&1; then
        echo "$service (端口: $port): ✗ 仍在运行"
    else
        echo "$service (端口: $port): ✓ 已停止"
    fi
done

echo "=========================================="
echo "启动服务命令: ./scripts/start-services.sh"
echo "==========================================" 