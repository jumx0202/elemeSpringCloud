package org.example.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
@Entity
public class Food {
    @Id
    private Integer id;
    //名称
    private String name;
    //评价
    private String text;
    //销量
    private String amount;
    //打折
    private String discount;
    //现价
    private Double redPrice;
    //原价
    private String grayPrice;
    //商家ID
    private Integer business;
    //图片路径
    private String img;
    //是否上架(0:下架 1:上架)
    private Integer selling;
    //打折 列表
    @Transient
    private List<String> discountList;

    public void setDiscount(String discounts) {
        this.discount = discounts;
        if (discounts != null && !discounts.trim().isEmpty()) {
            this.discountList = Arrays.asList(discounts.split("-"));
        }
    }
}
