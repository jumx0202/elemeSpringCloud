package org.example.service;

import org.example.dto.R;
import org.example.entity.Business;

import java.util.List;

/**
 * 商家服务接口
 */
public interface BusinessService {

    /**
     * 根据ID获取商家详细信息（包含食物列表）
     *
     * @param id 商家ID
     * @return 商家详细信息
     */
    R<Business> getBusinessById(Integer id);

    /**
     * 获取所有正常营业的商家
     *
     * @return 商家列表
     */
    R<List<Business>> getAllBusiness();

    /**
     * 根据类型获取商家
     *
     * @param type 商家类型
     * @return 商家列表
     */
    R<List<Business>> getBusinessByType(String type);

    /**
     * 搜索商家
     *
     * @param keyword 搜索关键字
     * @return 商家列表
     */
    R<List<Business>> searchBusiness(String keyword);

    /**
     * 获取推荐商家
     *
     * @param limit 限制数量
     * @return 商家列表
     */
    R<List<Business>> getRecommendBusiness(Integer limit);

    /**
     * 获取新商家
     *
     * @param limit 限制数量
     * @return 商家列表
     */
    R<List<Business>> getNewBusiness(Integer limit);

    /**
     * 获取热门商家
     *
     * @param limit 限制数量
     * @return 商家列表
     */
    R<List<Business>> getPopularBusiness(Integer limit);

    /**
     * 根据评分获取商家
     *
     * @param minRating 最低评分
     * @return 商家列表
     */
    R<List<Business>> getBusinessByRating(Double minRating);

    /**
     * 按距离获取商家
     *
     * @return 商家列表
     */
    R<List<Business>> getBusinessByDistance();

    /**
     * 获取所有商家类型
     *
     * @return 类型列表
     */
    R<List<String>> getAllBusinessTypes();
} 