package org.example.service;



import org.example.dto.OrderDetailDTO;
import org.example.dto.OrderRequestDTO;
import org.example.entity.UserOrder;

import java.util.List;

public interface IUserOrderService {
    UserOrder getById(Integer Id);
    Integer addUserOrder(OrderRequestDTO orderRequestDTO);
    Boolean havePayed(Integer Id);
    List<UserOrder> getAllByUserPhone(String userPhone);
    OrderDetailDTO getOrderDetail(Integer ID);
}
