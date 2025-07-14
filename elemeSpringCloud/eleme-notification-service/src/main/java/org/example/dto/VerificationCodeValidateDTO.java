package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 验证码验证请求DTO
 */
@Data
public class VerificationCodeValidateDTO {
    
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
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String code;
    
    /**
     * 验证成功后是否立即使用（标记为已使用）
     */
    private Boolean useImmediately = true;
} 