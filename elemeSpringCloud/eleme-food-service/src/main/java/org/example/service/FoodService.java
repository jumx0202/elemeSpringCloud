package org.example.service;

import org.example.entity.Food;

import java.util.List;

public interface FoodService {

    /**
     * 根据ID查询商品
     * @param id 商品ID
     * @return 商品信息
     */
    Food getById(Integer id);

    /**
     * 根据ID列表批量查询商品
     * @param ids 商品ID列表
     * @return 商品列表
     */
    List<Food> getFoodsByIds(List<Integer> ids);

    /**
     * 根据商家ID查询所有商品
     * @param businessId 商家ID
     * @return 商品列表
     */
    List<Food> getAllFoodsByBusinessId(Integer businessId);

    /**
     * 根据商家ID查询上架的商品
     * @param businessId 商家ID
     * @return 上架商品列表
     */
    List<Food> getOnSaleFoodsByBusinessId(Integer businessId);

    /**
     * 根据商家ID查询商品数量
     * @param businessId 商家ID
     * @return 商品数量
     */
    Integer countFoodsByBusinessId(Integer businessId);

    /**
     * 根据商家ID查询上架商品数量
     * @param businessId 商家ID
     * @return 上架商品数量
     */
    Integer countOnSaleFoodsByBusinessId(Integer businessId);

    /**
     * 根据商品名称搜索商品
     * @param name 商品名称关键词
     * @return 商品列表
     */
    List<Food> searchFoodsByName(String name);

    /**
     * 根据价格区间查询商品
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @return 商品列表
     */
    List<Food> getFoodsByPriceRange(Double minPrice, Double maxPrice);

    /**
     * 查询热门商品
     * @param limit 限制数量
     * @return 热门商品列表
     */
    List<Food> getHotFoods(Integer limit);

    /**
     * 根据商家ID查询热门商品
     * @param businessId 商家ID
     * @param limit 限制数量
     * @return 热门商品列表
     */
    List<Food> getHotFoodsByBusinessId(Integer businessId, Integer limit);

    /**
     * 查询特价商品
     * @return 特价商品列表
     */
    List<Food> getDiscountFoods();

    /**
     * 根据商家ID查询特价商品
     * @param businessId 商家ID
     * @return 特价商品列表
     */
    List<Food> getDiscountFoodsByBusinessId(Integer businessId);

    /**
     * 新增商品
     * @param food 商品信息
     * @return 是否成功
     */
    boolean addFood(Food food);

    /**
     * 更新商品信息
     * @param food 商品信息
     * @return 是否成功
     */
    boolean updateFood(Food food);

    /**
     * 删除商品
     * @param id 商品ID
     * @return 是否成功
     */
    boolean deleteFood(Integer id);

    /**
     * 商品上架
     * @param id 商品ID
     * @return 是否成功
     */
    boolean onSaleFood(Integer id);

    /**
     * 商品下架
     * @param id 商品ID
     * @return 是否成功
     */
    boolean offSaleFood(Integer id);
} 