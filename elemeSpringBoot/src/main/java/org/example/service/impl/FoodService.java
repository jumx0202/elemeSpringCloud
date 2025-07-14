package org.example.service.impl;

import jakarta.annotation.Resource;
import org.example.entity.Food;
import org.example.mapper.IFoodMapper;
import org.example.service.IFoodService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FoodService implements IFoodService {

    @Resource
    private IFoodMapper foodMapper;

    @Override
    public List<Food> getFoodsByIds(Integer[] ids) {
        List<Food> foodList = new ArrayList<>();
        for (Integer id : ids) {
            Food food = foodMapper.findFoodById(id);
            if (food != null) {
                foodList.add(food);
            }
        }
        return foodList;
    }

    @Override
    public Food getById(Integer id) {
        return foodMapper.findFoodById(id);
    }

    @Override
    public List<Food> getFoodsByBusinessId(Integer businessId) {
        // 获取商家的所有商品
        List<Food> allFoods = foodMapper.findAllByBusiness(businessId);
        // 过滤出上架的商品(selling=1)
        return allFoods.stream()
                .filter(food -> food.getSelling() != null && food.getSelling() == 1)
                .peek(food -> {
                    // 确保折扣信息被正确处理
                    if (food.getDiscount() != null) {
                        food.setDiscount(food.getDiscount());
                    }
                })
                .toList();
    }
} 