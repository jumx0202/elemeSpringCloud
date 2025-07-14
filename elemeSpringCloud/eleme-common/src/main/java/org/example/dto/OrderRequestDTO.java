package org.example.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 创建订单请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商家ID
     */
    private Integer businessID;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 订单商品ID列表
     */
    private List<Integer> orderList;

    /**
     * 订单总价
     */
    private Double price;

    /**
     * 收货地址
     */
    private String deliveryAddress;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 备注
     */
    private String remark;

    /**
     * 配送费
     */
    private Double deliveryFee;
} 