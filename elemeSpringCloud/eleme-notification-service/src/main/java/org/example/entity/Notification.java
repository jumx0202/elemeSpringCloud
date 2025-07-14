package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 通知类型：EMAIL, SMS, SYSTEM
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    /**
     * 接收者
     */
    @Column(nullable = false)
    private String receiver;
    
    /**
     * 发送者
     */
    private String sender;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;
    
    /**
     * 模板代码
     */
    private String templateCode;
    
    /**
     * 模板参数（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String templateParams;
    
    /**
     * 发送状态：PENDING, SENT, FAILED
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
    
    /**
     * 业务类型：VERIFICATION, ORDER_STATUS, PAYMENT, PROMOTION等
     */
    private String businessType;
    
    /**
     * 业务ID
     */
    private String businessId;
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 重试次数
     */
    @Column(nullable = false)
    private Integer retryCount = 0;
    
    /**
     * 最大重试次数
     */
    @Column(nullable = false)
    private Integer maxRetryCount = 3;
    
    /**
     * 失败原因
     */
    private String failureReason;
    
    /**
     * 外部通知ID（如短信服务商返回的ID）
     */
    private String externalId;
    
    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    
    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        if (this.status == null) {
            this.status = NotificationStatus.PENDING;
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
} 