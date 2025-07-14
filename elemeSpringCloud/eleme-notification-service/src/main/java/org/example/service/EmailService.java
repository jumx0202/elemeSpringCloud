package org.example.service;

import org.example.dto.EmailRequestDTO;
import org.example.dto.NotificationResponseDTO;

/**
 * 邮件服务接口
 */
public interface EmailService {
    
    /**
     * 发送邮件
     */
    NotificationResponseDTO sendEmail(EmailRequestDTO emailRequest);
    
    /**
     * 发送简单文本邮件
     */
    NotificationResponseDTO sendSimpleEmail(String to, String subject, String content);
    
    /**
     * 发送HTML邮件
     */
    NotificationResponseDTO sendHtmlEmail(String to, String subject, String htmlContent);
    
    /**
     * 发送模板邮件
     */
    NotificationResponseDTO sendTemplateEmail(String to, String subject, String templateCode, Object templateParams);
} 