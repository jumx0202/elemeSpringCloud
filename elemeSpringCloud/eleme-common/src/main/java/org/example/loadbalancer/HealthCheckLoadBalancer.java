package org.example.loadbalancer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 健康检查负载均衡器
 * 过滤掉不健康的实例，然后使用轮询策略
 */
@Slf4j
public class HealthCheckLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    
    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final String serviceId;
    
    public HealthCheckLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
                                  String serviceId) {
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.serviceId = serviceId;
    }
    
    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                .getIfAvailable(NoopServiceInstanceListSupplier::new);
        
        return supplier.get(request)
                .next()
                .map(serviceInstances -> processInstanceResponse(serviceInstances, request));
    }
    
    private Response<ServiceInstance> processInstanceResponse(List<ServiceInstance> serviceInstances, Request request) {
        if (serviceInstances.isEmpty()) {
            log.warn("No servers available for service: {}", serviceId);
            return new EmptyResponse();
        }
        
        // 过滤健康的实例
        List<ServiceInstance> healthyInstances = serviceInstances.stream()
                .filter(this::isHealthy)
                .collect(Collectors.toList());
        
        if (healthyInstances.isEmpty()) {
            log.warn("No healthy servers available for service: {}, falling back to all instances", serviceId);
            healthyInstances = serviceInstances; // 如果没有健康实例，使用所有实例
        }
        
        return getInstanceResponse(healthyInstances);
    }
    
    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances) {
        if (instances.size() == 1) {
            return new DefaultResponse(instances.get(0));
        }
        
        // 使用轮询策略选择实例
        ServiceInstance selected = instances.get((int) (System.currentTimeMillis() % instances.size()));
        log.debug("Selected healthy instance: {}", selected.getInstanceId());
        return new DefaultResponse(selected);
    }
    
    /**
     * 检查服务实例是否健康
     * 从实例的metadata中获取健康状态信息
     */
    private boolean isHealthy(ServiceInstance instance) {
        Map<String, String> metadata = instance.getMetadata();
        if (metadata != null) {
            // 检查健康状态
            String healthStatus = metadata.get("health.status");
            if (healthStatus != null) {
                boolean healthy = "UP".equalsIgnoreCase(healthStatus) || "HEALTHY".equalsIgnoreCase(healthStatus);
                if (!healthy) {
                    log.debug("Instance {} is not healthy, status: {}", instance.getInstanceId(), healthStatus);
                }
                return healthy;
            }
            
            // 检查是否被手动禁用
            String enabled = metadata.get("enabled");
            if (enabled != null) {
                boolean isEnabled = Boolean.parseBoolean(enabled);
                if (!isEnabled) {
                    log.debug("Instance {} is disabled", instance.getInstanceId());
                }
                return isEnabled;
            }
        }
        
        // 默认认为是健康的
        return true;
    }
    
    /**
     * 空实现的ServiceInstanceListSupplier
     */
    private static class NoopServiceInstanceListSupplier implements ServiceInstanceListSupplier {
        @Override
        public String getServiceId() {
            return null;
        }
        
        @Override
        public reactor.core.publisher.Flux<List<ServiceInstance>> get() {
            return reactor.core.publisher.Flux.empty();
        }
        
        @Override
        public reactor.core.publisher.Flux<List<ServiceInstance>> get(Request request) {
            return reactor.core.publisher.Flux.empty();
        }
    }
} 