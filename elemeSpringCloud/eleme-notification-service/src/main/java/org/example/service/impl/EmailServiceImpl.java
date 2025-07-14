package org.example.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.EmailRequestDTO;
import org.example.dto.NotificationResponseDTO;
import org.example.entity.Notification;
import org.example.entity.NotificationType;
import org.example.entity.NotificationStatus;
import org.example.repository.NotificationRepository;
import org.example.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 邮件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;
    private final StringRedisTemplate redisTemplate;
    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;
    
    @Value("${spring.mail.username}")
    private String from;
    
    @Value("${notification.email.personal:饿了么平台}")
    private String personal;
    
    @Override
    @Async
    @SentinelResource(value = "sendEmail", fallback = "sendEmailFallback")
    public NotificationResponseDTO sendEmail(EmailRequestDTO emailRequest) {
        log.info("开始发送邮件，收件人：{}", emailRequest.getTo());
        
        // 创建通知记录
        Notification notification = createNotificationRecord(emailRequest);
        
        try {
            // 发送邮件
            if (emailRequest.getIsHtml()) {
                sendHtmlEmailInternal(emailRequest);
            } else {
                sendSimpleEmailInternal(emailRequest);
            }
            
            // 更新发送状态
            notification.setStatus(NotificationStatus.SENT);
            notification.setSendTime(LocalDateTime.now());
            notificationRepository.save(notification);
            
            // 缓存发送结果
            cacheEmailResult(emailRequest.getTo(), "SUCCESS");
            
            log.info("邮件发送成功，收件人：{}", emailRequest.getTo());
            
        } catch (Exception e) {
            log.error("邮件发送失败，收件人：{}，错误：{}", emailRequest.getTo(), e.getMessage(), e);
            
            // 更新失败状态
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);
            
            // 缓存失败结果
            cacheEmailResult(emailRequest.getTo(), "FAILED");
            
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
        
        return convertToResponseDTO(notification);
    }
    
    @Override
    public NotificationResponseDTO sendSimpleEmail(String to, String subject, String content) {
        EmailRequestDTO emailRequest = new EmailRequestDTO();
        emailRequest.setTo(to);
        emailRequest.setSubject(subject);
        emailRequest.setContent(content);
        emailRequest.setIsHtml(false);
        
        return sendEmail(emailRequest);
    }
    
    @Override
    public NotificationResponseDTO sendHtmlEmail(String to, String subject, String htmlContent) {
        EmailRequestDTO emailRequest = new EmailRequestDTO();
        emailRequest.setTo(to);
        emailRequest.setSubject(subject);
        emailRequest.setContent(htmlContent);
        emailRequest.setIsHtml(true);
        
        return sendEmail(emailRequest);
    }
    
    @Override
    public NotificationResponseDTO sendTemplateEmail(String to, String subject, String templateCode, Object templateParams) {
        try {
            // 使用模板引擎渲染内容
            Context context = new Context();
            context.setVariable("data", templateParams);
            String content = templateEngine.process(templateCode, context);
            
            return sendHtmlEmail(to, subject, content);
        } catch (Exception e) {
            log.error("模板邮件发送失败，模板：{}，错误：{}", templateCode, e.getMessage(), e);
            throw new RuntimeException("模板邮件发送失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 发送简单文本邮件
     */
    private void sendSimpleEmailInternal(EmailRequestDTO emailRequest) throws Exception {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(emailRequest.getTo());
        message.setSubject(emailRequest.getSubject());
        message.setText(emailRequest.getContent());
        
        if (!CollectionUtils.isEmpty(emailRequest.getCc())) {
            message.setCc(emailRequest.getCc().toArray(new String[0]));
        }
        if (!CollectionUtils.isEmpty(emailRequest.getBcc())) {
            message.setBcc(emailRequest.getBcc().toArray(new String[0]));
        }
        
        mailSender.send(message);
    }
    
    /**
     * 发送HTML邮件
     */
    private void sendHtmlEmailInternal(EmailRequestDTO emailRequest) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(from, personal);
        helper.setTo(emailRequest.getTo());
        helper.setSubject(emailRequest.getSubject());
        helper.setText(emailRequest.getContent(), true);
        
        if (!CollectionUtils.isEmpty(emailRequest.getCc())) {
            helper.setCc(emailRequest.getCc().toArray(new String[0]));
        }
        if (!CollectionUtils.isEmpty(emailRequest.getBcc())) {
            helper.setBcc(emailRequest.getBcc().toArray(new String[0]));
        }
        
        // 处理附件
        if (!CollectionUtils.isEmpty(emailRequest.getAttachments())) {
            emailRequest.getAttachments().forEach(attachment -> {
                try {
                    byte[] data = java.util.Base64.getDecoder().decode(attachment.getContent());
                    helper.addAttachment(attachment.getName(), () -> new java.io.ByteArrayInputStream(data));
                } catch (Exception e) {
                    log.error("添加附件失败：{}", attachment.getName(), e);
                }
            });
        }
        
        mailSender.send(message);
    }
    
    /**
     * 创建通知记录
     */
    private Notification createNotificationRecord(EmailRequestDTO emailRequest) {
        Notification notification = new Notification();
        notification.setType(NotificationType.EMAIL);
        notification.setReceiver(emailRequest.getTo());
        notification.setSender(from);
        notification.setTitle(emailRequest.getSubject());
        notification.setContent(emailRequest.getContent());
        notification.setStatus(NotificationStatus.PENDING);
        notification.setBusinessType(emailRequest.getBusinessType());
        notification.setBusinessId(emailRequest.getBusinessId());
        notification.setUserId(emailRequest.getUserId());
        
        if (StringUtils.hasText(emailRequest.getTemplateCode())) {
            notification.setTemplateCode(emailRequest.getTemplateCode());
            try {
                notification.setTemplateParams(objectMapper.writeValueAsString(emailRequest.getTemplateParams()));
            } catch (Exception e) {
                log.error("模板参数序列化失败", e);
            }
        }
        
        return notificationRepository.save(notification);
    }
    
    /**
     * 缓存邮件发送结果
     */
    private void cacheEmailResult(String to, String status) {
        try {
            String key = "email:result:" + to;
            redisTemplate.opsForValue().set(key, status, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("缓存邮件发送结果失败", e);
        }
    }
    
    /**
     * 转换为响应DTO
     */
    private NotificationResponseDTO convertToResponseDTO(Notification notification) {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType().name());
        dto.setReceiver(notification.getReceiver());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setStatus(notification.getStatus().name());
        dto.setBusinessType(notification.getBusinessType());
        dto.setBusinessId(notification.getBusinessId());
        dto.setCreateTime(notification.getCreateTime());
        dto.setSendTime(notification.getSendTime());
        dto.setFailureReason(notification.getFailureReason());
        return dto;
    }
    
    /**
     * 邮件发送失败回调
     */
    public NotificationResponseDTO sendEmailFallback(EmailRequestDTO emailRequest, Throwable ex) {
        log.error("邮件发送降级处理，收件人：{}，错误：{}", emailRequest.getTo(), ex.getMessage());
        
        NotificationResponseDTO response = new NotificationResponseDTO();
        response.setType(NotificationType.EMAIL.name());
        response.setReceiver(emailRequest.getTo());
        response.setStatus(NotificationStatus.FAILED.name());
        response.setFailureReason("服务降级：" + ex.getMessage());
        response.setCreateTime(LocalDateTime.now());
        
        return response;
    }
} 