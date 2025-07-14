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
 * 商家实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("business")
public class Business implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商家ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商家登录密码
     */
    @TableField("password")
    private String password;

    /**
     * 商家名称
     */
    @TableField("business_name")
    private String businessName;

    /**
     * 商家评分
     */
    @TableField("rating")
    private String rating;

    /**
     * 商家销量
     */
    @TableField("sales")
    private String sales;

    /**
     * 距离、时间
     */
    @TableField("distance")
    private String distance;

    /**
     * 起送价格
     */
    @TableField("min_order")
    private String minOrder;

    /**
     * 评价
     */
    @TableField("comment")
    private String comment;

    /**
     * 折扣、满减
     */
    @TableField("discounts")
    private String discounts;

    /**
     * 店内显示折扣
     */
    @TableField("discount")
    private String discount;

    /**
     * 公告
     */
    @TableField("notice")
    private String notice;

    /**
     * 侧栏元素
     */
    @TableField("sidebar_items")
    private String sidebarItems;

    /**
     * 商家LOGO图片地址
     */
    @TableField("img_logo")
    private String imgLogo;

    /**
     * 配送费
     */
    @TableField("delivery")
    private String delivery;

    /**
     * 商家类型
     */
    @TableField("type")
    private String type;

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
     * 商家状态（0-禁用，1-正常）
     */
    @TableField("status")
    private Integer status;

    /**
     * 折扣、满减 列表，不添加到数据库
     */
    @TableField(exist = false)
    private List<String> discountsList;

    /**
     * 侧栏数据 列表，不添加到数据库
     */
    @TableField(exist = false)
    private List<String> sidebarItemsList;

    /**
     * 商家的食物列表，不添加到数据库
     */
    @TableField(exist = false)
    private List<Food> foodList;

    /**
     * 设置折扣信息，同时解析为列表
     */
    public void setDiscounts(String discounts) {
        this.discounts = discounts;
        if (discounts != null && !discounts.trim().isEmpty()) {
            this.discountsList = Arrays.asList(discounts.split("-"));
        } else {
            this.discountsList = new ArrayList<>();
        }
    }

    /**
     * 设置侧栏元素，同时解析为列表
     */
    public void setSidebarItems(String sidebarItems) {
        this.sidebarItems = sidebarItems;
        if (sidebarItems != null && !sidebarItems.trim().isEmpty()) {
            try {
                this.sidebarItemsList = Arrays.asList(sidebarItems.split("/"));
            } catch (Exception e) {
                this.sidebarItemsList = new ArrayList<>();
            }
        } else {
            this.sidebarItemsList = new ArrayList<>();
        }
    }

    /**
     * 获取侧栏元素列表，懒加载处理
     */
    public List<String> getSidebarItemsList() {
        if (this.sidebarItemsList == null) {
            setSidebarItems(this.sidebarItems);
        }
        return this.sidebarItemsList;
    }

    /**
     * 获取折扣列表，懒加载处理
     */
    public List<String> getDiscountsList() {
        if (this.discountsList == null) {
            setDiscounts(this.discounts);
        }
        return this.discountsList;
    }
} 