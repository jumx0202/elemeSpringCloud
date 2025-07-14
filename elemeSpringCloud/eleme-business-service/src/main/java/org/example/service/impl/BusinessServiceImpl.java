package org.example.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import lombok.extern.slf4j.Slf4j;
import org.example.constants.CommonConstants;
import org.example.dto.R;
import org.example.entity.Business;
import org.example.entity.Food;
import org.example.feign.FoodClient;
import org.example.mapper.BusinessMapper;
import org.example.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商家服务实现类
 */
@Slf4j
@Service
public class BusinessServiceImpl implements BusinessService {

    @Autowired
    private BusinessMapper businessMapper;

    @Autowired
    private FoodClient foodClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    @SentinelResource(value = "get-business-by-id", fallback = "getBusinessByIdFallback")
    public R<Business> getBusinessById(Integer id) {
        try {
            if (id == null || id <= 0) {
                return R.error(CommonConstants.ERROR_CODE, "商家ID不能为空");
            }

            // 先从缓存获取
            String cacheKey = CommonConstants.BUSINESS_CACHE_PREFIX + id;
            Business cachedBusiness = getCachedBusiness(cacheKey);
            if (cachedBusiness != null) {
                log.debug("从缓存获取商家信息: {}", id);
                return R.success(cachedBusiness);
            }

            // 从数据库获取商家信息
            Business business = businessMapper.findBusinessById(id);
            if (business == null) {
                return R.error(CommonConstants.NOT_FOUND_CODE, "商家不存在");
            }

            // 处理商家的折扣和侧边栏数据
            processBusinessData(business);

            // 获取商家的食物列表
            try {
                R<List<Food>> foodResult = foodClient.getFoodsByBusinessId(id);
                if (foodResult.isSuccess() && foodResult.getData() != null) {
                    business.setFoodList(foodResult.getData());
                } else {
                    business.setFoodList(new ArrayList<>());
                    log.warn("获取商家 {} 的食物列表失败", id);
                }
            } catch (Exception e) {
                log.error("调用食物服务失败", e);
                business.setFoodList(new ArrayList<>());
            }

            // 缓存商家信息
            cacheBusiness(cacheKey, business);

            log.info("获取商家详情成功: {}", id);
            return R.success(business);

        } catch (Exception e) {
            log.error("获取商家详情异常", e);
            return R.error("获取商家信息失败");
        }
    }

    @Override
    @SentinelResource(value = "get-all-business", fallback = "getAllBusinessFallback")
    public R<List<Business>> getAllBusiness() {
        try {
            List<Business> businessList = businessMapper.findAllActiveBusiness();
            
            // 处理每个商家的数据
            businessList.forEach(this::processBusinessData);
            
            log.info("获取所有商家成功，数量: {}", businessList.size());
            return R.success(businessList);

        } catch (Exception e) {
            log.error("获取所有商家异常", e);
            return R.error("获取商家列表失败");
        }
    }

    @Override
    @SentinelResource(value = "get-business-by-type", fallback = "getBusinessByTypeFallback")
    public R<List<Business>> getBusinessByType(String type) {
        try {
            if (StrUtil.isBlank(type)) {
                return R.error(CommonConstants.ERROR_CODE, "商家类型不能为空");
            }

            List<Business> businessList = businessMapper.findByType(type);
            
            // 处理每个商家的数据
            businessList.forEach(this::processBusinessData);
            
            log.info("根据类型获取商家成功，类型: {}, 数量: {}", type, businessList.size());
            return R.success(businessList);

        } catch (Exception e) {
            log.error("根据类型获取商家异常", e);
            return R.error("获取商家列表失败");
        }
    }

    @Override
    @SentinelResource(value = "search-business", fallback = "searchBusinessFallback")
    public R<List<Business>> searchBusiness(String keyword) {
        try {
            if (StrUtil.isBlank(keyword)) {
                return R.error(CommonConstants.ERROR_CODE, "搜索关键字不能为空");
            }

            List<Business> businessList = businessMapper.findByBusinessNameContaining(keyword);
            
            // 处理每个商家的数据
            businessList.forEach(this::processBusinessData);
            
            log.info("搜索商家成功，关键字: {}, 数量: {}", keyword, businessList.size());
            return R.success(businessList);

        } catch (Exception e) {
            log.error("搜索商家异常", e);
            return R.error("搜索商家失败");
        }
    }

    @Override
    @SentinelResource(value = "get-recommend-business", fallback = "getRecommendBusinessFallback")
    public R<List<Business>> getRecommendBusiness(Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                limit = 10; // 默认10个
            }

            List<Business> businessList = businessMapper.findRecommendBusiness(limit);
            
            // 处理每个商家的数据
            businessList.forEach(this::processBusinessData);
            
