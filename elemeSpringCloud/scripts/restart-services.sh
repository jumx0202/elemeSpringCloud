#!/bin/bash

# 饿了么微服务重启脚本
# 使用说明：./restart-services.sh

echo "=========================================="
echo "       饿了么微服务重启脚本"
echo "=========================================="

# 切换到项目根目录
cd "$(dirname "$0")/.."

echo "1. 停止所有微服务..."
./scripts/stop-services.sh

echo ""
echo "2. 等待服务完全停止..."
sleep 5

echo ""
echo "3. 重新启动所有微服务..."
./scripts/start-services.sh

echo ""
echo "=========================================="
echo "       重启完成"
echo "=========================================="
echo "检查服务状态: ./scripts/check-services.sh"
echo "==========================================" 