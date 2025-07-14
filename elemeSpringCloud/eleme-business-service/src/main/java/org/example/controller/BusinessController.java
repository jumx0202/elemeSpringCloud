package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.R;
import org.example.entity.Business;
import org.example.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商家控制器
 */
@Slf4j
@RestController
@RequestMapping("/business")
@Tag(name = "商家管理", description = "商家信息查询、搜索、分类等功能")
@Validated
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    /**
     * 根据ID获取商家详细信息
     */
    @PostMapping("/getBusinessById")
    @Operation(summary = "获取商家详情", description = "根据商家ID获取详细信息，包含食物列表")
    public R<Business> getBusinessById(@RequestBody Map<String, Integer> request) {
        Integer id = request.get("ID");
        log.info("获取商家详情请求: {}", id);
        return businessService.getBusinessById(id);
    }

    /**
     * 根据ID获取商家详细信息（供其他服务调用）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取商家详情", description = "根据商家ID获取详细信息（供其他服务调用）")
    public R<Business> getBusinessByIdForService(
            @Parameter(description = "商家ID", required = true) 
            @PathVariable("id") Integer id) {
        log.info("获取商家详情请求（服务调用）: {}", id);
        return businessService.getBusinessById(id);
    }

    /**
     * 获取所有商家
     */
    @PostMapping("/getAll")
    @Operation(summary = "获取所有商家", description = "获取所有正常营业的商家列表")
    public R<List<Business>> getAllBusiness() {
        log.info("获取所有商家请求");
        return businessService.getAllBusiness();
    }

    /**
     * 根据类型获取商家
     */
    @PostMapping("/getBusinessByType")
    @Operation(summary = "按类型获取商家", description = "根据商家类型获取商家列表")
    public R<List<Business>> getBusinessByType(@RequestBody Map<String, String> request) {
        String type = request.get("type");
        log.info("根据类型获取商家请求: {}", type);
        return businessService.getBusinessByType(type);
    }

    /**
     * 搜索商家
     */
    @PostMapping("/search")
    @Operation(summary = "搜索商家", description = "根据关键字搜索商家")
    public R<List<Business>> searchBusiness(@RequestBody Map<String, String> request) {
        String keyword = request.get("keyword");
        log.info("搜索商家请求: {}", keyword);
        return businessService.searchBusiness(keyword);
    }

    /**
     * 获取推荐商家
     */
    @GetMapping("/recommend")
    @Operation(summary = "获取推荐商家", description = "获取推荐的商家列表（按评分和销量排序）")
    public R<List<Business>> getRecommendBusiness(
            @Parameter(description = "限制数量，默认10个") 
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取推荐商家请求，数量: {}", limit);
        return businessService.getRecommendBusiness(limit);
    }

    /**
     * 获取新商家
     */
    @GetMapping("/new")
    @Operation(summary = "获取新商家", description = "获取新开的商家列表（按创建时间排序）")
    public R<List<Business>> getNewBusiness(
            @Parameter(description = "限制数量，默认10个") 
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取新商家请求，数量: {}", limit);
        return businessService.getNewBusiness(limit);
    }

    /**
     * 获取热门商家
     */
    @GetMapping("/popular")
    @Operation(summary = "获取热门商家", description = "获取热门商家列表（按销量排序）")
    public R<List<Business>> getPopularBusiness(
            @Parameter(description = "限制数量，默认10个") 
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取热门商家请求，数量: {}", limit);
        return businessService.getPopularBusiness(limit);
    }

    /**
     * 根据评分获取商家
     */
    @GetMapping("/byRating")
    @Operation(summary = "按评分获取商家", description = "获取评分不低于指定值的商家")
    public R<List<Business>> getBusinessByRating(
            @Parameter(description = "最低评分，0-5之间") 
            @RequestParam Double minRating) {
        log.info("根据评分获取商家请求，最低评分: {}", minRating);
        return businessService.getBusinessByRating(minRating);
    }

    /**
     * 按距离获取商家
     */
    @GetMapping("/byDistance")
    @Operation(summary = "按距离获取商家", description = "按距离远近排序获取商家")
    public R<List<Business>> getBusinessByDistance() {
        log.info("按距离获取商家请求");
        return businessService.getBusinessByDistance();
    }

    /**
     * 获取所有商家类型
     */
    @GetMapping("/types")
    @Operation(summary = "获取商家类型", description = "获取所有可用的商家类型列表")
    public R<List<String>> getAllBusinessTypes() {
        log.info("获取商家类型请求");
        return businessService.getAllBusinessTypes();
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "服务健康状态检查")
    public R<Object> health() {
        return R.success("商家服务运行正常");
    }
} 