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
 * 食物实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("food")
public class Food implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 食物ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 食物名称
     */
    @TableField("name")
    private String name;

    /**
     * 食物描述
     */
    @TableField("text")
    private String text;

    /**
     * 销量
     */
    @TableField("amount")
    private String amount;

    /**
     * 打折信息
     */
    @TableField("discount")
    private String discount;

    /**
     * 现价
     */
    @TableField("red_price")
    private Double redPrice;

    /**
     * 原价
     */
    @TableField("gray_price")
    private String grayPrice;

    /**
     * 所属商家ID
     */
    @TableField("business")
    private Integer business;

    /**
     * 食物图片路径
     */
    @TableField("img")
    private String img;

    /**
     * 是否上架（0-下架，1-上架）
     */
    @TableField("selling")
    private Integer selling;

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
     * 食物状态（0-禁用，1-正常）
     */
    @TableField("status")
    private Integer status;

    /**
     * 食物分类
     */
    @TableField("category")
    private String category;

    /**
     * 打折信息列表，不添加到数据库
     */
    @TableField(exist = false)
    private List<String> discountList;

    /**
     * 设置打折信息，同时解析为列表
     */
    public void setDiscount(String discount) {
        this.discount = discount;
        if (discount != null && !discount.trim().isEmpty()) {
            this.discountList = Arrays.asList(discount.split("-"));
        } else {
            this.discountList = new ArrayList<>();
        }
    }

    /**
     * 获取打折信息列表，懒加载处理
     */
    public List<String> getDiscountList() {
        if (this.discountList == null) {
            setDiscount(this.discount);
        }
        return this.discountList;
    }
} 