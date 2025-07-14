package org.example.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Food;
import org.example.mapper.FoodMapper;
import org.example.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FoodServiceImpl implements FoodService {

    @Autowired
    private FoodMapper foodMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String FOOD_CACHE_KEY = "food:";
    private static final String BUSINESS_FOODS_CACHE_KEY = "business:foods:";
    private static final String HOT_FOODS_CACHE_KEY = "hot:foods";
    private static final String DISCOUNT_FOODS_CACHE_KEY = "discount:foods";
    private static final long CACHE_EXPIRE_TIME = 30; // 30分钟

    @Override
    @SentinelResource(value = "getById", fallback = "getByIdFallback")
    public Food getById(Integer id) {
        if (id == null) {
            return null;
        }

        // 先从缓存中获取
        String cacheKey = FOOD_CACHE_KEY + id;
        Food food = (Food) redisTemplate.opsForValue().get(cacheKey);
        if (food != null) {
            log.debug("从缓存中获取商品信息: {}", id);
            return food;
        }

        // 从数据库查询
        food = foodMapper.selectById(id);
        if (food != null) {
            // 处理折扣信息
            processDiscountList(food);
            // 存入缓存
            redisTemplate.opsForValue().set(cacheKey, food, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            log.debug("从数据库获取商品信息并缓存: {}", id);
        }
        return food;
    }

    @Override
    @SentinelResource(value = "getFoodsByIds", fallback = "getFoodsByIdsFallback")
    public List<Food> getFoodsByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<Food> foods = foodMapper.findFoodsByIds(ids);
        // 处理折扣信息
        foods.forEach(this::processDiscountList);
        return foods;
    }

    @Override
    @SentinelResource(value = "getAllFoodsByBusinessId", fallback = "getAllFoodsByBusinessIdFallback")
    public List<Food> getAllFoodsByBusinessId(Integer businessId) {
        if (businessId == null) {
            return List.of();
        }

        // 先从缓存中获取
        String cacheKey = BUSINESS_FOODS_CACHE_KEY + businessId + ":all";
        List<Food> foods = (List<Food>) redisTemplate.opsForValue().get(cacheKey);
        if (foods != null) {
            log.debug("从缓存中获取商家商品列表: {}", businessId);
            return foods;
        }

        // 从数据库查询
        foods = foodMapper.findAllByBusiness(businessId);
        // 处理折扣信息
        foods.forEach(this::processDiscountList);
        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, foods, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        log.debug("从数据库获取商家商品列表并缓存: {}", businessId);
        return foods;
    }

    @Override
    @SentinelResource(value = "getOnSaleFoodsByBusinessId", fallback = "getOnSaleFoodsByBusinessIdFallback")
    public List<Food> getOnSaleFoodsByBusinessId(Integer businessId) {
        if (businessId == null) {
            return List.of();
        }

        // 先从缓存中获取
        String cacheKey = BUSINESS_FOODS_CACHE_KEY + businessId + ":onsale";
        List<Food> foods = (List<Food>) redisTemplate.opsForValue().get(cacheKey);
        if (foods != null) {
            log.debug("从缓存中获取商家上架商品列表: {}", businessId);
            return foods;
        }

        // 从数据库查询
        foods = foodMapper.findOnSaleFoodsByBusiness(businessId);
        // 处理折扣信息
        foods.forEach(this::processDiscountList);
        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, foods, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        log.debug("从数据库获取商家上架商品列表并缓存: {}", businessId);
        return foods;
    }

    @Override
    @SentinelResource(value = "countFoodsByBusinessId", fallback = "countFoodsByBusinessIdFallback")
    public Integer countFoodsByBusinessId(Integer businessId) {
        if (businessId == null) {
            return 0;
        }
        return foodMapper.countFoodsByBusiness(businessId);
    }

    @Override
    @SentinelResource(value = "countOnSaleFoodsByBusinessId", fallback = "countOnSaleFoodsByBusinessIdFallback")
    public Integer countOnSaleFoodsByBusinessId(Integer businessId) {
        if (businessId == null) {
            return 0;
        }
        return foodMapper.countOnSaleFoodsByBusiness(businessId);
    }

    @Override
    @SentinelResource(value = "searchFoodsByName", fallback = "searchFoodsByNameFallback")
    public List<Food> searchFoodsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }
        List<Food> foods = foodMapper.findFoodsByNameLike(name.trim());
        foods.forEach(this::processDiscountList);
        return foods;
    }

    @Override
    @SentinelResource(value = "getFoodsByPriceRange", fallback = "getFoodsByPriceRangeFallback")
    public List<Food> getFoodsByPriceRange(Double minPrice, Double maxPrice) {
        if (minPrice == null || maxPrice == null || minPrice > maxPrice) {
            return List.of();
        }
        List<Food> foods = foodMapper.findFoodsByPriceRange(minPrice, maxPrice);
        foods.forEach(this::processDiscountList);
        return foods;
    }

    @Override
    @SentinelResource(value = "getHotFoods", fallback = "getHotFoodsFallback")
    public List<Food> getHotFoods(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // 先从缓存中获取
        String cacheKey = HOT_FOODS_CACHE_KEY + ":" + limit;
        List<Food> foods = (List<Food>) redisTemplate.opsForValue().get(cacheKey);
        if (foods != null) {
            log.debug("从缓存中获取热门商品列表");
            return foods;
        }

        // 从数据库查询
        foods = foodMapper.findHotFoods(limit);
        foods.forEach(this::processDiscountList);
        // 存入缓存，热门商品缓存时间稍短
        redisTemplate.opsForValue().set(cacheKey, foods, 10, TimeUnit.MINUTES);
        log.debug("从数据库获取热门商品列表并缓存");
        return foods;
    }

    @Override
    @SentinelResource(value = "getHotFoodsByBusinessId", fallback = "getHotFoodsByBusinessIdFallback")
    public List<Food> getHotFoodsByBusinessId(Integer businessId, Integer limit) {
        if (businessId == null || limit == null || limit <= 0) {
            return List.of();
        }
        List<Food> foods = foodMapper.findHotFoodsByBusiness(businessId, limit);
        foods.forEach(this::processDiscountList);
        return foods;
    }

    @Override
    @SentinelResource(value = "getDiscountFoods", fallback = "getDiscountFoodsFallback")
    public List<Food> getDiscountFoods() {
        // 先从缓存中获取
        List<Food> foods = (List<Food>) redisTemplate.opsForValue().get(DISCOUNT_FOODS_CACHE_KEY);
        if (foods != null) {
            log.debug("从缓存中获取特价商品列表");
            return foods;
        }

        // 从数据库查询
        foods = foodMapper.findDiscountFoods();
        foods.forEach(this::processDiscountList);
        // 存入缓存
        redisTemplate.opsForValue().set(DISCOUNT_FOODS_CACHE_KEY, foods, 15, TimeUnit.MINUTES);
        log.debug("从数据库获取特价商品列表并缓存");
        return foods;
    }

    @Override
    @SentinelResource(value = "getDiscountFoodsByBusinessId", fallback = "getDiscountFoodsByBusinessIdFallback")
    public List<Food> getDiscountFoodsByBusinessId(Integer businessId) {
        if (businessId == null) {
            return List.of();
        }
        List<Food> foods = foodMapper.findDiscountFoodsByBusiness(businessId);
        foods.forEach(this::processDiscountList);
        return foods;
    }

    @Override
    @Transactional
    @SentinelResource(value = "addFood", fallback = "addFoodFallback")
    public boolean addFood(Food food) {
        if (food == null) {
            return false;
        }
        try {
            int result = foodMapper.insert(food);
            if (result > 0) {
                // 清理相关缓存
                clearBusinessFoodsCache(food.getBusiness());
                clearHotFoodsCache();
                clearDiscountFoodsCache();
                log.info("新增商品成功: {}", food.getId());
                return true;
            }
        } catch (Exception e) {
            log.error("新增商品失败", e);
        }
        return false;
    }

    @Override
    @Transactional
    @SentinelResource(value = "updateFood", fallback = "updateFoodFallback")
    public boolean updateFood(Food food) {
        if (food == null || food.getId() == null) {
            return false;
        }
        try {
            int result = foodMapper.updateById(food);
            if (result > 0) {
                // 清理相关缓存
                clearFoodCache(food.getId());
                clearBusinessFoodsCache(food.getBusiness());
                clearHotFoodsCache();
                clearDiscountFoodsCache();
                log.info("更新商品成功: {}", food.getId());
                return true;
            }
        } catch (Exception e) {
            log.error("更新商品失败", e);
        }
        return false;
    }

    @Override
    @Transactional
    @SentinelResource(value = "deleteFood", fallback = "deleteFoodFallback")
    public boolean deleteFood(Integer id) {
        if (id == null) {
            return false;
        }
        try {
            Food food = foodMapper.selectById(id);
            if (food != null) {
                int result = foodMapper.deleteById(id);
                if (result > 0) {
                    // 清理相关缓存
                    clearFoodCache(id);
                    clearBusinessFoodsCache(food.getBusiness());
                    clearHotFoodsCache();
                    clearDiscountFoodsCache();
                    log.info("删除商品成功: {}", id);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("删除商品失败", e);
        }
        return false;
    }

    @Override
    @Transactional
    @SentinelResource(value = "onSaleFood", fallback = "onSaleFoodFallback")
    public boolean onSaleFood(Integer id) {
        if (id == null) {
            return false;
        }
        try {
            Food food = new Food();
            food.setId(id);
            food.setSelling(1);
            int result = foodMapper.updateById(food);
            if (result > 0) {
                // 清理相关缓存
                Food existingFood = foodMapper.selectById(id);
                if (existingFood != null) {
                    clearFoodCache(id);
                    clearBusinessFoodsCache(existingFood.getBusiness());
                    clearHotFoodsCache();
                    clearDiscountFoodsCache();
                }
                log.info("商品上架成功: {}", id);
                return true;
            }
        } catch (Exception e) {
            log.error("商品上架失败", e);
        }
        return false;
    }

    @Override
    @Transactional
    @SentinelResource(value = "offSaleFood", fallback = "offSaleFoodFallback")
    public boolean offSaleFood(Integer id) {
        if (id == null) {
            return false;
        }
        try {
            Food food = new Food();
            food.setId(id);
            food.setSelling(0);
            int result = foodMapper.updateById(food);
            if (result > 0) {
                // 清理相关缓存
                Food existingFood = foodMapper.selectById(id);
                if (existingFood != null) {
                    clearFoodCache(id);
                    clearBusinessFoodsCache(existingFood.getBusiness());
                    clearHotFoodsCache();
                    clearDiscountFoodsCache();
                }
                log.info("商品下架成功: {}", id);
                return true;
            }
        } catch (Exception e) {
            log.error("商品下架失败", e);
        }
        return false;
    }

    /**
     * 处理折扣信息
     */
    private void processDiscountList(Food food) {
        if (food != null && food.getDiscount() != null) {
            food.setDiscount(food.getDiscount());
        }
    }

    /**
     * 清理商品缓存
     */
    private void clearFoodCache(Integer id) {
        if (id != null) {
            redisTemplate.delete(FOOD_CACHE_KEY + id);
        }
    }

    /**
     * 清理商家商品缓存
     */
    private void clearBusinessFoodsCache(Integer businessId) {
        if (businessId != null) {
            redisTemplate.delete(BUSINESS_FOODS_CACHE_KEY + businessId + ":all");
            redisTemplate.delete(BUSINESS_FOODS_CACHE_KEY + businessId + ":onsale");
        }
    }

    /**
     * 清理热门商品缓存
     */
    private void clearHotFoodsCache() {
        redisTemplate.delete(HOT_FOODS_CACHE_KEY + ":*");
    }

    /**
     * 清理特价商品缓存
     */
    private void clearDiscountFoodsCache() {
        redisTemplate.delete(DISCOUNT_FOODS_CACHE_KEY);
    }

    // Sentinel 降级方法
    public Food getByIdFallback(Integer id, Throwable throwable) {
        log.error("获取商品信息降级处理: {}", id, throwable);
        return null;
    }

    public List<Food> getFoodsByIdsFallback(List<Integer> ids, Throwable throwable) {
        log.error("批量获取商品信息降级处理: {}", ids, throwable);
        return List.of();
    }

    public List<Food> getAllFoodsByBusinessIdFallback(Integer businessId, Throwable throwable) {
        log.error("获取商家商品列表降级处理: {}", businessId, throwable);
        return List.of();
    }

    public List<Food> getOnSaleFoodsByBusinessIdFallback(Integer businessId, Throwable throwable) {
        log.error("获取商家上架商品列表降级处理: {}", businessId, throwable);
        return List.of();
    }

    public Integer countFoodsByBusinessIdFallback(Integer businessId, Throwable throwable) {
        log.error("统计商家商品数量降级处理: {}", businessId, throwable);
        return 0;
    }

    public Integer countOnSaleFoodsByBusinessIdFallback(Integer businessId, Throwable throwable) {
        log.error("统计商家上架商品数量降级处理: {}", businessId, throwable);
        return 0;
    }

    public List<Food> searchFoodsByNameFallback(String name, Throwable throwable) {
        log.error("搜索商品降级处理: {}", name, throwable);
        return List.of();
    }

    public List<Food> getFoodsByPriceRangeFallback(Double minPrice, Double maxPrice, Throwable throwable) {
        log.error("价格区间查询商品降级处理: {}-{}", minPrice, maxPrice, throwable);
        return List.of();
    }

    public List<Food> getHotFoodsFallback(Integer limit, Throwable throwable) {
        log.error("获取热门商品降级处理: {}", limit, throwable);
        return List.of();
    }

    public List<Food> getHotFoodsByBusinessIdFallback(Integer businessId, Integer limit, Throwable throwable) {
        log.error("获取商家热门商品降级处理: {}-{}", businessId, limit, throwable);
        return List.of();
    }

    public List<Food> getDiscountFoodsFallback(Throwable throwable) {
        log.error("获取特价商品降级处理", throwable);
        return List.of();
    }

    public List<Food> getDiscountFoodsByBusinessIdFallback(Integer businessId, Throwable throwable) {
        log.error("获取商家特价商品降级处理: {}", businessId, throwable);
        return List.of();
    }

    public boolean addFoodFallback(Food food, Throwable throwable) {
        log.error("新增商品降级处理: {}", food, throwable);
        return false;
    }

    public boolean updateFoodFallback(Food food, Throwable throwable) {
        log.error("更新商品降级处理: {}", food, throwable);
        return false;
    }

    public boolean deleteFoodFallback(Integer id, Throwable throwable) {
        log.error("删除商品降级处理: {}", id, throwable);
        return false;
    }

    public boolean onSaleFoodFallback(Integer id, Throwable throwable) {
        log.error("商品上架降级处理: {}", id, throwable);
        return false;
    }

    public boolean offSaleFoodFallback(Integer id, Throwable throwable) {
        log.error("商品下架降级处理: {}", id, throwable);
        return false;
    }
} 