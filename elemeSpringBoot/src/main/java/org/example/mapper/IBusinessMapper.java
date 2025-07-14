package org.example.mapper;

import org.example.entity.Business;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBusinessMapper extends CrudRepository<Business, Integer> {
    // 根据ID查找商家
    Business findBusinessById(Integer id);
    
    // 查找所有商家
    List<Business> findAll();
    
    // 根据类型查找商家
    List<Business> findByType(String type);
    
    // 根据商家名称模糊查询
    List<Business> findByBusinessNameContaining(String keyword);
}
