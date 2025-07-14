package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.OrderDetailDTO;
import org.example.dto.OrderRequestDTO;
import org.example.dto.R;
import org.example.entity.UserOrder;
import org.example.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
@Tag(name = "订单服务", description = "订单管理相关接口")
@Slf4j
@Validated
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private org.example.feign.BusinessClient businessClient;

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询订单", description = "根据订单ID查询订单详细信息")
    public R<UserOrder> getById(
            @Parameter(description = "订单ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer id) {
        
        log.info("查询订单信息: {}", id);
        UserOrder order = orderService.getById(id);
        if (order != null) {
            return R.success(order);
        } else {
            return R.error("订单不存在");
        }
    }

    @PostMapping
    @Operation(summary = "创建订单", description = "创建新的订单")
    public R<Integer> createOrder(
            @Parameter(description = "订单请求信息", required = true) 
            @RequestBody @Valid OrderRequestDTO orderRequestDTO) {
        
        log.info("创建订单: {}", orderRequestDTO);
        Integer orderId = orderService.createOrder(orderRequestDTO);
        if (orderId != null) {
            return R.success("订单创建成功", orderId);
        } else {
            return R.error("订单创建失败");
        }
    }

    @PutMapping("/{id}/pay")
    @Operation(summary = "订单支付", description = "处理订单支付")
    public R<String> payOrder(
            @Parameter(description = "订单ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer id) {
        
        log.info("订单支付: {}", id);
        Boolean success = orderService.payOrder(id);
        if (success) {
            return R.success("订单支付成功");
        } else {
            return R.error("订单支付失败");
        }
    }

    @GetMapping("/user/{userPhone}")
    @Operation(summary = "查询用户订单", description = "根据用户手机号查询订单列表")
    public R<List<UserOrder>> getOrdersByUserPhone(
            @Parameter(description = "用户手机号", required = true) 
            @PathVariable @NotBlank String userPhone) {
        
        log.info("查询用户订单: {}", userPhone);
        List<UserOrder> orders = orderService.getOrdersByUserPhone(userPhone);
        return R.success(orders);
    }

    @GetMapping("/business/{businessId}")
    @Operation(summary = "查询商家订单", description = "根据商家ID查询订单列表")
    public R<List<UserOrder>> getOrdersByBusinessId(
            @Parameter(description = "商家ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer businessId) {
        
        log.info("查询商家订单: {}", businessId);
        List<UserOrder> orders = orderService.getOrdersByBusinessId(businessId);
        return R.success(orders);
    }

    @GetMapping("/state/{state}")
    @Operation(summary = "按状态查询订单", description = "根据订单状态查询订单列表")
    public R<List<UserOrder>> getOrdersByState(
            @Parameter(description = "订单状态", required = true) 
            @PathVariable @NotNull Integer state) {
        
        log.info("按状态查询订单: {}", state);
        List<UserOrder> orders = orderService.getOrdersByState(state);
        return R.success(orders);
    }

    @GetMapping("/user/{userPhone}/state/{state}")
    @Operation(summary = "查询用户特定状态订单", description = "根据用户手机号和状态查询订单列表")
    public R<List<UserOrder>> getOrdersByUserPhoneAndState(
            @Parameter(description = "用户手机号", required = true) 
            @PathVariable @NotBlank String userPhone,
            @Parameter(description = "订单状态", required = true) 
            @PathVariable @NotNull Integer state) {
        
        log.info("查询用户特定状态订单: {}-{}", userPhone, state);
        List<UserOrder> orders = orderService.getOrdersByUserPhoneAndState(userPhone, state);
        return R.success(orders);
    }

    @GetMapping("/business/{businessId}/state/{state}")
    @Operation(summary = "查询商家特定状态订单", description = "根据商家ID和状态查询订单列表")
    public R<List<UserOrder>> getOrdersByBusinessIdAndState(
            @Parameter(description = "商家ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer businessId,
            @Parameter(description = "订单状态", required = true) 
            @PathVariable @NotNull Integer state) {
        
        log.info("查询商家特定状态订单: {}-{}", businessId, state);
        List<UserOrder> orders = orderService.getOrdersByBusinessIdAndState(businessId, state);
        return R.success(orders);
    }

    @GetMapping("/{id}/detail")
    @Operation(summary = "查询订单详情", description = "根据订单ID查询详细信息，包括商家和商品信息")
    public R<OrderDetailDTO> getOrderDetail(
            @Parameter(description = "订单ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer id) {
        
        log.info("查询订单详情: {}", id);
        OrderDetailDTO orderDetail = orderService.getOrderDetail(id);
        if (orderDetail != null) {
            return R.success(orderDetail);
        } else {
            return R.error("订单详情不存在");
        }
    }

    @PutMapping("/{id}/state/{state}")
    @Operation(summary = "更新订单状态", description = "更新指定订单的状态")
    public R<String> updateOrderState(
            @Parameter(description = "订单ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer id,
            @Parameter(description = "新状态", required = true) 
            @PathVariable @NotNull Integer state) {
        
        log.info("更新订单状态: {}-{}", id, state);
        Boolean success = orderService.updateOrderState(id, state);
        if (success) {
            return R.success("订单状态更新成功");
        } else {
            return R.error("订单状态更新失败");
        }
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "取消指定订单")
    public R<String> cancelOrder(
            @Parameter(description = "订单ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer id) {
        
        log.info("取消订单: {}", id);
        Boolean success = orderService.cancelOrder(id);
        if (success) {
            return R.success("订单取消成功");
        } else {
            return R.error("订单取消失败");
        }
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认订单", description = "确认指定订单")
    public R<String> confirmOrder(
            @Parameter(description = "订单ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer id) {
        
        log.info("确认订单: {}", id);
        Boolean success = orderService.confirmOrder(id);
        if (success) {
            return R.success("订单确认成功");
        } else {
            return R.error("订单确认失败");
        }
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "完成订单", description = "完成指定订单")
    public R<String> completeOrder(
            @Parameter(description = "订单ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer id) {
        
        log.info("完成订单: {}", id);
        Boolean success = orderService.completeOrder(id);
        if (success) {
            return R.success("订单完成成功");
        } else {
            return R.error("订单完成失败");
        }
    }

    @GetMapping("/user/{userPhone}/count")
    @Operation(summary = "统计用户订单数量", description = "统计指定用户的订单总数")
    public R<Integer> countOrdersByUserPhone(
            @Parameter(description = "用户手机号", required = true) 
            @PathVariable @NotBlank String userPhone) {
        
        log.info("统计用户订单数量: {}", userPhone);
        Integer count = orderService.countOrdersByUserPhone(userPhone);
        return R.success(count);
    }

    @GetMapping("/business/{businessId}/count")
    @Operation(summary = "统计商家订单数量", description = "统计指定商家的订单总数")
    public R<Integer> countOrdersByBusinessId(
            @Parameter(description = "商家ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer businessId) {
        
        log.info("统计商家订单数量: {}", businessId);
        Integer count = orderService.countOrdersByBusinessId(businessId);
        return R.success(count);
    }

    @GetMapping("/state/{state}/count")
    @Operation(summary = "统计特定状态订单数量", description = "统计指定状态的订单总数")
    public R<Integer> countOrdersByState(
            @Parameter(description = "订单状态", required = true) 
            @PathVariable @NotNull Integer state) {
        
        log.info("统计特定状态订单数量: {}", state);
        Integer count = orderService.countOrdersByState(state);
        return R.success(count);
    }

    @GetMapping("/user/{userPhone}/time-range")
    @Operation(summary = "按时间范围查询用户订单", description = "根据用户手机号和时间范围查询订单")
    public R<List<UserOrder>> getOrdersByUserPhoneAndTimeRange(
            @Parameter(description = "用户手机号", required = true) 
            @PathVariable @NotBlank String userPhone,
            @Parameter(description = "开始时间", required = true) 
            @RequestParam @NotBlank String startTime,
            @Parameter(description = "结束时间", required = true) 
            @RequestParam @NotBlank String endTime) {
        
        log.info("按时间范围查询用户订单: {}-{}-{}", userPhone, startTime, endTime);
        List<UserOrder> orders = orderService.getOrdersByUserPhoneAndTimeRange(userPhone, startTime, endTime);
        return R.success(orders);
    }

    @GetMapping("/business/{businessId}/time-range")
    @Operation(summary = "按时间范围查询商家订单", description = "根据商家ID和时间范围查询订单")
    public R<List<UserOrder>> getOrdersByBusinessIdAndTimeRange(
            @Parameter(description = "商家ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer businessId,
            @Parameter(description = "开始时间", required = true) 
            @RequestParam @NotBlank String startTime,
            @Parameter(description = "结束时间", required = true) 
            @RequestParam @NotBlank String endTime) {
        
        log.info("按时间范围查询商家订单: {}-{}-{}", businessId, startTime, endTime);
        List<UserOrder> orders = orderService.getOrdersByBusinessIdAndTimeRange(businessId, startTime, endTime);
        return R.success(orders);
    }

    @GetMapping("/price-range")
    @Operation(summary = "按价格范围查询订单", description = "根据价格范围查询订单")
    public R<List<UserOrder>> getOrdersByPriceRange(
            @Parameter(description = "最低价格", required = true) 
            @RequestParam @NotNull @Min(0) Double minPrice,
            @Parameter(description = "最高价格", required = true) 
            @RequestParam @NotNull @Min(0) Double maxPrice) {
        
        log.info("按价格范围查询订单: {}-{}", minPrice, maxPrice);
        if (minPrice > maxPrice) {
            return R.error("最低价格不能大于最高价格");
        }
        List<UserOrder> orders = orderService.getOrdersByPriceRange(minPrice, maxPrice);
        return R.success(orders);
    }

    @GetMapping("/recent")
    @Operation(summary = "查询最近订单", description = "查询最近的订单，按时间倒序")
    public R<List<UserOrder>> getRecentOrders(
            @Parameter(description = "限制数量", required = false) 
            @RequestParam(defaultValue = "10") @Min(1) Integer limit) {
        
        log.info("查询最近订单: {}", limit);
        List<UserOrder> orders = orderService.getRecentOrders(limit);
        return R.success(orders);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除订单", description = "删除指定订单（仅允许删除已取消的订单）")
    public R<String> deleteOrder(
            @Parameter(description = "订单ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer id) {
        
        log.info("删除订单: {}", id);
        Boolean success = orderService.deleteOrder(id);
        if (success) {
            return R.success("订单删除成功");
        } else {
            return R.error("订单删除失败");
        }
    }

    @GetMapping("/{id}/time")
    @Operation(summary = "查询订单时间", description = "查询指定订单的创建时间")
    public R<LocalDateTime> getOrderTime(
            @Parameter(description = "订单ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer id) {
        
        log.info("查询订单时间: {}", id);
        UserOrder order = orderService.getById(id);
        if (order != null) {
            return R.success(order.getCreatedAt());
        } else {
            return R.error("订单不存在");
        }
    }

    // 兼容原有接口格式
    @PostMapping("/getUserOrderById")
    @Operation(summary = "根据ID查询订单(兼容接口)", description = "根据订单ID查询订单信息(兼容原有接口)")
    public R<UserOrder> getUserOrderById(@RequestBody Map<String, Integer> requestBody) {
        Integer id = requestBody.get("ID");
        if (id == null) {
            return R.error("订单ID不能为空");
        }
        
        log.info("查询订单信息(兼容接口): {}", id);
        UserOrder order = orderService.getById(id);
        if (order != null) {
            return R.success(order);
        } else {
            return R.error("订单不存在");
        }
    }

    @PostMapping("/addUserOrder")
    @Operation(summary = "创建订单(兼容接口)", description = "创建新的订单(兼容原有接口)")
    public R<Integer> addUserOrder(@RequestBody OrderRequestDTO orderRequestDTO) {
        log.info("创建订单(兼容接口): {}", orderRequestDTO);
        Integer orderId = orderService.createOrder(orderRequestDTO);
        if (orderId != null) {
            return R.success("订单创建成功", orderId);
        } else {
            return R.error("订单创建失败");
        }
    }

    @PostMapping("/havePayed")
    @Operation(summary = "订单支付(兼容接口)", description = "处理订单支付(兼容原有接口)")
    public R<Boolean> havePayed(@RequestBody Map<String, Integer> requestBody) {
        Integer id = requestBody.get("ID");
        if (id == null) {
            return R.error("订单ID不能为空");
        }
        
        log.info("订单支付(兼容接口): {}", id);
        Boolean success = orderService.payOrder(id);
        return R.success(success);
    }

    @GetMapping("/test-business/{id}")
    @Operation(summary = "测试商家服务调用", description = "测试Feign调用商家服务是否正常")
    public R<Object> testBusinessService(@PathVariable Integer id) {
        log.info("测试商家服务调用: {}", id);
        try {
            log.info("开始调用BusinessClient.getBusinessById({})", id);
            R<org.example.entity.Business> result = businessClient.getBusinessById(id);
            log.info("商家服务调用结果: code={}, message={}, data={}", 
                result.getCode(), result.getMessage(), result.getData() != null ? "有数据" : "无数据");
            return R.success("调用成功", result);
        } catch (feign.FeignException e) {
            log.error("Feign调用异常: status={}, contentUTF8={}", e.status(), e.contentUTF8(), e);
            return R.error("Feign调用失败: " + e.status() + " - " + e.contentUTF8());
        } catch (Exception e) {
            log.error("商家服务调用失败", e);
            return R.error("调用失败: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @PostMapping("/getAllUserOrder")
    @Operation(summary = "查询用户订单(兼容接口)", description = "根据用户手机号查询订单列表(兼容原有接口)")
    public R<List<UserOrder>> getAllUserOrder(@RequestBody Map<String, String> requestBody) {
        String userPhone = requestBody.get("userPhone");
        if (userPhone == null || userPhone.trim().isEmpty()) {
            return R.error("用户手机号不能为空");
        }
        
        log.info("查询用户订单(兼容接口): {}", userPhone);
        List<UserOrder> orders = orderService.getOrdersByUserPhone(userPhone);
        return R.success(orders);
    }

    @PostMapping("/getAllUserOrderDetails")
    @Operation(summary = "查询用户订单详情列表(完整信息)", description = "根据用户手机号查询包含商家和食物信息的订单详情列表")
    public R<List<OrderDetailDTO>> getAllUserOrderDetails(@RequestBody Map<String, String> requestBody) {
        String userPhone = requestBody.get("userPhone");
        if (userPhone == null || userPhone.trim().isEmpty()) {
            return R.error("用户手机号不能为空");
        }
        
        log.info("查询用户订单详情列表: {}", userPhone);
        List<OrderDetailDTO> orderDetails = orderService.getOrderDetailsByUserPhone(userPhone);
        return R.success(orderDetails);
    }

    @PostMapping("/getOrderDetail")
    @Operation(summary = "查询订单详情(兼容接口)", description = "根据订单ID查询详细信息(兼容原有接口)")
    public R<OrderDetailDTO> getOrderDetail(@RequestBody Map<String, Integer> requestBody) {
        Integer id = requestBody.get("ID");
        if (id == null) {
            return R.error("订单ID不能为空");
        }
        
        log.info("查询订单详情(兼容接口): {}", id);
        OrderDetailDTO orderDetail = orderService.getOrderDetail(id);
        if (orderDetail != null) {
            return R.success(orderDetail);
        } else {
            return R.error("订单详情不存在");
        }
    }
} 