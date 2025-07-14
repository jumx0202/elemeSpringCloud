package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.entity.UserOrder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface UserOrderMapper extends BaseMapper<UserOrder> {

    /**
     * 根据用户手机号查询订单
     * @param userPhone 用户手机号
     * @return 订单列表
     */
    @Select("SELECT * FROM user_order WHERE user_phone = #{userPhone} ORDER BY created_at DESC")
    List<UserOrder> findAllByUserPhone(String userPhone);

    /**
     * 根据商家ID查询订单
     * @param businessId 商家ID
     * @return 订单列表
     */
    @Select("SELECT * FROM user_order WHERE business_id = #{businessId} ORDER BY created_at DESC")
    List<UserOrder> findAllByBusinessId(Integer businessId);

    /**
     * 根据订单状态查询订单
     * @param state 订单状态
     * @return 订单列表
     */
    @Select("SELECT * FROM user_order WHERE state = #{state} ORDER BY created_at DESC")
    List<UserOrder> findAllByState(Integer state);

    /**
     * 根据用户手机号和订单状态查询订单
     * @param userPhone 用户手机号
     * @param state 订单状态
     * @return 订单列表
     */
    @Select("SELECT * FROM user_order WHERE user_phone = #{userPhone} AND state = #{state} ORDER BY created_at DESC")
    List<UserOrder> findAllByUserPhoneAndState(String userPhone, Integer state);

    /**
     * 根据商家ID和订单状态查询订单
     * @param businessId 商家ID
     * @param state 订单状态
     * @return 订单列表
     */
    @Select("SELECT * FROM user_order WHERE business_id = #{businessId} AND state = #{state} ORDER BY created_at DESC")
    List<UserOrder> findAllByBusinessIdAndState(Integer businessId, Integer state);

    /**
     * 统计用户订单数量
     * @param userPhone 用户手机号
     * @return 订单数量
     */
    @Select("SELECT COUNT(*) FROM user_order WHERE user_phone = #{userPhone}")
    Integer countByUserPhone(String userPhone);

    /**
     * 统计商家订单数量
     * @param businessId 商家ID
     * @return 订单数量
     */
    @Select("SELECT COUNT(*) FROM user_order WHERE business_id = #{businessId}")
    Integer countByBusinessId(Integer businessId);

    /**
     * 统计特定状态的订单数量
     * @param state 订单状态
     * @return 订单数量
     */
    @Select("SELECT COUNT(*) FROM user_order WHERE state = #{state}")
    Integer countByState(Integer state);

    /**
     * 更新订单状态
     * @param id 订单ID
     * @param state 新状态
     * @return 更新行数
     */
    @Update("UPDATE user_order SET state = #{state} WHERE id = #{id}")
    int updateStateById(@Param("id") Integer id, @Param("state") Integer state);

    /**
     * 根据用户手机号和时间范围查询订单
     * @param userPhone 用户手机号
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 订单列表
     */
    @Select("SELECT * FROM user_order WHERE user_phone = #{userPhone} AND created_at BETWEEN #{startTime} AND #{endTime} ORDER BY created_at DESC")
    List<UserOrder> findOrdersByUserPhoneAndTimeRange(String userPhone, String startTime, String endTime);

    /**
     * 根据商家ID和时间范围查询订单
     * @param businessId 商家ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 订单列表
     */
    @Select("SELECT * FROM user_order WHERE business_id = #{businessId} AND created_at BETWEEN #{startTime} AND #{endTime} ORDER BY created_at DESC")
    List<UserOrder> findOrdersByBusinessIdAndTimeRange(Integer businessId, String startTime, String endTime);

    /**
     * 根据价格范围查询订单
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @return 订单列表
     */
    @Select("SELECT * FROM user_order WHERE price >= #{minPrice} AND price <= #{maxPrice} ORDER BY created_at DESC")
    List<UserOrder> findOrdersByPriceRange(Double minPrice, Double maxPrice);

    /**
     * 查询热门订单（按创建时间最近）
     * @param limit 限制数量
     * @return 订单列表
     */
    @Select("SELECT * FROM user_order ORDER BY created_at DESC LIMIT #{limit}")
    List<UserOrder> findRecentOrders(Integer limit);
} 