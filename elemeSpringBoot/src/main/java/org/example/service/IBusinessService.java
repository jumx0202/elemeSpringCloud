package org.example.service;

import org.example.entity.Business;
//import org.example.response.BusinessResponse;
import java.util.List;

public interface IBusinessService {
    // 根据ID获取商家信息
    Business findBusinessById(Integer id);
    
    // 获取所有商家
    List<Business> getAll();
    
    // 根据类型获取商家
    List<Business> getBusinessByType(String type);
    
    // 搜索商家
    List<Business> searchBusinesses(String keyword);
    
    // 获取推荐商家
    List<Business> getRecommendBusiness();
    
    // 获取新商家
    List<Business> getNewBusiness();
    
    // 获取热门商家
    List<Business> getPopularBusiness();
    
    // 根据评分获取商家
    List<Business> getBusinessByRating(Double minRating);
    
    // 根据配送范围获取商家
    List<Business> getBusinessByDistance(Double maxDistance);
    
    // 根据起送价获取商家
    List<Business> getBusinessByMinPrice(Double maxMinPrice);
}
