package org.example.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知响应DTO
 */
@Data
public class NotificationResponseDTO {
    
    /**
     * 通知ID
     */
    private Long id;
    
    /**
     * 通知类型
     */
    private String type;
    
    /**
     * 接收者
     */
    private String receiver;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 内容
     */
    private String content;
    
    /**
     * 发送状态
     */
    private String status;
    
    /**
     * 业务类型
     */
    private String businessType;
    
    /**
     * 业务ID
     */
    private String businessId;
    
    /**
     * 外部通知ID
     */
    private String externalId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
    
    /**
     * 失败原因
     */
    private String failureReason;
} 