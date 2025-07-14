package org.example.service;

import org.example.dto.SmsRequestDTO;
import org.example.dto.NotificationResponseDTO;

/**
 * 短信服务接口
 */
public interface SmsService {
    
    /**
     * 发送短信
     */
    NotificationResponseDTO sendSms(SmsRequestDTO smsRequest);
    
    /**
     * 发送简单短信
     */
    NotificationResponseDTO sendSimpleSms(String phone, String content);
    
    /**
     * 发送模板短信
     */
    NotificationResponseDTO sendTemplateSms(String phone, String templateCode, Object templateParams);
} 