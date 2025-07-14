package org.example.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 验证码响应DTO
 */
@Data
public class CaptchaResponse {
    
    /**
     * 验证码Key
     */
    private String key;
    
    /**
     * 验证码类型
     */
    private String type;
    
    /**
     * 图形验证码Base64编码（图形验证码）
     */
    private String imageBase64;
    
    /**
     * 图形验证码DataURL（图形验证码）
     */
    private String imageDataUrl;
    
    /**
     * 滑动验证码背景图片（滑动验证码）
     */
    private String backgroundImage;
    
    /**
     * 滑动验证码拼图图片（滑动验证码）
     */
    private String sliderImage;
    
    /**
     * 滑动验证码正确位置（服务端不返回，仅用于内部验证）
     */
    private Integer correctPosition;
    
    /**
     * 点选验证码图片（点选验证码）
     */
    private String clickImage;
    
    /**
     * 点选验证码提示文字（点选验证码）
     */
    private String clickText;
    
    /**
     * 验证码过期时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 验证码过期时间（秒）
     */
    private Integer expireSeconds;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 接收者（短信和邮箱验证码）
     */
    private String receiver;
    
    /**
     * 发送状态（短信和邮箱验证码）
     */
    private String sendStatus;
    
    /**
     * 成功标志
     */
    private Boolean success = true;
    
    /**
     * 错误信息
     */
    private String message;
} 