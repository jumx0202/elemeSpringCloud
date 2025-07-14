package org.example.feign;

import org.example.dto.R;
import org.example.entity.Food;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "eleme-food-service", fallback = FoodClientFallback.class)
public interface FoodClient {

    /**
     * 根据ID查询食物信息
     * @param id 食物ID
     * @return 食物信息
     */
    @GetMapping("/food/{id}")
    R<Food> getFoodById(@PathVariable("id") Integer id);

    /**
     * 批量查询食物信息
     * @param ids 食物ID列表
     * @return 食物信息列表
     */
    @PostMapping("/food/batch")
    R<List<Food>> getFoodsByIds(@RequestBody List<Integer> ids);

    /**
     * 根据商家ID查询上架食物
     * @param businessId 商家ID
     * @return 食物列表
     */
    @GetMapping("/food/business/{businessId}/onsale")
    R<List<Food>> getOnSaleFoodsByBusinessId(@PathVariable("businessId") Integer businessId);
} 