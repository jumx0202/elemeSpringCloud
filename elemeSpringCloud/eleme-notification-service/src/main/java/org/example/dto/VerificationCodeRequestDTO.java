package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.example.entity.NotificationType;

/**
 * 验证码发送请求DTO
 */
@Data
public class VerificationCodeRequestDTO {
    
    /**
     * 接收者（手机号或邮箱）
     */
    @NotBlank(message = "接收者不能为空")
    private String receiver;
    
    /**
     * 验证码类型
     */
    @NotBlank(message = "验证码类型不能为空")
    private String codeType;
    
    /**
     * 发送方式
     */
    private NotificationType sendType;
    
    /**
     * 客户端IP
     */
    private String clientIp;
    
    /**
     * 用户代理
     */
    private String userAgent;
} 