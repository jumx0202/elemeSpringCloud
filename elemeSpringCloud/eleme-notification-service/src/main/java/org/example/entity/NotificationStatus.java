package org.example.entity;

/**
 * 通知状态枚举
 */
public enum NotificationStatus {
    /**
     * 待发送
     */
    PENDING,
    
    /**
     * 已发送
     */
    SENT,
    
    /**
     * 发送失败
     */
    FAILED,
    
    /**
     * 已过期
     */
    EXPIRED
} 