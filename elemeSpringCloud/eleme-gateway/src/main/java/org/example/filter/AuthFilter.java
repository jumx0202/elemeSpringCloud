package org.example.filter;

import lombok.extern.slf4j.Slf4j;
import org.example.constants.CommonConstants;
import org.example.utils.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 身份验证过滤器
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    /**
     * 不需要验证的路径
     */
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
        "/api/user/login",
        "/api/user/register",
        "/api/captcha",
        "/api/business/getAll",
        "/api/business/getBusinessById",
        "/api/business/getBusinessByType",
        "/api/business/", // 支持 /api/business/{id} 格式
        "/api/food/getFoodById",
        "/api/food/getAllByIds",
        "/api/food/business/", // 支持 /api/food/business/{id}/onsale 格式
        "/api/notification/sendVerificationCode",
        "/actuator",
        "/health",
        "/swagger-ui",
        "/swagger-ui.html",
        "/swagger-resources",
        "/swagger-config",
        "/v3/api-docs",
        "/webjars",
        "/doc.html",
        "/favicon.ico"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 检查是否需要跳过验证
        if (shouldSkipAuth(path)) {
            return chain.filter(exchange);
        }

        // 获取Authorization头
        String authorization = request.getHeaders().getFirst(CommonConstants.TOKEN_HEADER);
        
        if (authorization == null || !authorization.startsWith(CommonConstants.TOKEN_PREFIX)) {
            log.warn("请求路径：{} 缺少认证头", path);
            return handleUnauthorized(exchange, "缺少认证头");
        }

        // 提取Token
        String token = authorization.substring(CommonConstants.TOKEN_PREFIX.length());
        
        // 验证Token
        if (!JwtUtil.verifyToken(token)) {
            log.warn("请求路径：{} Token验证失败", path);
            return handleUnauthorized(exchange, "Token验证失败");
        }

        // 检查Token是否过期
        if (JwtUtil.isTokenExpired(token)) {
            log.warn("请求路径：{} Token已过期", path);
            return handleUnauthorized(exchange, "Token已过期");
        }

        // 获取用户信息并添加到请求头
        String userId = JwtUtil.getUserId(token);
        String userType = JwtUtil.getUserType(token);
        
        if (userId == null || userType == null) {
            log.warn("请求路径：{} Token中缺少用户信息", path);
            return handleUnauthorized(exchange, "Token中缺少用户信息");
        }

        // 添加用户信息到请求头
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Type", userType)
                .build();

        log.info("用户 {} (类型: {}) 访问路径：{}", userId, userType, path);

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    /**
     * 检查是否需要跳过认证
     */
    private boolean shouldSkipAuth(String path) {
        return EXCLUDE_PATHS.stream().anyMatch(excludePath -> 
            path.startsWith(excludePath) || path.contains(excludePath));
    }

    /**
     * 处理未授权请求
     */
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        String body = String.format(
            "{\"code\": %d, \"message\": \"%s\", \"data\": null, \"timestamp\": %d}",
            CommonConstants.UNAUTHORIZED_CODE, message, System.currentTimeMillis()
        );

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Flux.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 优先级最高
    }
} 