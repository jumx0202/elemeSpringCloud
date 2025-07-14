package org.example.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.entity.Business;
import org.example.entity.Food;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Integer id;

    /**
     * 商家ID
     */
    private Integer businessID;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 订单商品列表
     */
    private String orderList;

    /**
     * 订单总价
     */
    private Double price;

    /**
     * 订单状态
     */
    private Integer state;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime paidAt;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime completedAt;

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

    /**
     * 商家信息
     */
    private Business business;

    /**
     * 订单商品详情列表
     */
    private List<Food> orderItems;
} 