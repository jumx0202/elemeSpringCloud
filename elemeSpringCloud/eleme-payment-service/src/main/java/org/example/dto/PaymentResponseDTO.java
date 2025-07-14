package org.example.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponseDTO {

    /**
     * 支付记录ID
     */
    private Long id;

    /**
     * 支付单号
     */
    private String paymentNo;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式: 1-支付宝, 2-微信, 3-银联, 4-余额
     */
    private Integer paymentMethod;

    /**
     * 支付状态: 0-待支付, 1-支付中, 2-支付成功, 3-支付失败, 4-已取消, 5-已退款
     */
    private Integer paymentStatus;

    /**
     * 第三方支付平台交易号
     */
    private String transactionId;

    /**
     * 支付成功时间
     */
    private LocalDateTime paymentTime;

    /**
     * 支付失败原因
     */
    private String failureReason;

    /**
     * 支付二维码（用于扫码支付）
     */
    private String qrCode;

    /**
     * 支付链接（用于H5支付）
     */
    private String paymentUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 过期时间
     */
    private LocalDateTime expiredAt;
} 