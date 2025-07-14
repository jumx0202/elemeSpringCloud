package org.example.service.impl;

import jakarta.annotation.Resource;
import org.example.dto.OrderDetailDTO;
import org.example.dto.OrderRequestDTO;
import org.example.entity.Business;
import org.example.entity.Food;
import org.example.entity.User;
import org.example.entity.UserOrder;
import org.example.mapper.IBusinessMapper;
import org.example.mapper.IFoodMapper;
import org.example.mapper.IUserOrderMapper;
import org.example.service.IUserOrderService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserOrderService implements IUserOrderService {
    //资源
    @Resource
    IUserOrderMapper userOrderMapper;
    @Resource
    IBusinessMapper businessMapper;
    @Resource
    IFoodMapper foodMapper;
    @Override
    public UserOrder getById(Integer Id) {
        return userOrderMapper.findById(Id).orElse(null);
    }

    @Override
    public Integer addUserOrder(OrderRequestDTO orderRequestDTO) {
        //当前时间
        LocalDateTime currentTime = LocalDateTime.now();
        Integer businessID = orderRequestDTO.getBusinessID();
        String userPhone = orderRequestDTO.getUserPhone();
        List<Integer> orderList = orderRequestDTO.getOrderList();
        Double price = orderRequestDTO.getPrice();
        String orders = orderList.stream().map(String::valueOf).collect(Collectors.joining("-"));
        UserOrder userOrder = new UserOrder();
        userOrder.setUserPhone(userPhone);
        userOrder.setBusinessID(businessID);
        userOrder.setOrderList(orders);
        userOrder.setPrice(price);
        userOrder.setState(0);  // 设置初始状态为未支付
        //保存到数据库
        UserOrder savedUserOrder = userOrderMapper.save(userOrder);
        //获取其订单ID
        return savedUserOrder.getID();
    }

    @Override
    public Boolean havePayed(Integer ID) {
        Optional<UserOrder> orderOptional = userOrderMapper.findById(ID);
        if(orderOptional.isPresent()){
            UserOrder userOrder = orderOptional.get();
            userOrder.setState(1);  // 设置状态为已支付
            userOrderMapper.save(userOrder);
            return true;
        }
        return false;
    }

    @Override
    public List<UserOrder> getAllByUserPhone(String userPhone) {
        return userOrderMapper.findAllByUserPhone(userPhone);
    }

    @Override
    public OrderDetailDTO getOrderDetail(Integer ID) {
        if (ID == null) {
            return null;
        }
        Optional<UserOrder> orderOptional = userOrderMapper.findById(ID);
        if(orderOptional.isEmpty()) {
            return null;
        }
        UserOrder userOrder = orderOptional.get();
        
        OrderDetailDTO orderDetailDTO = new OrderDetailDTO();
        orderDetailDTO.setId(userOrder.getID());
        orderDetailDTO.setBusinessID(userOrder.getBusinessID());
        orderDetailDTO.setUserPhone(userOrder.getUserPhone());
        orderDetailDTO.setOrderList(userOrder.getOrderList());
        orderDetailDTO.setPrice(userOrder.getPrice());
        orderDetailDTO.setState(userOrder.getState());
        orderDetailDTO.setCreatedAt(userOrder.getCreatedAt());
        return orderDetailDTO;
    }


}
