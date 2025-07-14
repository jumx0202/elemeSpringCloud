package org.example.loadbalancer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 基于权重的负载均衡器
 */
@Slf4j
public class WeightedLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    
    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final String serviceId;
    private final Random random = new Random();
    
    public WeightedLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
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
        
        return getInstanceResponse(serviceInstances, request);
    }
    
    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances, Request request) {
        // 如果只有一个实例，直接返回
        if (instances.size() == 1) {
            return new DefaultResponse(instances.get(0));
        }
        
        // 计算总权重
        int totalWeight = 0;
        for (ServiceInstance instance : instances) {
            int weight = getWeight(instance);
            totalWeight += weight;
        }
        
        if (totalWeight == 0) {
            // 如果所有实例权重都为0，使用随机策略
            return new DefaultResponse(instances.get(ThreadLocalRandom.current().nextInt(instances.size())));
        }
        
        // 根据权重选择实例
        int randomWeight = ThreadLocalRandom.current().nextInt(totalWeight);
        int currentWeight = 0;
        
        for (ServiceInstance instance : instances) {
            currentWeight += getWeight(instance);
            if (randomWeight < currentWeight) {
                log.debug("Selected instance: {} with weight: {}", instance.getInstanceId(), getWeight(instance));
                return new DefaultResponse(instance);
            }
        }
        
        // 备选方案，返回第一个实例
        return new DefaultResponse(instances.get(0));
    }
    
    /**
     * 获取服务实例的权重
     * 从实例的metadata中获取权重信息，默认权重为1
     */
    private int getWeight(ServiceInstance instance) {
        Map<String, String> metadata = instance.getMetadata();
        if (metadata != null && metadata.containsKey("weight")) {
            try {
                int weight = Integer.parseInt(metadata.get("weight"));
                return Math.max(weight, 0); // 确保权重不为负数
            } catch (NumberFormatException e) {
                log.warn("Invalid weight value for instance {}: {}", instance.getInstanceId(), metadata.get("weight"));
            }
        }
        return 1; // 默认权重
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
        public Flux<List<ServiceInstance>> get() {
            return Flux.empty();
        }
        
        @Override
        public Flux<List<ServiceInstance>> get(Request request) {
            return Flux.empty();
        }
    }
} 