package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.R;
import org.example.entity.Food;
import org.example.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/food")
@Tag(name = "食物服务", description = "食物信息管理相关接口")
@Slf4j
@Validated
public class FoodController {

    @Autowired
    private FoodService foodService;

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询商品", description = "根据商品ID查询商品详细信息")
    public R<Food> getById(
            @Parameter(description = "商品ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer id) {
        
        log.info("查询商品信息: {}", id);
        Food food = foodService.getById(id);
        if (food != null) {
            return R.success(food);
        } else {
            return R.error("商品不存在");
        }
    }

    @PostMapping("/batch")
    @Operation(summary = "批量查询商品", description = "根据商品ID列表批量查询商品信息")
    public R<List<Food>> getFoodsByIds(
            @Parameter(description = "商品ID列表", required = true) 
            @RequestBody @Valid @NotEmpty List<@NotNull @Min(1) Integer> ids) {
        
        log.info("批量查询商品信息: {}", ids);
        List<Food> foods = foodService.getFoodsByIds(ids);
        return R.success(foods);
    }

    @GetMapping("/business/{businessId}")
    @Operation(summary = "查询商家所有商品", description = "根据商家ID查询该商家的所有商品")
    public R<List<Food>> getAllFoodsByBusinessId(
            @Parameter(description = "商家ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer businessId) {
        
        log.info("查询商家所有商品: {}", businessId);
        List<Food> foods = foodService.getAllFoodsByBusinessId(businessId);
        return R.success(foods);
    }

    @GetMapping("/business/{businessId}/onsale")
    @Operation(summary = "查询商家上架商品", description = "根据商家ID查询该商家上架的商品")
    public R<List<Food>> getOnSaleFoodsByBusinessId(
            @Parameter(description = "商家ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer businessId) {
        
        log.info("查询商家上架商品: {}", businessId);
        List<Food> foods = foodService.getOnSaleFoodsByBusinessId(businessId);
        return R.success(foods);
    }

    @GetMapping("/business/{businessId}/count")
    @Operation(summary = "统计商家商品数量", description = "统计指定商家的商品总数")
    public R<Integer> countFoodsByBusinessId(
            @Parameter(description = "商家ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer businessId) {
        
        log.info("统计商家商品数量: {}", businessId);
        Integer count = foodService.countFoodsByBusinessId(businessId);
        return R.success(count);
    }

    @GetMapping("/business/{businessId}/onsale/count")
    @Operation(summary = "统计商家上架商品数量", description = "统计指定商家上架商品的数量")
    public R<Integer> countOnSaleFoodsByBusinessId(
            @Parameter(description = "商家ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer businessId) {
        
        log.info("统计商家上架商品数量: {}", businessId);
        Integer count = foodService.countOnSaleFoodsByBusinessId(businessId);
        return R.success(count);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索商品", description = "根据商品名称关键词搜索商品")
    public R<List<Food>> searchFoodsByName(
            @Parameter(description = "搜索关键词", required = true) 
            @RequestParam @NotNull @Size(min = 1, max = 50) String name) {
        
        log.info("搜索商品: {}", name);
        List<Food> foods = foodService.searchFoodsByName(name);
        return R.success(foods);
    }

    @GetMapping("/price-range")
    @Operation(summary = "价格区间查询", description = "根据价格区间查询商品")
    public R<List<Food>> getFoodsByPriceRange(
            @Parameter(description = "最低价格", required = true) 
            @RequestParam @NotNull @Min(0) Double minPrice,
            @Parameter(description = "最高价格", required = true) 
            @RequestParam @NotNull @Min(0) Double maxPrice) {
        
        log.info("价格区间查询商品: {}-{}", minPrice, maxPrice);
        if (minPrice > maxPrice) {
            return R.error("最低价格不能大于最高价格");
        }
        List<Food> foods = foodService.getFoodsByPriceRange(minPrice, maxPrice);
        return R.success(foods);
    }

    @GetMapping("/hot")
    @Operation(summary = "查询热门商品", description = "查询热门商品，按销量排序")
    public R<List<Food>> getHotFoods(
            @Parameter(description = "限制数量", required = false) 
            @RequestParam(defaultValue = "10") @Min(1) Integer limit) {
        
        log.info("查询热门商品: {}", limit);
        List<Food> foods = foodService.getHotFoods(limit);
        return R.success(foods);
    }

    @GetMapping("/business/{businessId}/hot")
    @Operation(summary = "查询商家热门商品", description = "查询指定商家的热门商品")
    public R<List<Food>> getHotFoodsByBusinessId(
            @Parameter(description = "商家ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer businessId,
            @Parameter(description = "限制数量", required = false) 
            @RequestParam(defaultValue = "10") @Min(1) Integer limit) {
        
        log.info("查询商家热门商品: {}-{}", businessId, limit);
        List<Food> foods = foodService.getHotFoodsByBusinessId(businessId, limit);
        return R.success(foods);
    }

    @GetMapping("/discount")
    @Operation(summary = "查询特价商品", description = "查询所有特价商品")
    public R<List<Food>> getDiscountFoods() {
        log.info("查询特价商品");
        List<Food> foods = foodService.getDiscountFoods();
        return R.success(foods);
    }

    @GetMapping("/business/{businessId}/discount")
    @Operation(summary = "查询商家特价商品", description = "查询指定商家的特价商品")
    public R<List<Food>> getDiscountFoodsByBusinessId(
            @Parameter(description = "商家ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer businessId) {
        
        log.info("查询商家特价商品: {}", businessId);
        List<Food> foods = foodService.getDiscountFoodsByBusinessId(businessId);
        return R.success(foods);
    }

    @PostMapping
    @Operation(summary = "新增商品", description = "新增商品信息")
    public R<String> addFood(
            @Parameter(description = "商品信息", required = true) 
            @RequestBody @Valid Food food) {
        
        log.info("新增商品: {}", food);
        boolean success = foodService.addFood(food);
        if (success) {
            return R.success("新增商品成功");
        } else {
            return R.error("新增商品失败");
        }
    }

    @PutMapping
    @Operation(summary = "更新商品", description = "更新商品信息")
    public R<String> updateFood(
            @Parameter(description = "商品信息", required = true) 
            @RequestBody @Valid Food food) {
        
        log.info("更新商品: {}", food);
        if (food.getId() == null) {
            return R.error("商品ID不能为空");
        }
        boolean success = foodService.updateFood(food);
        if (success) {
            return R.success("更新商品成功");
        } else {
            return R.error("更新商品失败");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品", description = "删除指定商品")
    public R<String> deleteFood(
            @Parameter(description = "商品ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer id) {
        
        log.info("删除商品: {}", id);
        boolean success = foodService.deleteFood(id);
        if (success) {
            return R.success("删除商品成功");
        } else {
            return R.error("删除商品失败");
        }
    }

    @PutMapping("/{id}/onsale")
    @Operation(summary = "商品上架", description = "将指定商品设为上架状态")
    public R<String> onSaleFood(
            @Parameter(description = "商品ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer id) {
        
        log.info("商品上架: {}", id);
        boolean success = foodService.onSaleFood(id);
        if (success) {
            return R.success("商品上架成功");
        } else {
            return R.error("商品上架失败");
        }
    }

    @PutMapping("/{id}/offsale")
    @Operation(summary = "商品下架", description = "将指定商品设为下架状态")
    public R<String> offSaleFood(
            @Parameter(description = "商品ID", required = true) 
            @PathVariable @NotNull @Min(1) Integer id) {
        
        log.info("商品下架: {}", id);
        boolean success = foodService.offSaleFood(id);
        if (success) {
            return R.success("商品下架成功");
        } else {
            return R.error("商品下架失败");
        }
    }

    // 兼容原有接口格式
    @PostMapping("/getAllByIds")
    @Operation(summary = "批量查询商品(兼容接口)", description = "根据商品ID列表批量查询商品信息(兼容原有接口)")
    public R<List<Food>> getAllByIds(@RequestBody Map<String, List<Integer>> requestBody) {
        List<Integer> ids = requestBody.get("ids");
        if (ids == null || ids.isEmpty()) {
            return R.error("商品ID列表不能为空");
        }
        
        log.info("批量查询商品信息(兼容接口): {}", ids);
        List<Food> foods = foodService.getFoodsByIds(ids);
        return R.success(foods);
    }

    @PostMapping("/getFoodById")
    @Operation(summary = "根据ID查询商品(兼容接口)", description = "根据商品ID查询商品信息(兼容原有接口)")
    public R<Food> getFoodById(@RequestBody Map<String, Integer> requestBody) {
        Integer id = requestBody.get("ID");
        if (id == null) {
            return R.error("商品ID不能为空");
        }
        
        log.info("查询商品信息(兼容接口): {}", id);
        Food food = foodService.getById(id);
        if (food != null) {
            return R.success(food);
        } else {
            return R.error("商品不存在");
        }
    }
} 