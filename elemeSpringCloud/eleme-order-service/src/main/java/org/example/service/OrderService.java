package org.example.service;

import org.example.dto.OrderDetailDTO;
import org.example.dto.OrderRequestDTO;
import org.example.entity.UserOrder;

import java.util.List;

public interface OrderService {

    /**
     * 根据ID查询订单
     * @param id 订单ID
     * @return 订单信息
     */
    UserOrder getById(Integer id);

    /**
     * 创建新订单
     * @param orderRequestDTO 订单请求DTO
     * @return 订单ID
     */
    Integer createOrder(OrderRequestDTO orderRequestDTO);

    /**
     * 订单支付
     * @param id 订单ID
     * @return 是否成功
     */
    Boolean payOrder(Integer id);

    /**
     * 根据用户手机号查询订单
     * @param userPhone 用户手机号
     * @return 订单列表
     */
    List<UserOrder> getOrdersByUserPhone(String userPhone);

    /**
     * 根据用户手机号查询订单详情列表（包含商家和食物信息）
     * @param userPhone 用户手机号
     * @return 订单详情列表
     */
    List<OrderDetailDTO> getOrderDetailsByUserPhone(String userPhone);

    /**
     * 根据商家ID查询订单
     * @param businessId 商家ID
     * @return 订单列表
     */
    List<UserOrder> getOrdersByBusinessId(Integer businessId);

    /**
     * 根据订单状态查询订单
     * @param state 订单状态
     * @return 订单列表
     */
    List<UserOrder> getOrdersByState(Integer state);

    /**
     * 根据用户手机号和状态查询订单
     * @param userPhone 用户手机号
     * @param state 订单状态
     * @return 订单列表
     */
    List<UserOrder> getOrdersByUserPhoneAndState(String userPhone, Integer state);

    /**
     * 根据商家ID和状态查询订单
     * @param businessId 商家ID
     * @param state 订单状态
     * @return 订单列表
     */
    List<UserOrder> getOrdersByBusinessIdAndState(Integer businessId, Integer state);

    /**
     * 获取订单详情
     * @param id 订单ID
     * @return 订单详情DTO
     */
    OrderDetailDTO getOrderDetail(Integer id);

    /**
     * 更新订单状态
     * @param id 订单ID
     * @param state 新状态
     * @return 是否成功
     */
    Boolean updateOrderState(Integer id, Integer state);

    /**
     * 取消订单
     * @param id 订单ID
     * @return 是否成功
     */
    Boolean cancelOrder(Integer id);

    /**
     * 确认订单
     * @param id 订单ID
     * @return 是否成功
     */
    Boolean confirmOrder(Integer id);

    /**
     * 完成订单
     * @param id 订单ID
     * @return 是否成功
     */
    Boolean completeOrder(Integer id);

    /**
     * 统计用户订单数量
     * @param userPhone 用户手机号
     * @return 订单数量
     */
    Integer countOrdersByUserPhone(String userPhone);

    /**
     * 统计商家订单数量
     * @param businessId 商家ID
     * @return 订单数量
     */
    Integer countOrdersByBusinessId(Integer businessId);

    /**
     * 统计特定状态的订单数量
     * @param state 订单状态
     * @return 订单数量
     */
    Integer countOrdersByState(Integer state);

    /**
     * 根据时间范围查询订单
     * @param userPhone 用户手机号
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 订单列表
     */
    List<UserOrder> getOrdersByUserPhoneAndTimeRange(String userPhone, String startTime, String endTime);

    /**
     * 根据商家ID和时间范围查询订单
     * @param businessId 商家ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 订单列表
     */
    List<UserOrder> getOrdersByBusinessIdAndTimeRange(Integer businessId, String startTime, String endTime);

    /**
     * 根据价格范围查询订单
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @return 订单列表
     */
    List<UserOrder> getOrdersByPriceRange(Double minPrice, Double maxPrice);

    /**
     * 查询最近订单
     * @param limit 限制数量
     * @return 订单列表
     */
    List<UserOrder> getRecentOrders(Integer limit);

    /**
     * 删除订单
     * @param id 订单ID
     * @return 是否成功
     */
    Boolean deleteOrder(Integer id);
} 