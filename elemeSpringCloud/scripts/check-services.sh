#!/bin/bash

# 饿了么微服务状态检查脚本
# 使用说明：./check-services.sh

echo "=========================================="
echo "       饿了么微服务状态检查"
echo "=========================================="

# 切换到项目根目录
cd "$(dirname "$0")/.."

echo "1. 基础设施服务状态："
echo "----------------------------------------"

# 检查Nacos
if curl -f http://localhost:8848/ > /dev/null 2>&1; then
    echo "Nacos (8848):              ✓ 正常运行"
else
    echo "Nacos (8848):              ✗ 未运行"
fi

# 检查MySQL
if docker ps | grep mysql-master > /dev/null 2>&1; then
    echo "MySQL (3306):              ✓ 正常运行"
else
    echo "MySQL (3306):              ✗ 未运行"
fi

# 检查Redis
if docker ps | grep redis-server > /dev/null 2>&1; then
    echo "Redis (6379):              ✓ 正常运行"
else
    echo "Redis (6379):              ✗ 未运行"
fi

# 检查Sentinel
if curl -f http://localhost:8080 > /dev/null 2>&1; then
    echo "Sentinel (8080):           ✓ 正常运行"
else
    echo "Sentinel (8080):           ✗ 未运行"
fi

# 检查Elasticsearch
if curl -f http://localhost:9200 > /dev/null 2>&1; then
    echo "Elasticsearch (9200):      ✓ 正常运行"
else
    echo "Elasticsearch (9200):      ✗ 未运行"
fi

# 检查Kibana
if curl -f http://localhost:5601 > /dev/null 2>&1; then
    echo "Kibana (5601):             ✓ 正常运行"
else
    echo "Kibana (5601):             ✗ 未运行"
fi

echo ""
echo "2. 微服务状态："
echo "----------------------------------------"

# 检查微服务
services=("eleme-gateway:8888" "eleme-user-service:8001" "eleme-business-service:8002" 
          "eleme-food-service:8003" "eleme-order-service:8004" "eleme-payment-service:8005" 
          "eleme-notification-service:8006" "eleme-captcha-service:8007" "eleme-monitor:8009")

for service_port in "${services[@]}"; do
    service="${service_port%:*}"
    port="${service_port#*:}"
    
    # 格式化服务名显示
    service_display=$(echo $service | sed 's/eleme-//' | sed 's/-service//')
    
    # 检查健康状态
    if curl -f http://localhost:$port/actuator/health > /dev/null 2>&1; then
        echo "$(printf "%-20s" "$service_display") ($port): ✓ 正常运行"
    else
        # 检查端口是否占用
        if lsof -i :$port > /dev/null 2>&1; then
            echo "$(printf "%-20s" "$service_display") ($port): ⚠ 启动中..."
        else
            echo "$(printf "%-20s" "$service_display") ($port): ✗ 未运行"
        fi
    fi
done

echo ""
echo "3. 快速访问链接："
echo "----------------------------------------"
echo "网关服务:     http://localhost:8888"
echo "Swagger文档:  http://localhost:8888/swagger-ui.html"
echo "Nacos控制台:  http://localhost:8848/nacos"
echo "Sentinel控制台: http://localhost:8080"
echo "Kibana日志:   http://localhost:5601"

echo ""
echo "4. 日志查看："
echo "----------------------------------------"
if [ -d "logs" ]; then
    echo "日志文件目录: logs/"
    for log_file in logs/*.log; do
        if [ -f "$log_file" ]; then
            service_name=$(basename "$log_file" .log)
            echo "  $service_name: tail -f logs/$service_name.log"
        fi
    done
fi

echo ""
echo "=========================================="
echo "管理命令:"
echo "  启动所有服务: ./scripts/start-services.sh"
echo "  停止所有服务: ./scripts/stop-services.sh"
echo "  重启所有服务: ./scripts/restart-services.sh"
echo "==========================================" 