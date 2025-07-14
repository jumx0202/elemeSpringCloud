package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.dto.IdsWrapper;
import org.example.entity.Food;
import org.example.service.IFoodService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
//API子路径
@RequestMapping("/food")
//标签
@Tag(name = "商品（食物）服务接口")
//跨域
@CrossOrigin(origins = "*" )
public class FoodController {

    //资源
    @Resource
    IFoodService foodService;


    @PostMapping("/getAllByIds")
    public ResponseEntity<List<Food>> getAllByIds(@RequestBody IdsWrapper idsWrapper) {
        System.out.println(idsWrapper.getIds());
        List<Food> foodList = foodService.getFoodsByIds(idsWrapper.getIds().toArray(new Integer[0]));
        if(!foodList.isEmpty()){
            return new ResponseEntity<>(foodList,HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @PostMapping("/getFoodById")
    public ResponseEntity<Food> getFoodById(@RequestBody Map<String, Integer> requestBody) {
        Integer ID = requestBody.get("ID");
        if (ID == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Food food = foodService.getById(ID);
        if (food != null) {
            return new ResponseEntity<>(food, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
