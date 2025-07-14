package org.example.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PaymentRequestDTO {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Integer orderId;

    /**
     * 用户手机号
     */
    @NotBlank(message = "用户手机号不能为空")
    private String userPhone;

    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal amount;

    /**
     * 支付方式: 1-支付宝, 2-微信, 3-银联, 4-余额
     */
    @NotNull(message = "支付方式不能为空")
    private Integer paymentMethod;

    /**
     * 支付描述
     */
    private String description;

    /**
     * 支付成功后的回调地址
     */
    private String returnUrl;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 设备信息
     */
    private String deviceInfo;
} 