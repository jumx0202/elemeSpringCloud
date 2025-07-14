package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.entity.Business;

import java.util.List;

/**
 * 商家Mapper接口
 */
@Mapper
public interface BusinessMapper extends BaseMapper<Business> {

    /**
     * 根据ID查找商家
     *
     * @param id 商家ID
     * @return 商家信息
     */
    @Select("SELECT * FROM business WHERE id = #{id} AND status = 1")
    Business findBusinessById(@Param("id") Integer id);

    /**
     * 查找所有正常营业的商家
     *
     * @return 商家列表
     */
    @Select("SELECT * FROM business WHERE status = 1 ORDER BY created_at DESC")
    List<Business> findAllActiveBusiness();

    /**
     * 根据类型查找商家
     *
     * @param type 商家类型
     * @return 商家列表
     */
    @Select("SELECT * FROM business WHERE type = #{type} AND status = 1 ORDER BY rating DESC")
    List<Business> findByType(@Param("type") String type);

    /**
     * 根据商家名称模糊查询
     *
     * @param keyword 关键字
     * @return 商家列表
     */
    @Select("SELECT * FROM business WHERE business_name LIKE CONCAT('%', #{keyword}, '%') AND status = 1 ORDER BY rating DESC")
    List<Business> findByBusinessNameContaining(@Param("keyword") String keyword);

    /**
     * 根据评分范围查询商家
     *
     * @param minRating 最低评分
     * @return 商家列表
     */
    @Select("SELECT * FROM business WHERE CAST(rating AS DECIMAL(3,1)) >= #{minRating} AND status = 1 ORDER BY rating DESC")
    List<Business> findByRatingGreaterThanEqual(@Param("minRating") Double minRating);

    /**
     * 获取推荐商家（评分高且销量好）
     *
     * @param limit 限制数量
     * @return 商家列表
     */
    @Select("SELECT * FROM business WHERE status = 1 ORDER BY CAST(rating AS DECIMAL(3,1)) DESC, CAST(sales AS UNSIGNED) DESC LIMIT #{limit}")
    List<Business> findRecommendBusiness(@Param("limit") Integer limit);

    /**
     * 获取新商家（按创建时间排序）
     *
     * @param limit 限制数量
     * @return 商家列表
     */
    @Select("SELECT * FROM business WHERE status = 1 ORDER BY created_at DESC LIMIT #{limit}")
    List<Business> findNewBusiness(@Param("limit") Integer limit);

    /**
     * 获取热门商家（按销量排序）
     *
     * @param limit 限制数量
     * @return 商家列表
     */
    @Select("SELECT * FROM business WHERE status = 1 ORDER BY CAST(sales AS UNSIGNED) DESC LIMIT #{limit}")
    List<Business> findPopularBusiness(@Param("limit") Integer limit);

    /**
     * 按距离查找商家（模拟，实际应该用地理位置计算）
     *
     * @param maxDistance 最大距离（字符串形式，如"1.2km"）
     * @return 商家列表
     */
    @Select("SELECT * FROM business WHERE status = 1 ORDER BY CAST(SUBSTRING(distance, 1, LENGTH(distance)-2) AS DECIMAL(3,1)) ASC")
    List<Business> findByDistance();

    /**
     * 获取商家类型列表
     *
     * @return 类型列表
     */
    @Select("SELECT DISTINCT type FROM business WHERE type IS NOT NULL AND type != '' AND status = 1")
    List<String> findAllTypes();
} 