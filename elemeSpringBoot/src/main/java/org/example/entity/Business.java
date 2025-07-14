package org.example.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

@Data
@Entity
public class Business {
    @Id
    private Integer id;
    private String password;
    //商家名
    private String businessName;
    //评分
    private String rating;
    //销量
    private String sales;
    //距离、时间
    private String distance;
    //起送价格
    private String minOrder;
    //评价
    private String comment;
    //折扣、满减
    private String discounts;
    //店内显示折扣
    private String discount;
    //公告
    private String notice;
    //侧栏元素
    private String sidebarItems;
    //图片地址
    private String imgLogo;
    //配送费
    private String delivery;
    //商家类型
    private String type;
    //折扣、满减 列表，不添加到数据库
    @Transient
    private List<String> discountsList;
    //侧栏数据 列表，不添加到数据库
    @Transient
    private List<String> sidebarItemsList;
    @Transient
    private List<Food> foodList;

    public void setDiscounts(String discounts) {
        this.discounts = discounts;
        this.discountsList = Arrays.asList(discounts.split("-"));
    }

    public void setSidebarItems(String sidebarItems) {
        this.sidebarItems = sidebarItems;
        if (sidebarItems != null && !sidebarItems.trim().isEmpty()) {
            // 处理可能的空值或格式问题
            try {
                this.sidebarItemsList = Arrays.asList(sidebarItems.split("/"));  // 使用 / 作为分隔符
            } catch (Exception e) {
                this.sidebarItemsList = new ArrayList<>();  // 如果解析失败，返回空列表
            }
        } else {
            this.sidebarItemsList = new ArrayList<>();
        }
    }

    public List<String> getSidebarItemsList() {
        if (this.sidebarItemsList == null) {
            setSidebarItems(this.sidebarItems);  // 懒加载处理
        }
        return this.sidebarItemsList;
    }
}
