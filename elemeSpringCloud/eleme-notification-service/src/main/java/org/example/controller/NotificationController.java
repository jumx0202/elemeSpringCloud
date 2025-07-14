package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.*;
import org.example.service.EmailService;
import org.example.service.NotificationService;
import org.example.service.VerificationCodeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 通知服务控制器
 */
@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
@Validated
@Tag(name = "通知服务", description = "提供邮件、短信等通知功能")
public class NotificationController {
    
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final VerificationCodeService verificationCodeService;
    
    /**
     * 发送邮件
     */
    @PostMapping("/email/send")
    @Operation(summary = "发送邮件", description = "发送邮件通知")
    public ResponseEntity<NotificationResponseDTO> sendEmail(@Valid @RequestBody EmailRequestDTO emailRequest) {
        log.info("收到邮件发送请求，收件人：{}", emailRequest.getTo());
        
        NotificationResponseDTO response = emailService.sendEmail(emailRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 发送简单邮件
     */
    @PostMapping("/email/send-simple")
    @Operation(summary = "发送简单邮件", description = "发送简单文本邮件")
    public ResponseEntity<NotificationResponseDTO> sendSimpleEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String content) {
        
        log.info("收到简单邮件发送请求，收件人：{}", to);
        
        NotificationResponseDTO response = emailService.sendSimpleEmail(to, subject, content);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 发送HTML邮件
     */
    @PostMapping("/email/send-html")
    @Operation(summary = "发送HTML邮件", description = "发送HTML格式邮件")
    public ResponseEntity<NotificationResponseDTO> sendHtmlEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String htmlContent) {
        
        log.info("收到HTML邮件发送请求，收件人：{}", to);
        
        NotificationResponseDTO response = emailService.sendHtmlEmail(to, subject, htmlContent);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 发送模板邮件
     */
    @PostMapping("/email/send-template")
    @Operation(summary = "发送模板邮件", description = "使用模板发送邮件")
    public ResponseEntity<NotificationResponseDTO> sendTemplateEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String templateCode,
            @RequestBody Object templateParams) {
        
        log.info("收到模板邮件发送请求，收件人：{}，模板：{}", to, templateCode);
        
        NotificationResponseDTO response = emailService.sendTemplateEmail(to, subject, templateCode, templateParams);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 发送验证码
     */
    @PostMapping("/verification-code/send")
    @Operation(summary = "发送验证码", description = "发送短信或邮件验证码")
    public ResponseEntity<NotificationResponseDTO> sendVerificationCode(
            @Valid @RequestBody VerificationCodeRequestDTO request,
            HttpServletRequest httpRequest) {
        
        // 设置客户端信息
        if (request.getClientIp() == null) {
            request.setClientIp(getClientIp(httpRequest));
        }
        if (request.getUserAgent() == null) {
            request.setUserAgent(httpRequest.getHeader("User-Agent"));
        }
        
        log.info("收到验证码发送请求，接收者：{}，类型：{}", request.getReceiver(), request.getCodeType());
        
        NotificationResponseDTO response = verificationCodeService.sendVerificationCode(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 验证验证码
     */
    @PostMapping("/verification-code/validate")
    @Operation(summary = "验证验证码", description = "验证短信或邮件验证码")
    public ResponseEntity<Map<String, Object>> validateVerificationCode(
            @Valid @RequestBody VerificationCodeValidateDTO validateDTO) {
        
        log.info("收到验证码验证请求，接收者：{}，类型：{}", validateDTO.getReceiver(), validateDTO.getCodeType());
        
        boolean isValid = verificationCodeService.validateVerificationCode(validateDTO);
        
        Map<String, Object> result = Map.of(
            "valid", isValid,
            "message", isValid ? "验证码验证成功" : "验证码验证失败"
        );
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 查询用户通知
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户通知", description = "分页查询用户的通知记录")
    public ResponseEntity<Page<NotificationResponseDTO>> getUserNotifications(
            @PathVariable Integer userId,
            Pageable pageable) {
        
        log.info("收到用户通知查询请求，用户ID：{}", userId);
        
        Page<NotificationResponseDTO> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * 查询通知详情
     */
    @GetMapping("/{notificationId}")
    @Operation(summary = "查询通知详情", description = "根据ID查询通知详情")
    public ResponseEntity<NotificationResponseDTO> getNotificationById(@PathVariable Long notificationId) {
        
        log.info("收到通知详情查询请求，通知ID：{}", notificationId);
        
        NotificationResponseDTO notification = notificationService.getNotificationById(notificationId);
        return ResponseEntity.ok(notification);
    }
    
    /**
     * 根据业务查询通知
     */
    @GetMapping("/business/{businessType}/{businessId}")
    @Operation(summary = "根据业务查询通知", description = "根据业务类型和业务ID查询通知")
    public ResponseEntity<List<NotificationResponseDTO>> getNotificationsByBusiness(
            @PathVariable String businessType,
            @PathVariable String businessId) {
        
        log.info("收到业务通知查询请求，业务类型：{}，业务ID：{}", businessType, businessId);
        
        List<NotificationResponseDTO> notifications = notificationService.getNotificationsByBusiness(businessType, businessId);
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查通知服务健康状态")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "service", "eleme-notification-service",
            "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(health);
    }
    
    /**
     * 清理过期数据
     */
    @PostMapping("/cleanup")
    @Operation(summary = "清理过期数据", description = "清理过期的通知和验证码")
    public ResponseEntity<Map<String, String>> cleanup() {
        log.info("开始清理过期数据");
        
        try {
            notificationService.cleanupExpiredNotifications();
            verificationCodeService.cleanupExpiredCodes();
            
            return ResponseEntity.ok(Map.of("message", "清理过期数据成功"));
        } catch (Exception e) {
            log.error("清理过期数据失败", e);
            return ResponseEntity.ok(Map.of("message", "清理过期数据失败：" + e.getMessage()));
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