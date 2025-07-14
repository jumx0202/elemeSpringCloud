package org.example.feign;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.R;
import org.example.entity.Food;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class FoodClientFallback implements FoodClient {

    @Override
    public R<Food> getFoodById(Integer id) {
        log.error("调用食物服务失败，食物ID: {}", id);
        return R.error("食物服务暂时不可用");
    }

    @Override
    public R<List<Food>> getFoodsByIds(List<Integer> ids) {
        log.error("调用食物服务失败，食物ID列表: {}", ids);
        return R.error("食物服务暂时不可用");
    }

    @Override
    public R<List<Food>> getOnSaleFoodsByBusinessId(Integer businessId) {
        log.error("调用食物服务失败，商家ID: {}", businessId);
        return R.error("食物服务暂时不可用");
    }
} 