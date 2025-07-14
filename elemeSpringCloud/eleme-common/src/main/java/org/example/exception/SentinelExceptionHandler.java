package org.example.exception;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Sentinel异常处理器
 */
@Slf4j
@RestControllerAdvice
public class SentinelExceptionHandler {
    
    /**
     * 处理限流异常
     */
    @ExceptionHandler(FlowException.class)
    public ResponseEntity<Map<String, Object>> handleFlowException(FlowException ex) {
        log.warn("触发限流规则：{}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("message", "请求过于频繁，请稍后再试");
        response.put("type", "FLOW_LIMIT");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
    
    /**
     * 处理熔断异常
     */
    @ExceptionHandler(DegradeException.class)
    public ResponseEntity<Map<String, Object>> handleDegradeException(DegradeException ex) {
        log.warn("触发熔断规则：{}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("message", "服务暂时不可用，请稍后再试");
        response.put("type", "CIRCUIT_BREAKER");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    /**
     * 处理系统保护异常
     */
    @ExceptionHandler(SystemBlockException.class)
    public ResponseEntity<Map<String, Object>> handleSystemBlockException(SystemBlockException ex) {
        log.warn("触发系统保护规则：{}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("message", "系统负载过高，请稍后再试");
        response.put("type", "SYSTEM_PROTECTION");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    /**
     * 处理热点参数限流异常
     */
    @ExceptionHandler(ParamFlowException.class)
    public ResponseEntity<Map<String, Object>> handleParamFlowException(ParamFlowException ex) {
        log.warn("触发热点参数限流规则：{}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("message", "热点参数访问过于频繁，请稍后再试");
        response.put("type", "PARAM_FLOW_LIMIT");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
    
    /**
     * 处理授权异常
     */
    @ExceptionHandler(AuthorityException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorityException(AuthorityException ex) {
        log.warn("触发授权规则：{}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", HttpStatus.FORBIDDEN.value());
        response.put("message", "访问被拒绝，权限不足");
        response.put("type", "AUTHORITY_DENY");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    /**
     * 处理其他Sentinel阻塞异常
     */
    @ExceptionHandler(BlockException.class)
    public ResponseEntity<Map<String, Object>> handleBlockException(BlockException ex) {
        log.warn("触发Sentinel阻塞规则：{}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("message", "请求被阻塞，请稍后再试");
        response.put("type", "SENTINEL_BLOCK");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
} 