            log.info("获取推荐商家成功，数量: {}", businessList.size());
            return R.success(businessList);

        } catch (Exception e) {
            log.error("获取推荐商家异常", e);
            return R.error("获取推荐商家失败");
        }
    }

    @Override
    @SentinelResource(value = "get-new-business", fallback = "getNewBusinessFallback")
    public R<List<Business>> getNewBusiness(Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                limit = 10; // 默认10个
            }

            List<Business> businessList = businessMapper.findNewBusiness(limit);
            
            // 处理每个商家的数据
            businessList.forEach(this::processBusinessData);
            
            log.info("获取新商家成功，数量: {}", businessList.size());
            return R.success(businessList);

        } catch (Exception e) {
            log.error("获取新商家异常", e);
            return R.error("获取新商家失败");
        }
    }

    @Override
    @SentinelResource(value = "get-popular-business", fallback = "getPopularBusinessFallback")
    public R<List<Business>> getPopularBusiness(Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                limit = 10; // 默认10个
            }

            List<Business> businessList = businessMapper.findPopularBusiness(limit);
            
            // 处理每个商家的数据
            businessList.forEach(this::processBusinessData);
            
            log.info("获取热门商家成功，数量: {}", businessList.size());
            return R.success(businessList);

        } catch (Exception e) {
            log.error("获取热门商家异常", e);
            return R.error("获取热门商家失败");
        }
    }

    @Override
    @SentinelResource(value = "get-business-by-rating", fallback = "getBusinessByRatingFallback")
    public R<List<Business>> getBusinessByRating(Double minRating) {
        try {
            if (minRating == null || minRating < 0 || minRating > 5) {
                return R.error(CommonConstants.ERROR_CODE, "评分范围应在0-5之间");
            }

            List<Business> businessList = businessMapper.findByRatingGreaterThanEqual(minRating);
            
            // 处理每个商家的数据
            businessList.forEach(this::processBusinessData);
            
            log.info("根据评分获取商家成功，最低评分: {}, 数量: {}", minRating, businessList.size());
            return R.success(businessList);

        } catch (Exception e) {
            log.error("根据评分获取商家异常", e);
            return R.error("获取商家列表失败");
        }
    }

    @Override
    @SentinelResource(value = "get-business-by-distance", fallback = "getBusinessByDistanceFallback")
    public R<List<Business>> getBusinessByDistance() {
        try {
            List<Business> businessList = businessMapper.findByDistance();
            
            // 处理每个商家的数据
            businessList.forEach(this::processBusinessData);
            
            log.info("按距离获取商家成功，数量: {}", businessList.size());
            return R.success(businessList);

        } catch (Exception e) {
            log.error("按距离获取商家异常", e);
            return R.error("获取商家列表失败");
        }
    }

    @Override
    @SentinelResource(value = "get-business-types", fallback = "getAllBusinessTypesFallback")
    public R<List<String>> getAllBusinessTypes() {
        try {
            List<String> types = businessMapper.findAllTypes();
            
            log.info("获取商家类型成功，数量: {}", types.size());
            return R.success(types);

        } catch (Exception e) {
            log.error("获取商家类型异常", e);
            return R.error("获取商家类型失败");
        }
    }

    /**
     * 处理商家数据（折扣、侧边栏等）
     */
    private void processBusinessData(Business business) {
        if (business == null) return;
        
        // 处理折扣信息
        if (StrUtil.isNotBlank(business.getDiscounts())) {
            business.setDiscounts(business.getDiscounts());
        }
        
        // 处理侧边栏信息
        if (StrUtil.isNotBlank(business.getSidebarItems())) {
            business.setSidebarItems(business.getSidebarItems());
        }
    }

    /**
     * 从缓存获取商家信息
     */
    private Business getCachedBusiness(String cacheKey) {
        try {
            // 这里可以实现Redis缓存逻辑
            // 由于JSON序列化复杂，这里暂时返回null
            return null;
        } catch (Exception e) {
            log.warn("从缓存获取商家信息失败", e);
            return null;
        }
    }

    /**
     * 缓存商家信息
     */
    private void cacheBusiness(String cacheKey, Business business) {
        try {
            // 这里可以实现Redis缓存逻辑
            // 由于JSON序列化复杂，这里暂时不实现
            log.debug("缓存商家信息: {}", business.getId());
        } catch (Exception e) {
            log.warn("缓存商家信息失败", e);
        }
    }

    // Sentinel 降级方法
    public R<Business> getBusinessByIdFallback(Integer id, Throwable ex) {
        log.error("获取商家详情服务降级", ex);
        return R.error("商家服务暂时不可用");
    }

    public R<List<Business>> getAllBusinessFallback(Throwable ex) {
        log.error("获取所有商家服务降级", ex);
        return R.error("商家服务暂时不可用");
    }

    public R<List<Business>> getBusinessByTypeFallback(String type, Throwable ex) {
        log.error("根据类型获取商家服务降级", ex);
        return R.error("商家服务暂时不可用");
    }

    public R<List<Business>> searchBusinessFallback(String keyword, Throwable ex) {
        log.error("搜索商家服务降级", ex);
        return R.error("搜索服务暂时不可用");
    }

    public R<List<Business>> getRecommendBusinessFallback(Integer limit, Throwable ex) {
        log.error("获取推荐商家服务降级", ex);
        return R.error("推荐服务暂时不可用");
    }

    public R<List<Business>> getNewBusinessFallback(Integer limit, Throwable ex) {
        log.error("获取新商家服务降级", ex);
        return R.error("商家服务暂时不可用");
    }

    public R<List<Business>> getPopularBusinessFallback(Integer limit, Throwable ex) {
        log.error("获取热门商家服务降级", ex);
        return R.error("商家服务暂时不可用");
    }

    public R<List<Business>> getBusinessByRatingFallback(Double minRating, Throwable ex) {
        log.error("根据评分获取商家服务降级", ex);
        return R.error("商家服务暂时不可用");
    }

    public R<List<Business>> getBusinessByDistanceFallback(Throwable ex) {
        log.error("按距离获取商家服务降级", ex);
        return R.error("商家服务暂时不可用");
    }

    public R<List<String>> getAllBusinessTypesFallback(Throwable ex) {
        log.error("获取商家类型服务降级", ex);
        return R.error("商家服务暂时不可用");
    }
} 