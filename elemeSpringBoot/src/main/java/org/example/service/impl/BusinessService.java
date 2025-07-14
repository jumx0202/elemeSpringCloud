package org.example.service.impl;

import jakarta.annotation.Resource;
import org.example.entity.Business;
import org.example.entity.Food;
import org.example.mapper.IBusinessMapper;
import org.example.service.IBusinessService;
import org.example.service.IFoodService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
public class BusinessService implements IBusinessService {

    @Resource
    private IBusinessMapper businessMapper;

    @Resource
    private IFoodService foodService;

    @Override
    public Business findBusinessById(Integer id) {
        Business business = businessMapper.findBusinessById(id);
        if (business != null) {
            // 获取商品列表并处理折扣信息
            List<Food> foodList = foodService.getFoodsByBusinessId(id);
            for (Food food : foodList) {
                if (food.getDiscount() != null) {
                    food.setDiscount(food.getDiscount());
                }
            }
            
            // 处理商家的折扣和侧边栏数据
            if (business.getDiscounts() != null) {
                business.setDiscounts(business.getDiscounts());
            }
            
            if (business.getSidebarItems() != null) {
                business.setSidebarItems(business.getSidebarItems());
            }
            
            // 将商品列表添加到响应中
            business.setFoodList(foodList);
        }
        return business;
    }

    @Override
    public List<Business> getAll() {
        return (List<Business>) businessMapper.findAll();
    }

    @Override
    public List<Business> getBusinessByType(String type) {
        return businessMapper.findByType(type);
    }

    @Override
    public List<Business> searchBusinesses(String keyword) {
        return businessMapper.findByBusinessNameContaining(keyword);
    }

    @Override
    public List<Business> getRecommendBusiness() {
        // 获取评分高的商家作为推荐商家
        return ((List<Business>) businessMapper.findAll()).stream()
                .filter(business -> {
                    try {
                        double rating = Double.parseDouble(business.getRating());
                        return rating >= 4.5;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .limit(10)
                .toList();
    }

    @Override
    public List<Business> getNewBusiness() {
        // 由于Business实体中没有createTime字段，这里暂时返回所有商家的前10个
        return ((List<Business>) businessMapper.findAll()).stream()
                .limit(10)
                .toList();
    }

    @Override
    public List<Business> getPopularBusiness() {
        // 根据销量排序获取热门商家
        return ((List<Business>) businessMapper.findAll()).stream()
                .sorted((b1, b2) -> {
                    try {
                        int sales1 = Integer.parseInt(b1.getSales().replaceAll("[^0-9]", ""));
                        int sales2 = Integer.parseInt(b2.getSales().replaceAll("[^0-9]", ""));
                        return sales2 - sales1;
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .limit(10)
                .toList();
    }

    @Override
    public List<Business> getBusinessByRating(Double minRating) {
        // 根据最低评分筛选商家
        return ((List<Business>) businessMapper.findAll()).stream()
                .filter(business -> {
                    try {
                        double rating = Double.parseDouble(business.getRating());
                        return rating >= minRating;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .toList();
    }

    @Override
    public List<Business> getBusinessByDistance(Double maxDistance) {
        // 根据最大配送距离筛选商家
        return ((List<Business>) businessMapper.findAll()).stream()
                .filter(business -> {
                    try {
                        double distance = Double.parseDouble(business.getDistance().replaceAll("[^0-9.]", ""));
                        return distance <= maxDistance;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .toList();
    }

    @Override
    public List<Business> getBusinessByMinPrice(Double maxMinPrice) {
        // 根据最大起送价筛选商家
        return ((List<Business>) businessMapper.findAll()).stream()
                .filter(business -> {
                    try {
                        double minPrice = Double.parseDouble(business.getMinOrder().replaceAll("[^0-9.]", ""));
                        return minPrice <= maxMinPrice;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .toList();
    }
}
