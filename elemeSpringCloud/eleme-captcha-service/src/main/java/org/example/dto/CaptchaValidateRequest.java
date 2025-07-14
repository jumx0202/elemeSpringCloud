package org.example.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 验证码验证请求DTO
 */
@Data
public class CaptchaValidateRequest {
    
    /**
     * 验证码类型：image, sms, email, slider, click
     */
    @NotBlank(message = "验证码类型不能为空")
    private String type;
    
    /**
     * 验证码Key
     */
    @NotBlank(message = "验证码Key不能为空")
    private String key;
    
    /**
     * 验证码值
     */
    @NotBlank(message = "验证码值不能为空")
    private String value;
    
    /**
     * 接收者（手机号或邮箱，用于短信和邮箱验证码）
     */
    private String receiver;
    
    /**
     * 滑动验证码位置（滑动验证码）
     */
    private Integer sliderPosition;
    
    /**
     * 点选验证码坐标（点选验证码）
     */
    private List<CaptchaPoint> clickPoints;
    
    /**
     * 验证成功后是否立即使用（标记为已使用）
     */
    private Boolean useImmediately = true;
    
    /**
     * 客户端IP
     */
    private String clientIp;
    
    /**
     * 点选验证码坐标点
     */
    @Data
    public static class CaptchaPoint {
        private Integer x;
        private Integer y;
    }
} 