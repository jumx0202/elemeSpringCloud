package org.example.service;

import org.example.dto.VerificationCodeRequestDTO;
import org.example.dto.VerificationCodeValidateDTO;
import org.example.dto.NotificationResponseDTO;

/**
 * 验证码服务接口
 */
public interface VerificationCodeService {
    
    /**
     * 发送验证码
     */
    NotificationResponseDTO sendVerificationCode(VerificationCodeRequestDTO request);
    
    /**
     * 验证验证码
     */
    boolean validateVerificationCode(VerificationCodeValidateDTO validateDTO);
    
    /**
     * 检查验证码发送频率限制
     */
    boolean checkRateLimit(String receiver, String clientIp);
    
    /**
     * 生成验证码
     */
    String generateCode(int length);
    
    /**
     * 清理过期验证码
     */
    void cleanupExpiredCodes();
} 