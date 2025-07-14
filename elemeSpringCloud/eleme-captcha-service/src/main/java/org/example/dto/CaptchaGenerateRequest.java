package org.example.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 验证码生成请求DTO
 */
@Data
public class CaptchaGenerateRequest {
    
    /**
     * 验证码类型：image, sms, email, slider, click
     */
    @NotBlank(message = "验证码类型不能为空")
    private String type;
    
    /**
     * 接收者（手机号或邮箱，用于短信和邮箱验证码）
     */
    private String receiver;
    
    /**
     * 验证码长度（可选，使用默认配置）
     */
    private Integer length;
    
    /**
     * 图片宽度（图形验证码）
     */
    private Integer width;
    
    /**
     * 图片高度（图形验证码）
     */
    private Integer height;
    
    /**
     * 客户端IP
     */
    private String clientIp;
    
    /**
     * 用户代理
     */
    private String userAgent;
    
    /**
     * 业务场景：login, register, resetPassword等
     */
    private String scene;
} 