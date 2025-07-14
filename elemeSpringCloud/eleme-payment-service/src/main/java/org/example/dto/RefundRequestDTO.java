package org.example.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class RefundRequestDTO {

    /**
     * 支付单号
     */
    @NotBlank(message = "支付单号不能为空")
    private String paymentNo;

    /**
     * 退款金额
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    @NotBlank(message = "退款原因不能为空")
    private String refundReason;

    /**
     * 操作员
     */
    private String operator;

    /**
     * 退款单号（可选，系统自动生成）
     */
    private String refundNo;
} 