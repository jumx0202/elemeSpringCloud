package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.CaptchaGenerateRequest;
import org.example.dto.CaptchaValidateRequest;
import org.example.dto.CaptchaResponse;
import org.example.service.CaptchaService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * 验证码控制器
 */
@Slf4j
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
@Validated
@Tag(name = "验证码服务", description = "提供各种类型的验证码生成和验证功能")
public class CaptchaController {
    
    private final CaptchaService captchaService;
    
    /**
     * 生成验证码
     */
    @PostMapping("/generate")
    @Operation(summary = "生成验证码", description = "根据类型生成不同的验证码")
    public ResponseEntity<CaptchaResponse> generateCaptcha(
            @Valid @RequestBody CaptchaGenerateRequest request,
            HttpServletRequest httpRequest) {
        
        // 设置客户端信息
        if (request.getClientIp() == null) {
            request.setClientIp(getClientIp(httpRequest));
        }
        if (request.getUserAgent() == null) {
            request.setUserAgent(httpRequest.getHeader("User-Agent"));
        }
        
        log.info("收到验证码生成请求，类型：{}", request.getType());
        
        CaptchaResponse response = captchaService.generateCaptcha(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 验证验证码
     */
    @PostMapping("/validate")
    @Operation(summary = "验证验证码", description = "验证各种类型的验证码")
    public ResponseEntity<Map<String, Object>> validateCaptcha(
            @Valid @RequestBody CaptchaValidateRequest request,
            HttpServletRequest httpRequest) {
        
        // 设置客户端信息
        if (request.getClientIp() == null) {
            request.setClientIp(getClientIp(httpRequest));
        }
        
        log.info("收到验证码验证请求，类型：{}，Key：{}", request.getType(), request.getKey());
        
        boolean isValid = captchaService.validateCaptcha(request);
        
        Map<String, Object> result = Map.of(
            "valid", isValid,
            "message", isValid ? "验证码验证成功" : "验证码验证失败"
        );
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 生成图形验证码
     */
    @PostMapping("/image")
    @Operation(summary = "生成图形验证码", description = "生成图形验证码")
    public ResponseEntity<CaptchaResponse> generateImageCaptcha(
            @RequestBody(required = false) CaptchaGenerateRequest request,
            HttpServletRequest httpRequest) {
        
        if (request == null) {
            request = new CaptchaGenerateRequest();
        }
        request.setType("image");
        request.setClientIp(getClientIp(httpRequest));
        request.setUserAgent(httpRequest.getHeader("User-Agent"));
        
        log.info("收到图形验证码生成请求");
        
        CaptchaResponse response = captchaService.generateImageCaptcha(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 生成短信验证码
     */
    @PostMapping("/sms")
    @Operation(summary = "生成短信验证码", description = "生成短信验证码")
    public ResponseEntity<CaptchaResponse> generateSmsVerificationCode(
            @RequestParam String phone,
            @RequestParam(required = false) String scene,
            HttpServletRequest httpRequest) {
        
        CaptchaGenerateRequest request = new CaptchaGenerateRequest();
        request.setType("sms");
        request.setReceiver(phone);
        request.setScene(scene);
        request.setClientIp(getClientIp(httpRequest));
        request.setUserAgent(httpRequest.getHeader("User-Agent"));
        
        log.info("收到短信验证码生成请求，手机号：{}", phone);
        
        CaptchaResponse response = captchaService.generateSmsVerificationCode(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 生成邮箱验证码
     */
    @PostMapping("/email")
    @Operation(summary = "生成邮箱验证码", description = "生成邮箱验证码")
    public ResponseEntity<CaptchaResponse> generateEmailVerificationCode(
            @RequestParam String email,
            @RequestParam(required = false) String scene,
            HttpServletRequest httpRequest) {
        
        CaptchaGenerateRequest request = new CaptchaGenerateRequest();
        request.setType("email");
        request.setReceiver(email);
        request.setScene(scene);
        request.setClientIp(getClientIp(httpRequest));
        request.setUserAgent(httpRequest.getHeader("User-Agent"));
        
        log.info("收到邮箱验证码生成请求，邮箱：{}", email);
        
        CaptchaResponse response = captchaService.generateEmailVerificationCode(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 生成滑动验证码
     */
    @PostMapping("/slider")
    @Operation(summary = "生成滑动验证码", description = "生成滑动验证码")
    public ResponseEntity<CaptchaResponse> generateSliderCaptcha(
            @RequestBody(required = false) CaptchaGenerateRequest request,
            HttpServletRequest httpRequest) {
        
        if (request == null) {
            request = new CaptchaGenerateRequest();
        }
        request.setType("slider");
        request.setClientIp(getClientIp(httpRequest));
        request.setUserAgent(httpRequest.getHeader("User-Agent"));
        
        log.info("收到滑动验证码生成请求");
        
        CaptchaResponse response = captchaService.generateSliderCaptcha(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 生成点选验证码
     */
    @PostMapping("/click")
    @Operation(summary = "生成点选验证码", description = "生成点选验证码")
    public ResponseEntity<CaptchaResponse> generateClickCaptcha(
            @RequestBody(required = false) CaptchaGenerateRequest request,
            HttpServletRequest httpRequest) {
        
        if (request == null) {
            request = new CaptchaGenerateRequest();
        }
        request.setType("click");
        request.setClientIp(getClientIp(httpRequest));
        request.setUserAgent(httpRequest.getHeader("User-Agent"));
        
        log.info("收到点选验证码生成请求");
        
        CaptchaResponse response = captchaService.generateClickCaptcha(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 验证图形验证码
     */
    @PostMapping("/image/validate")
    @Operation(summary = "验证图形验证码", description = "验证图形验证码")
    public ResponseEntity<Map<String, Object>> validateImageCaptcha(
            @RequestParam String key,
            @RequestParam String value,
            HttpServletRequest httpRequest) {
        
        CaptchaValidateRequest request = new CaptchaValidateRequest();
        request.setType("image");
        request.setKey(key);
        request.setValue(value);
        request.setClientIp(getClientIp(httpRequest));
        
        log.info("收到图形验证码验证请求，Key：{}", key);
        
        boolean isValid = captchaService.validateCaptcha(request);
        
        Map<String, Object> result = Map.of(
            "valid", isValid,
            "message", isValid ? "验证码验证成功" : "验证码验证失败"
        );
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 验证短信验证码
     */
    @PostMapping("/sms/validate")
    @Operation(summary = "验证短信验证码", description = "验证短信验证码")
    public ResponseEntity<Map<String, Object>> validateSmsVerificationCode(
            @RequestParam String key,
            @RequestParam String code,
            @RequestParam String phone,
            HttpServletRequest httpRequest) {
        
        CaptchaValidateRequest request = new CaptchaValidateRequest();
        request.setType("sms");
        request.setKey(key);
        request.setValue(code);
        request.setReceiver(phone);
        request.setClientIp(getClientIp(httpRequest));
        
        log.info("收到短信验证码验证请求，Key：{}，手机号：{}", key, phone);
        
        boolean isValid = captchaService.validateCaptcha(request);
        
        Map<String, Object> result = Map.of(
            "valid", isValid,
            "message", isValid ? "验证码验证成功" : "验证码验证失败"
        );
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 验证邮箱验证码
     */
    @PostMapping("/email/validate")
    @Operation(summary = "验证邮箱验证码", description = "验证邮箱验证码")
    public ResponseEntity<Map<String, Object>> validateEmailVerificationCode(
            @RequestParam String key,
            @RequestParam String code,
            @RequestParam String email,
            HttpServletRequest httpRequest) {
        
        CaptchaValidateRequest request = new CaptchaValidateRequest();
        request.setType("email");
        request.setKey(key);
        request.setValue(code);
        request.setReceiver(email);
        request.setClientIp(getClientIp(httpRequest));
        
        log.info("收到邮箱验证码验证请求，Key：{}，邮箱：{}", key, email);
        
        boolean isValid = captchaService.validateCaptcha(request);
        
        Map<String, Object> result = Map.of(
            "valid", isValid,
            "message", isValid ? "验证码验证成功" : "验证码验证失败"
        );
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查验证码服务健康状态")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "service", "eleme-captcha-service",
            "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(health);
    }
    
    /**
     * 清理过期验证码
     */
    @PostMapping("/cleanup")
    @Operation(summary = "清理过期验证码", description = "清理过期的验证码")
    public ResponseEntity<Map<String, String>> cleanup() {
        log.info("开始清理过期验证码");
        
        try {
            captchaService.cleanupExpiredCaptcha();
            return ResponseEntity.ok(Map.of("message", "清理过期验证码成功"));
        } catch (Exception e) {
            log.error("清理过期验证码失败", e);
            return ResponseEntity.ok(Map.of("message", "清理过期验证码失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
} 