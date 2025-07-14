package org.example.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 监控指标配置
 */
@Configuration
public class MetricsConfig {

    /**
     * 自定义业务指标
     */
    @Bean
    public MeterBinder customMetrics(MeterRegistry meterRegistry) {
        return (registry) -> {
            // 服务在线数量
            registry.gauge("eleme.services.online", 0);
            
            // 总订单数
            registry.gauge("eleme.orders.total", 0);
            
            // 今日订单数
            registry.gauge("eleme.orders.today", 0);
            
            // 在线用户数
            registry.gauge("eleme.users.online", 0);
            
            // 系统错误数
            registry.gauge("eleme.system.errors", 0);
        };
    }
} 