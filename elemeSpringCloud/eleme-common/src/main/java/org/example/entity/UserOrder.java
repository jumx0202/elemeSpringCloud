package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * 用户订单实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_order")
public class UserOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商家ID
     */
    @TableField("business_id")
    private Integer businessId;

    /**
     * 用户手机号
     */
    @TableField("user_phone")
    private String userPhone;

    /**
     * 订单商品列表（用"-"分隔的商品ID）
     */
    @TableField("order_list")
    private String orderList;

    /**
     * 订单总价
     */
    @TableField("price")
    private Double price;

    /**
     * 订单状态（0-未支付，1-已支付，2-已完成，3-已取消）
     */
    @TableField("state")
    private Integer state;

    /**
     * 创建时间
     */
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updatedAt;

    /**
     * 支付时间
     */
    @TableField("paid_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime paidAt;

    /**
     * 完成时间
     */
    @TableField("completed_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime completedAt;

    /**
     * 收货地址
     */
    @TableField("delivery_address")
    private String deliveryAddress;

    /**
     * 收货人姓名
     */
    @TableField("receiver_name")
    private String receiverName;

    /**
     * 收货人电话
     */
    @TableField("receiver_phone")
    private String receiverPhone;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 配送费
     */
    @TableField("delivery_fee")
    private Double deliveryFee;

    /**
     * 订单商品ID列表，不添加到数据库
     */
    @TableField(exist = false)
    private List<Integer> orderItemIds;

    /**
     * 商家信息，不添加到数据库
     */
    @TableField(exist = false)
    private Business business;

    /**
     * 订单商品详情列表，不添加到数据库
     */
    @TableField(exist = false)
    private List<Food> orderItems;

    /**
     * 设置订单商品列表，同时解析为ID列表
     */
    public void setOrderList(String orderList) {
        this.orderList = orderList;
        if (orderList != null && !orderList.trim().isEmpty()) {
            try {
                this.orderItemIds = Arrays.stream(orderList.split("-"))
                        .map(Integer::parseInt)
                        .toList();
            } catch (NumberFormatException e) {
                this.orderItemIds = new ArrayList<>();
            }
        } else {
            this.orderItemIds = new ArrayList<>();
        }
    }

    /**
     * 获取订单商品ID列表，懒加载处理
     */
    public List<Integer> getOrderItemIds() {
        if (this.orderItemIds == null) {
            setOrderList(this.orderList);
        }
        return this.orderItemIds;
    }
} 