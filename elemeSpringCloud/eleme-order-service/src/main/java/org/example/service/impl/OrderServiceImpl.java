package org.example.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.OrderDetailDTO;
import org.example.dto.OrderRequestDTO;
import org.example.dto.R;
import org.example.entity.Business;
import org.example.entity.Food;
import org.example.entity.User;
import org.example.entity.UserOrder;
import org.example.feign.BusinessClient;
import org.example.feign.FoodClient;
import org.example.feign.UserClient;
import org.example.mapper.UserOrderMapper;
import org.example.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UserOrderMapper userOrderMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserClient userClient;

    @Autowired
    private BusinessClient businessClient;

    @Autowired
    private FoodClient foodClient;

    private static final String ORDER_CACHE_KEY = "order:";
    private static final String USER_ORDERS_CACHE_KEY = "user:orders:";
    private static final String BUSINESS_ORDERS_CACHE_KEY = "business:orders:";
    private static final String ORDER_DETAIL_CACHE_KEY = "order:detail:";
    private static final long CACHE_EXPIRE_TIME = 30; // 30分钟

    @Override
    @SentinelResource(value = "getById", fallback = "getByIdFallback")
    public UserOrder getById(Integer id) {
        if (id == null) {
            return null;
        }

        // 先从缓存中获取
        String cacheKey = ORDER_CACHE_KEY + id;
        UserOrder order = (UserOrder) redisTemplate.opsForValue().get(cacheKey);
        if (order != null) {
            log.debug("从缓存中获取订单信息: {}", id);
            return order;
        }

        // 从数据库查询
        order = userOrderMapper.selectById(id);
        if (order != null) {
            // 存入缓存
            redisTemplate.opsForValue().set(cacheKey, order, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            log.debug("从数据库获取订单信息并缓存: {}", id);
        }
        return order;
    }

    @Override
    @Transactional
    @SentinelResource(value = "createOrder", fallback = "createOrderFallback")
    public Integer createOrder(OrderRequestDTO orderRequestDTO) {
        if (orderRequestDTO == null || orderRequestDTO.getUserPhone() == null 
            || orderRequestDTO.getBusinessID() == null || orderRequestDTO.getOrderList() == null 
            || orderRequestDTO.getPrice() == null) {
            log.error("创建订单失败，参数不完整: {}", orderRequestDTO);
            return null;
        }

        try {
            // 1. 验证用户是否存在
            R<User> userResult = userClient.getUserByPhone(orderRequestDTO.getUserPhone());
            if (!userResult.isSuccess() || userResult.getData() == null) {
                log.error("用户不存在: {}", orderRequestDTO.getUserPhone());
                return null;
            }

            // 2. 验证商家是否存在
            R<Business> businessResult = businessClient.getBusinessById(orderRequestDTO.getBusinessID());
            if (!businessResult.isSuccess() || businessResult.getData() == null) {
                log.error("商家不存在: {}", orderRequestDTO.getBusinessID());
                return null;
            }

            // 3. 验证商品是否存在且可用
            R<List<Food>> foodResult = foodClient.getFoodsByIds(orderRequestDTO.getOrderList());
            if (!foodResult.isSuccess() || foodResult.getData() == null || foodResult.getData().isEmpty()) {
                log.error("商品不存在或不可用: {}", orderRequestDTO.getOrderList());
                return null;
            }

            // 4. 获取食物信息（用于订单详情，不进行价格验证）
            List<Food> foods = foodResult.getData();
            log.info("获取食物信息成功，食物数量: {}, 使用前端传入价格: {}", foods.size(), orderRequestDTO.getPrice());

            // 5. 创建订单
            UserOrder order = new UserOrder();
            order.setUserPhone(orderRequestDTO.getUserPhone());
            order.setBusinessId(orderRequestDTO.getBusinessID());
            order.setPrice(orderRequestDTO.getPrice());
            order.setDeliveryFee(orderRequestDTO.getDeliveryFee());
            order.setDeliveryAddress(orderRequestDTO.getDeliveryAddress());
            order.setReceiverName(orderRequestDTO.getReceiverName());
            order.setReceiverPhone(orderRequestDTO.getReceiverPhone());
            order.setRemark(orderRequestDTO.getRemark());
            order.setState(0); // 0: 未支付
            order.setCreatedAt(LocalDateTime.now());
            
            // 将订单列表转换为字符串
            String orderListStr = orderRequestDTO.getOrderList().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining("-"));
            order.setOrderList(orderListStr);

            // 保存到数据库
            int result = userOrderMapper.insert(order);
            if (result > 0) {
                // 清理相关缓存
                clearUserOrdersCache(orderRequestDTO.getUserPhone());
                clearBusinessOrdersCache(orderRequestDTO.getBusinessID());
                log.info("创建订单成功: {}", order.getId());
                return order.getId();
            }
        } catch (Exception e) {
            log.error("创建订单失败", e);
        }
        return null;
    }

    @Override
    @Transactional
    @SentinelResource(value = "payOrder", fallback = "payOrderFallback")
    public Boolean payOrder(Integer id) {
        if (id == null) {
            return false;
        }

        try {
            UserOrder order = userOrderMapper.selectById(id);
            if (order == null) {
                log.error("订单不存在: {}", id);
                return false;
            }

            if (order.getState() != 0) {
                log.error("订单状态不正确，无法支付: {}, 当前状态: {}", id, order.getState());
                return false;
            }

            // 更新订单状态为已支付
            int result = userOrderMapper.updateStateById(id, 1);
            if (result > 0) {
                // 清理相关缓存
                clearOrderCache(id);
                clearUserOrdersCache(order.getUserPhone());
                clearBusinessOrdersCache(order.getBusinessId());
                log.info("订单支付成功: {}", id);
                return true;
            }
        } catch (Exception e) {
            log.error("订单支付失败", e);
        }
        return false;
    }

    @Override
    @SentinelResource(value = "getOrdersByUserPhone", fallback = "getOrdersByUserPhoneFallback")
    public List<UserOrder> getOrdersByUserPhone(String userPhone) {
        if (userPhone == null || userPhone.trim().isEmpty()) {
            return List.of();
        }

        // 先从缓存中获取
        String cacheKey = USER_ORDERS_CACHE_KEY + userPhone;
        List<UserOrder> orders = (List<UserOrder>) redisTemplate.opsForValue().get(cacheKey);
        if (orders != null) {
            log.debug("从缓存中获取用户订单列表: {}", userPhone);
            return orders;
        }

        // 从数据库查询
        orders = userOrderMapper.findAllByUserPhone(userPhone);
        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, orders, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        log.debug("从数据库获取用户订单列表并缓存: {}", userPhone);
        return orders;
    }

    @Override
    @SentinelResource(value = "getOrderDetailsByUserPhone", fallback = "getOrderDetailsByUserPhoneFallback")
    public List<OrderDetailDTO> getOrderDetailsByUserPhone(String userPhone) {
        if (userPhone == null || userPhone.trim().isEmpty()) {
            return List.of();
        }

        try {
            // 先获取用户的所有订单
            List<UserOrder> orders = userOrderMapper.findAllByUserPhone(userPhone);
            if (orders.isEmpty()) {
                return List.of();
            }

            // 为每个订单构建详情
            List<OrderDetailDTO> orderDetails = new ArrayList<>();
            for (UserOrder order : orders) {
                try {
                    OrderDetailDTO orderDetail = new OrderDetailDTO();
                    orderDetail.setId(order.getId());
                    orderDetail.setBusinessID(order.getBusinessId());
                    orderDetail.setUserPhone(order.getUserPhone());
                    orderDetail.setOrderList(order.getOrderList());
                    orderDetail.setPrice(order.getPrice());
                    orderDetail.setState(order.getState());
                    orderDetail.setCreatedAt(order.getCreatedAt());
                    orderDetail.setPaidAt(order.getPaidAt());
                    orderDetail.setCompletedAt(order.getCompletedAt());

                    // 获取商家信息
                    R<Business> businessResult = businessClient.getBusinessById(order.getBusinessId());
                    if (businessResult.isSuccess() && businessResult.getData() != null) {
                        orderDetail.setBusiness(businessResult.getData());
                    }

                    // 获取商品信息
                    if (order.getOrderList() != null && !order.getOrderList().isEmpty()) {
                        List<Integer> foodIds = Arrays.stream(order.getOrderList().split("-"))
                                .map(Integer::parseInt)
                                .collect(Collectors.toList());
                        
                        R<List<Food>> foodResult = foodClient.getFoodsByIds(foodIds);
                        if (foodResult.isSuccess() && foodResult.getData() != null) {
                            orderDetail.setOrderItems(foodResult.getData());
                        }
                    }

                    orderDetails.add(orderDetail);
                } catch (Exception e) {
                    log.error("获取订单{}详情失败，跳过此订单", order.getId(), e);
                    // 即使某个订单详情获取失败，也不影响其他订单
                }
            }

            log.debug("获取用户订单详情列表成功: {}, 订单数量: {}", userPhone, orderDetails.size());
            return orderDetails;
        } catch (Exception e) {
            log.error("获取用户订单详情列表失败", e);
            return List.of();
        }
    }

    @Override
    @SentinelResource(value = "getOrdersByBusinessId", fallback = "getOrdersByBusinessIdFallback")
    public List<UserOrder> getOrdersByBusinessId(Integer businessId) {
        if (businessId == null) {
            return List.of();
        }

        // 先从缓存中获取
        String cacheKey = BUSINESS_ORDERS_CACHE_KEY + businessId;
        List<UserOrder> orders = (List<UserOrder>) redisTemplate.opsForValue().get(cacheKey);
        if (orders != null) {
            log.debug("从缓存中获取商家订单列表: {}", businessId);
            return orders;
        }

        // 从数据库查询
        orders = userOrderMapper.findAllByBusinessId(businessId);
        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, orders, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        log.debug("从数据库获取商家订单列表并缓存: {}", businessId);
        return orders;
    }

    @Override
    @SentinelResource(value = "getOrdersByState", fallback = "getOrdersByStateFallback")
    public List<UserOrder> getOrdersByState(Integer state) {
        if (state == null) {
            return List.of();
        }
        return userOrderMapper.findAllByState(state);
    }

    @Override
    @SentinelResource(value = "getOrdersByUserPhoneAndState", fallback = "getOrdersByUserPhoneAndStateFallback")
    public List<UserOrder> getOrdersByUserPhoneAndState(String userPhone, Integer state) {
        if (userPhone == null || userPhone.trim().isEmpty() || state == null) {
            return List.of();
        }
        return userOrderMapper.findAllByUserPhoneAndState(userPhone, state);
    }

    @Override
    @SentinelResource(value = "getOrdersByBusinessIdAndState", fallback = "getOrdersByBusinessIdAndStateFallback")
    public List<UserOrder> getOrdersByBusinessIdAndState(Integer businessId, Integer state) {
        if (businessId == null || state == null) {
            return List.of();
        }
        return userOrderMapper.findAllByBusinessIdAndState(businessId, state);
    }

    @Override
    @SentinelResource(value = "getOrderDetail", fallback = "getOrderDetailFallback")
    public OrderDetailDTO getOrderDetail(Integer id) {
        if (id == null) {
            return null;
        }

        // 先从缓存中获取
        String cacheKey = ORDER_DETAIL_CACHE_KEY + id;
        OrderDetailDTO orderDetail = (OrderDetailDTO) redisTemplate.opsForValue().get(cacheKey);
        if (orderDetail != null) {
            log.debug("从缓存中获取订单详情: {}", id);
            return orderDetail;
        }

        try {
            // 从数据库查询订单
            UserOrder order = userOrderMapper.selectById(id);
            if (order == null) {
                return null;
            }

            // 构建订单详情
            orderDetail = new OrderDetailDTO();
            orderDetail.setId(order.getId());
            orderDetail.setBusinessID(order.getBusinessId());
            orderDetail.setUserPhone(order.getUserPhone());
            orderDetail.setOrderList(order.getOrderList());
            orderDetail.setPrice(order.getPrice());
            orderDetail.setState(order.getState());
            orderDetail.setCreatedAt(order.getCreatedAt());

            // 获取商家信息
            R<Business> businessResult = businessClient.getBusinessById(order.getBusinessId());
            if (businessResult.isSuccess() && businessResult.getData() != null) {
                orderDetail.setBusiness(businessResult.getData());
            }

            // 获取商品信息
            if (order.getOrderList() != null && !order.getOrderList().isEmpty()) {
                List<Integer> foodIds = Arrays.stream(order.getOrderList().split("-"))
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
                
                R<List<Food>> foodResult = foodClient.getFoodsByIds(foodIds);
                if (foodResult.isSuccess() && foodResult.getData() != null) {
                    orderDetail.setOrderItems(foodResult.getData());
                }
            }

            // 存入缓存
            redisTemplate.opsForValue().set(cacheKey, orderDetail, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            log.debug("从数据库获取订单详情并缓存: {}", id);
            return orderDetail;
        } catch (Exception e) {
            log.error("获取订单详情失败", e);
            return null;
        }
    }

    @Override
    @Transactional
    @SentinelResource(value = "updateOrderState", fallback = "updateOrderStateFallback")
    public Boolean updateOrderState(Integer id, Integer state) {
        if (id == null || state == null) {
            return false;
        }

        try {
            UserOrder order = userOrderMapper.selectById(id);
            if (order == null) {
                log.error("订单不存在: {}", id);
                return false;
            }

            int result = userOrderMapper.updateStateById(id, state);
            if (result > 0) {
                // 清理相关缓存
                clearOrderCache(id);
                clearOrderDetailCache(id);
                clearUserOrdersCache(order.getUserPhone());
                clearBusinessOrdersCache(order.getBusinessId());
                log.info("更新订单状态成功: {}, 新状态: {}", id, state);
                return true;
            }
        } catch (Exception e) {
            log.error("更新订单状态失败", e);
        }
        return false;
    }

    @Override
    @Transactional
    @SentinelResource(value = "cancelOrder", fallback = "cancelOrderFallback")
    public Boolean cancelOrder(Integer id) {
        return updateOrderState(id, -1); // -1: 已取消
    }

    @Override
    @Transactional
    @SentinelResource(value = "confirmOrder", fallback = "confirmOrderFallback")
    public Boolean confirmOrder(Integer id) {
        return updateOrderState(id, 2); // 2: 已确认
    }

    @Override
    @Transactional
    @SentinelResource(value = "completeOrder", fallback = "completeOrderFallback")
    public Boolean completeOrder(Integer id) {
        return updateOrderState(id, 3); // 3: 已完成
    }

    @Override
    @SentinelResource(value = "countOrdersByUserPhone", fallback = "countOrdersByUserPhoneFallback")
    public Integer countOrdersByUserPhone(String userPhone) {
        if (userPhone == null || userPhone.trim().isEmpty()) {
            return 0;
        }
        return userOrderMapper.countByUserPhone(userPhone);
    }

    @Override
    @SentinelResource(value = "countOrdersByBusinessId", fallback = "countOrdersByBusinessIdFallback")
    public Integer countOrdersByBusinessId(Integer businessId) {
        if (businessId == null) {
            return 0;
        }
        return userOrderMapper.countByBusinessId(businessId);
    }

    @Override
    @SentinelResource(value = "countOrdersByState", fallback = "countOrdersByStateFallback")
    public Integer countOrdersByState(Integer state) {
        if (state == null) {
            return 0;
        }
        return userOrderMapper.countByState(state);
    }

    @Override
    @SentinelResource(value = "getOrdersByUserPhoneAndTimeRange", fallback = "getOrdersByUserPhoneAndTimeRangeFallback")
    public List<UserOrder> getOrdersByUserPhoneAndTimeRange(String userPhone, String startTime, String endTime) {
        if (userPhone == null || userPhone.trim().isEmpty() || startTime == null || endTime == null) {
            return List.of();
        }
        return userOrderMapper.findOrdersByUserPhoneAndTimeRange(userPhone, startTime, endTime);
    }

    @Override
    @SentinelResource(value = "getOrdersByBusinessIdAndTimeRange", fallback = "getOrdersByBusinessIdAndTimeRangeFallback")
    public List<UserOrder> getOrdersByBusinessIdAndTimeRange(Integer businessId, String startTime, String endTime) {
        if (businessId == null || startTime == null || endTime == null) {
            return List.of();
        }
        return userOrderMapper.findOrdersByBusinessIdAndTimeRange(businessId, startTime, endTime);
    }

    @Override
    @SentinelResource(value = "getOrdersByPriceRange", fallback = "getOrdersByPriceRangeFallback")
    public List<UserOrder> getOrdersByPriceRange(Double minPrice, Double maxPrice) {
        if (minPrice == null || maxPrice == null || minPrice > maxPrice) {
            return List.of();
        }
        return userOrderMapper.findOrdersByPriceRange(minPrice, maxPrice);
    }

    @Override
    @SentinelResource(value = "getRecentOrders", fallback = "getRecentOrdersFallback")
    public List<UserOrder> getRecentOrders(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return userOrderMapper.findRecentOrders(limit);
    }

    @Override
    @Transactional
    @SentinelResource(value = "deleteOrder", fallback = "deleteOrderFallback")
    public Boolean deleteOrder(Integer id) {
        if (id == null) {
            return false;
        }

        try {
            UserOrder order = userOrderMapper.selectById(id);
            if (order == null) {
                log.error("订单不存在: {}", id);
                return false;
            }

            // 只允许取消状态的订单被删除
            if (order.getState() != -1) {
                log.error("订单状态不允许删除: {}, 当前状态: {}", id, order.getState());
                return false;
            }

            int result = userOrderMapper.deleteById(id);
            if (result > 0) {
                // 清理相关缓存
                clearOrderCache(id);
                clearOrderDetailCache(id);
                clearUserOrdersCache(order.getUserPhone());
                clearBusinessOrdersCache(order.getBusinessId());
                log.info("删除订单成功: {}", id);
                return true;
            }
        } catch (Exception e) {
            log.error("删除订单失败", e);
        }
        return false;
    }

    /**
     * 清理订单缓存
     */
    private void clearOrderCache(Integer id) {
        if (id != null) {
            redisTemplate.delete(ORDER_CACHE_KEY + id);
        }
    }

    /**
     * 清理订单详情缓存
     */
    private void clearOrderDetailCache(Integer id) {
        if (id != null) {
            redisTemplate.delete(ORDER_DETAIL_CACHE_KEY + id);
        }
    }

    /**
     * 清理用户订单缓存
     */
    private void clearUserOrdersCache(String userPhone) {
        if (userPhone != null) {
            redisTemplate.delete(USER_ORDERS_CACHE_KEY + userPhone);
        }
    }

    /**
     * 清理商家订单缓存
     */
    private void clearBusinessOrdersCache(Integer businessId) {
        if (businessId != null) {
            redisTemplate.delete(BUSINESS_ORDERS_CACHE_KEY + businessId);
        }
    }

    // Sentinel 降级方法
    public UserOrder getByIdFallback(Integer id, Throwable throwable) {
        log.error("获取订单信息降级处理: {}", id, throwable);
        return null;
    }

    public Integer createOrderFallback(OrderRequestDTO orderRequestDTO, Throwable throwable) {
        log.error("创建订单降级处理: {}", orderRequestDTO, throwable);
        return null;
    }

    public Boolean payOrderFallback(Integer id, Throwable throwable) {
        log.error("订单支付降级处理: {}", id, throwable);
        return false;
    }

    public List<UserOrder> getOrdersByUserPhoneFallback(String userPhone, Throwable throwable) {
        log.error("获取用户订单列表降级处理: {}", userPhone, throwable);
        return List.of();
    }

    public List<OrderDetailDTO> getOrderDetailsByUserPhoneFallback(String userPhone, Throwable throwable) {
        log.error("获取用户订单详情列表降级处理: {}", userPhone, throwable);
        return List.of();
    }

    public List<UserOrder> getOrdersByBusinessIdFallback(Integer businessId, Throwable throwable) {
        log.error("获取商家订单列表降级处理: {}", businessId, throwable);
        return List.of();
    }

    public List<UserOrder> getOrdersByStateFallback(Integer state, Throwable throwable) {
        log.error("获取订单状态列表降级处理: {}", state, throwable);
        return List.of();
    }

    public List<UserOrder> getOrdersByUserPhoneAndStateFallback(String userPhone, Integer state, Throwable throwable) {
        log.error("获取用户订单状态列表降级处理: {}-{}", userPhone, state, throwable);
        return List.of();
    }

    public List<UserOrder> getOrdersByBusinessIdAndStateFallback(Integer businessId, Integer state, Throwable throwable) {
        log.error("获取商家订单状态列表降级处理: {}-{}", businessId, state, throwable);
        return List.of();
    }

    public OrderDetailDTO getOrderDetailFallback(Integer id, Throwable throwable) {
        log.error("获取订单详情降级处理: {}", id, throwable);
        return null;
    }

    public Boolean updateOrderStateFallback(Integer id, Integer state, Throwable throwable) {
        log.error("更新订单状态降级处理: {}-{}", id, state, throwable);
        return false;
    }

    public Boolean cancelOrderFallback(Integer id, Throwable throwable) {
        log.error("取消订单降级处理: {}", id, throwable);
        return false;
    }

    public Boolean confirmOrderFallback(Integer id, Throwable throwable) {
        log.error("确认订单降级处理: {}", id, throwable);
        return false;
    }

    public Boolean completeOrderFallback(Integer id, Throwable throwable) {
        log.error("完成订单降级处理: {}", id, throwable);
        return false;
    }

    public Integer countOrdersByUserPhoneFallback(String userPhone, Throwable throwable) {
        log.error("统计用户订单数量降级处理: {}", userPhone, throwable);
        return 0;
    }

    public Integer countOrdersByBusinessIdFallback(Integer businessId, Throwable throwable) {
        log.error("统计商家订单数量降级处理: {}", businessId, throwable);
        return 0;
    }

    public Integer countOrdersByStateFallback(Integer state, Throwable throwable) {
        log.error("统计订单状态数量降级处理: {}", state, throwable);
        return 0;
    }

    public List<UserOrder> getOrdersByUserPhoneAndTimeRangeFallback(String userPhone, String startTime, String endTime, Throwable throwable) {
        log.error("获取用户时间范围订单降级处理: {}-{}-{}", userPhone, startTime, endTime, throwable);
        return List.of();
    }

    public List<UserOrder> getOrdersByBusinessIdAndTimeRangeFallback(Integer businessId, String startTime, String endTime, Throwable throwable) {
        log.error("获取商家时间范围订单降级处理: {}-{}-{}", businessId, startTime, endTime, throwable);
        return List.of();
    }

    public List<UserOrder> getOrdersByPriceRangeFallback(Double minPrice, Double maxPrice, Throwable throwable) {
        log.error("获取价格范围订单降级处理: {}-{}", minPrice, maxPrice, throwable);
        return List.of();
    }

    public List<UserOrder> getRecentOrdersFallback(Integer limit, Throwable throwable) {
        log.error("获取最近订单降级处理: {}", limit, throwable);
        return List.of();
    }

    public Boolean deleteOrderFallback(Integer id, Throwable throwable) {
        log.error("删除订单降级处理: {}", id, throwable);
        return false;
    }
} 