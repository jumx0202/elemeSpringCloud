package org.example.feign;

import org.example.dto.R;
import org.example.entity.Food;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 食物服务Feign客户端
 */
@FeignClient(name = "eleme-food-service", fallback = FoodClientFallback.class, configuration = org.example.config.FeignConfig.class)
public interface FoodClient {

    /**
     * 根据商家ID获取食物列表
     *
     * @param businessId 商家ID
     * @return 食物列表
     */
    @GetMapping("/food/business/{businessId}/onsale")
    R<List<Food>> getFoodsByBusinessId(@PathVariable("businessId") Integer businessId);
} 