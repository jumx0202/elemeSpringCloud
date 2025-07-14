package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.dto.OrderDetailDTO;
import org.example.dto.OrderRequestDTO;
import org.example.entity.UserOrder;
import org.example.service.IUserOrderService;
import org.example.service.impl.UserOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
//API子路径
@RequestMapping("/order")
//标签
@Tag(name = "订单服务接口")
//跨域
@CrossOrigin(origins = "*" )
public class OrderController {
    //资源
    @Resource
    IUserOrderService userOrderService;
    @PostMapping("/getUserOrderById")
    public ResponseEntity<UserOrder> getUserOrderById(@RequestBody Map<String, Integer> requestBody) {
        // 获取ID值
        Integer id = requestBody.get("ID");
        System.out.println(id);
        if (id == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // 调用服务层方法获取业务响应
        UserOrder userOrder = userOrderService.getById(id);

        if (userOrder!=null) {
            return new ResponseEntity<>(userOrder, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/addUserOrder")
    public ResponseEntity<Integer> addUserOrder(@RequestBody OrderRequestDTO requestBody){
        return new ResponseEntity<>(userOrderService.addUserOrder(requestBody),HttpStatus.OK);
    }

    @PostMapping("/havePayed")
    public ResponseEntity<Boolean> havePayed(@RequestBody Map<String,Integer> requestBody){
        Integer ID = requestBody.get("ID");
        return new ResponseEntity<>(userOrderService.havePayed(ID),HttpStatus.OK);
    }

    @PostMapping("/getAllUserOrder")
    public ResponseEntity<List<UserOrder>> getAllUserOrder(@RequestBody Map<String,String> requestBody){
        String userPhone = requestBody.get("userPhone");
        List<UserOrder> userOrders = userOrderService.getAllByUserPhone(userPhone);
        return new ResponseEntity<>(userOrders,HttpStatus.OK);
    }

    @PostMapping("/getOrderDetail")
    public ResponseEntity<OrderDetailDTO> getOrderDetail(@RequestBody Map<String, Integer> requestBody) {
        Integer ID = requestBody.get("ID");
        if (ID == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        OrderDetailDTO orderDetailDTO = userOrderService.getOrderDetail(ID);
        if(orderDetailDTO != null) {
            return new ResponseEntity<>(orderDetailDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }


    @GetMapping("/getOrderTime/{id}")
    public ResponseEntity<LocalDateTime> getOrderTime(@PathVariable Integer id) {
        UserOrder order = userOrderService.getById(id);
        if (order != null) {
            return new ResponseEntity<>(order.getCreatedAt(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
