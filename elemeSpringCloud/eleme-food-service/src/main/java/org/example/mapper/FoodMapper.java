package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.entity.Food;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface FoodMapper extends BaseMapper<Food> {

    /**
     * 根据商家ID查询所有商品
     * @param businessId 商家ID
     * @return 商品列表
     */
    @Select("SELECT * FROM food WHERE business = #{businessId}")
    List<Food> findAllByBusiness(Integer businessId);

    /**
     * 根据商家ID查询上架的商品
     * @param businessId 商家ID
     * @return 上架商品列表
     */
    @Select("SELECT * FROM food WHERE business = #{businessId} AND selling = 1")
    List<Food> findOnSaleFoodsByBusiness(Integer businessId);

    /**
     * 根据商家ID查询商品数量
     * @param businessId 商家ID
     * @return 商品数量
     */
    @Select("SELECT COUNT(*) FROM food WHERE business = #{businessId}")
    Integer countFoodsByBusiness(Integer businessId);

    /**
     * 根据商家ID查询上架商品数量
     * @param businessId 商家ID
     * @return 上架商品数量
     */
    @Select("SELECT COUNT(*) FROM food WHERE business = #{businessId} AND selling = 1")
    Integer countOnSaleFoodsByBusiness(Integer businessId);

    /**
     * 根据商品名称模糊查询
     * @param name 商品名称关键词
     * @return 商品列表
     */
    @Select("SELECT * FROM food WHERE name LIKE CONCAT('%', #{name}, '%') AND selling = 1")
    List<Food> findFoodsByNameLike(String name);

    /**
     * 根据价格区间查询商品
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @return 商品列表
     */
    @Select("SELECT * FROM food WHERE red_price >= #{minPrice} AND red_price <= #{maxPrice} AND selling = 1")
    List<Food> findFoodsByPriceRange(Double minPrice, Double maxPrice);

    /**
     * 查询热门商品（按销量排序）
     * @param limit 限制数量
     * @return 热门商品列表
     */
    @Select("SELECT * FROM food WHERE selling = 1 ORDER BY CAST(amount AS UNSIGNED) DESC LIMIT #{limit}")
    List<Food> findHotFoods(Integer limit);

    /**
     * 根据商家ID查询热门商品
     * @param businessId 商家ID
     * @param limit 限制数量
     * @return 热门商品列表
     */
    @Select("SELECT * FROM food WHERE business = #{businessId} AND selling = 1 ORDER BY CAST(amount AS UNSIGNED) DESC LIMIT #{limit}")
    List<Food> findHotFoodsByBusiness(Integer businessId, Integer limit);

    /**
     * 查询特价商品（有折扣的商品）
     * @return 特价商品列表
     */
    @Select("SELECT * FROM food WHERE discount IS NOT NULL AND discount != '' AND selling = 1")
    List<Food> findDiscountFoods();

    /**
     * 根据商家ID查询特价商品
     * @param businessId 商家ID
     * @return 特价商品列表
     */
    @Select("SELECT * FROM food WHERE business = #{businessId} AND discount IS NOT NULL AND discount != '' AND selling = 1")
    List<Food> findDiscountFoodsByBusiness(Integer businessId);

    /**
     * 批量查询商品
     * @param ids 商品ID列表
     * @return 商品列表
     */
    @Select("<script>" +
            "SELECT * FROM food WHERE id IN " +
            "<foreach item='id' index='index' collection='list' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Food> findFoodsByIds(List<Integer> ids);
} 