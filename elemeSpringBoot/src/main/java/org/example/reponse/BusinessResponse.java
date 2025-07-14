package org.example.reponse;

import lombok.Data;
import org.example.entity.Business;
import org.example.entity.Food;

import java.util.List;

@Data
public class BusinessResponse {
    private Integer id;
    private String password;
    private String businessName;
    private String rating;
    private String sales;
    private String distance;
    private String minOrder;
    private String comment;
    private String discount;
    private String notice;
    private String sidebarItems;
    private String imgLogo;
    private String delivery;
    private List<String> discountsList;
    private List<Food> foodList;
    private List<String> sidebarItemsList;

    public BusinessResponse() {
    }

    public BusinessResponse(Integer id, String password, String businessName, String rating,
                          String sales, String distance, String minOrder, String comment,
                          String discount, String notice, String sidebarItems, String imgLogo,
                          String delivery, List<String> discountsList, List<Food> foodList,
                          List<String> sidebarItemsList) {
        this.id = id;
        this.password = password;
        this.businessName = businessName;
        this.rating = rating;
        this.sales = sales;
        this.distance = distance;
        this.minOrder = minOrder;
        this.comment = comment;
        this.discount = discount;
        this.notice = notice;
        this.sidebarItems = sidebarItems;
        this.imgLogo = imgLogo;
        this.delivery = delivery;
        this.discountsList = discountsList;
        this.foodList = foodList;
        this.sidebarItemsList = sidebarItemsList;
    }

    // 添加一个从 Business 对象构造的方法
    public static BusinessResponse fromBusiness(Business business) {
        if (business == null) {
            return null;
        }
        return new BusinessResponse(
            business.getId(),
            business.getPassword(),
            business.getBusinessName(),
            business.getRating(),
            business.getSales(),
            business.getDistance(),
            business.getMinOrder(),
            business.getComment(),
            business.getDiscount(),
            business.getNotice(),
            business.getSidebarItems(),
            business.getImgLogo(),
            business.getDelivery(),
            business.getDiscountsList(),
            null,  // foodList will be set separately if needed
            business.getSidebarItemsList()
        );
    }
}
