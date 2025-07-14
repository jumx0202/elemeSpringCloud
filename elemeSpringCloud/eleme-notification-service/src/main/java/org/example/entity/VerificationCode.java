package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 验证码实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "verification_codes")
public class VerificationCode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 验证码类型：REGISTER, LOGIN, RESET_PASSWORD, CHANGE_PHONE等
     */
    @Column(nullable = false)
    private String codeType;
    
    /**
     * 接收者（手机号或邮箱）
     */
    @Column(nullable = false)
    private String receiver;
    
    /**
     * 验证码
     */
    @Column(nullable = false)
    private String code;
    
    /**
     * 发送方式：SMS, EMAIL
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType sendType;
    
    /**
     * 是否已使用
     */
    @Column(nullable = false)
    private Boolean used = false;
    
    /**
     * 验证尝试次数
     */
    @Column(nullable = false)
    private Integer attempts = 0;
    
    /**
     * 最大验证尝试次数
     */
    @Column(nullable = false)
    private Integer maxAttempts = 3;
    
    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createTime;
    
    /**
     * 过期时间
     */
    @Column(nullable = false)
    private LocalDateTime expireTime;
    
    /**
     * 使用时间
     */
    private LocalDateTime useTime;
    
    /**
     * 客户端IP
     */
    private String clientIp;
    
    /**
     * 用户代理
     */
    private String userAgent;
    
    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        // 默认5分钟过期
        this.expireTime = LocalDateTime.now().plusMinutes(5);
    }
    
    /**
     * 检查验证码是否有效
     */
    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expireTime) && attempts < maxAttempts;
    }
    
    /**
     * 检查验证码是否过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireTime);
    }
} 