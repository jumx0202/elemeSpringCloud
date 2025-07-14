package org.example.mapper;

import org.example.entity.UserOrder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUserOrderMapper extends CrudRepository<UserOrder,Integer> {
    UserOrder findOrderByID(Integer ID);
    List<UserOrder> findAllByUserPhone(String userPhone);

}
