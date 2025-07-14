package org.example.mapper;

import org.example.entity.Food;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface IFoodMapper extends CrudRepository<Food, Integer> {
    List<Food> findAllByBusiness(Integer businessId);
    Food findFoodById(Integer id);
}
