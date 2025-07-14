package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_payment")
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 支付单号（唯一）
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
     * 第三方支付平台用户标识
     */
    private String thirdPartyUserId;

    /**
     * 支付成功时间
     */
    private LocalDateTime paymentTime;

    /**
     * 支付失败原因
     */
    private String failureReason;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 支付平台回调信息
     */
    private String callbackData;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 过期时间
     */
    private LocalDateTime expiredAt;

    /**
     * 是否删除: 0-未删除, 1-已删除
     */
    private Integer deleted;
} 