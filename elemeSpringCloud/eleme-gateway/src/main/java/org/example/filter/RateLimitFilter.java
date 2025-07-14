package org.example.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * 限流过滤器
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 需要限流的路径
     */
    private static final List<String> RATE_LIMIT_PATHS = Arrays.asList(
        "/api/user/login",
        "/api/user/register",
        "/api/order/addUserOrder",
        "/api/payment"
    );

    /**
     * 限流配置：每个IP每分钟最多请求次数
     */
    private static final int MAX_REQUESTS_PER_MINUTE = 60;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 检查是否需要限流
        if (!shouldRateLimit(path)) {
            return chain.filter(exchange);
        }

        String clientIp = getClientIp(request);
        String redisKey = "rate_limit:" + clientIp + ":" + path;

        try {
            // 获取当前请求数
            String currentRequestsStr = redisTemplate.opsForValue().get(redisKey);
            int currentRequests = currentRequestsStr != null ? Integer.parseInt(currentRequestsStr) : 0;

            if (currentRequests >= MAX_REQUESTS_PER_MINUTE) {
                log.warn("IP {} 对路径 {} 的请求超过限制，当前请求数: {}", clientIp, path, currentRequests);
                return handleRateLimitExceeded(exchange);
            }

            // 增加请求计数
            if (currentRequests == 0) {
                // 首次请求，设置过期时间
                redisTemplate.opsForValue().set(redisKey, "1", Duration.ofMinutes(1));
            } else {
                // 增加计数
                redisTemplate.opsForValue().increment(redisKey);
            }

            log.debug("IP {} 对路径 {} 的请求计数: {}", clientIp, path, currentRequests + 1);

        } catch (Exception e) {
            log.error("限流检查失败，IP: {}, 路径: {}", clientIp, path, e);
            // 如果Redis异常，继续处理请求
        }

        return chain.filter(exchange);
    }

    /**
     * 检查是否需要限流
     */
    private boolean shouldRateLimit(String path) {
        return RATE_LIMIT_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null 
            ? request.getRemoteAddress().getAddress().getHostAddress() 
            : "unknown";
    }

    /**
     * 处理限流超过的请求
     */
    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        String body = String.format(
            "{\"code\": %d, \"message\": \"%s\", \"data\": null, \"timestamp\": %d}",
            429, "请求过于频繁，请稍后再试", System.currentTimeMillis()
        );

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Flux.just(buffer));
    }

    @Override
    public int getOrder() {
        return -80; // 限流过滤器优先级较高，在认证过滤器之后
    }
} 