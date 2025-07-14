#!/bin/bash

# 饿了么微服务停止脚本
# 使用说明：./stop-all.sh

echo "=========================================="
echo "       饿了么微服务停止脚本"
echo "=========================================="

# 切换到项目根目录
cd "$(dirname "$0")/.."

echo "1. 停止微服务..."

# 所有服务列表
services=(
    "eleme-gateway"
    "eleme-user-service"
    "eleme-business-service"
    "eleme-food-service"
    "eleme-order-service"
    "eleme-payment-service"
    "eleme-notification-service"
    "eleme-captcha-service"
    "eleme-monitor"
)

# 创建logs目录（如果不存在）
mkdir -p logs

for service in "${services[@]}"; do
    echo "   停止 $service..."
    
    # 检查PID文件是否存在
    if [ -f "logs/${service}.pid" ]; then
        pid=$(cat "logs/${service}.pid")
        
        # 检查进程是否存在
        if ps -p $pid > /dev/null 2>&1; then
            echo "   正在停止进程 $pid..."
            kill $pid
            
            # 等待进程停止
            sleep 5
            
            # 如果进程仍在运行，强制杀死
            if ps -p $pid > /dev/null 2>&1; then
                echo "   强制停止进程 $pid..."
                kill -9 $pid
            fi
            
            echo "   ✓ $service 已停止"
        else
            echo "   ✗ $service 进程不存在"
        fi
        
        # 删除PID文件
        rm -f "logs/${service}.pid"
    else
        echo "   ✗ $service PID文件不存在"
    fi
done

echo "2. 停止基础设施服务..."

echo "   - 停止ELK Stack..."
docker-compose -f infrastructure/elk/docker-compose.yml down

echo "   - 停止基础设施服务..."
docker-compose -f infrastructure/docker-compose.yml down

echo "3. 清理临时文件..."
echo "   - 清理日志文件..."
find logs -name "*.log" -type f -delete 2>/dev/null || true

echo "   - 清理PID文件..."
find logs -name "*.pid" -type f -delete 2>/dev/null || true

echo "4. 显示剩余Java进程..."
echo "   当前Java进程："
ps aux | grep java | grep -v grep | awk '{print $2, $11}' || echo "   没有Java进程运行"

echo ""
echo "=========================================="
echo "           停止完成"
echo "=========================================="
echo "所有服务已停止！"
echo ""
echo "如果有进程仍在运行，可以使用以下命令手动停止："
echo "ps aux | grep java | grep -v grep"
echo "kill -9 <进程ID>"
echo ""
echo "重新启动服务: ./start-all.sh"
echo "==========================================" 