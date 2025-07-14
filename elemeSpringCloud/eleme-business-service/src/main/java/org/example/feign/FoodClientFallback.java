package org.example.feign;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.R;
import org.example.entity.Food;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 食物服务降级回调
 */
@Slf4j
@Component
public class FoodClientFallback implements FoodClient {

    @Override
    public R<List<Food>> getFoodsByBusinessId(Integer businessId) {
        log.warn("食物服务调用失败，执行降级逻辑，商家ID: {}", businessId);
        return R.success("食物服务暂时不可用", new ArrayList<>());
    }
} 