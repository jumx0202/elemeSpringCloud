package org.example.service;

import org.example.dto.CaptchaGenerateRequest;
import org.example.dto.CaptchaValidateRequest;
import org.example.dto.CaptchaResponse;

/**
 * 验证码服务接口
 */
public interface CaptchaService {
    
    /**
     * 生成验证码
     */
    CaptchaResponse generateCaptcha(CaptchaGenerateRequest request);
    
    /**
     * 验证验证码
     */
    boolean validateCaptcha(CaptchaValidateRequest request);
    
    /**
     * 生成图形验证码
     */
    CaptchaResponse generateImageCaptcha(CaptchaGenerateRequest request);
    
    /**
     * 生成短信验证码
     */
    CaptchaResponse generateSmsVerificationCode(CaptchaGenerateRequest request);
    
    /**
     * 生成邮箱验证码
     */
    CaptchaResponse generateEmailVerificationCode(CaptchaGenerateRequest request);
    
    /**
     * 生成滑动验证码
     */
    CaptchaResponse generateSliderCaptcha(CaptchaGenerateRequest request);
    
    /**
     * 生成点选验证码
     */
    CaptchaResponse generateClickCaptcha(CaptchaGenerateRequest request);
    
    /**
     * 检查频率限制
     */
    boolean checkRateLimit(String type, String identifier);
    
    /**
     * 清理过期验证码
     */
    void cleanupExpiredCaptcha();
} 