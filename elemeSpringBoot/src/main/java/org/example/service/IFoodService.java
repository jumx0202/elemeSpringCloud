package org.example.service;

import org.example.entity.Food;

import java.util.List;

public interface IFoodService {
    List<Food> getFoodsByIds(Integer[] Ids);
    Food getById(Integer id);
    List<Food> getFoodsByBusinessId(Integer businessId);
}
