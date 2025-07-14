package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Map;

/**
 * 短信发送请求DTO
 */
@Data
public class SmsRequestDTO {
    
    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 模板代码
     */
    @NotBlank(message = "模板代码不能为空")
    private String templateCode;
    
    /**
     * 模板参数
     */
    private Map<String, Object> templateParams;
    
    /**
     * 业务类型
     */
    private String businessType;
    
    /**
     * 业务ID
     */
    private String businessId;
} 