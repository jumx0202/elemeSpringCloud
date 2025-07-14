package org.example.feign;

import org.example.dto.R;
import org.example.entity.Business;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "eleme-business-service", fallback = BusinessClientFallback.class)
public interface BusinessClient {

    /**
     * 根据ID查询商家信息
     * @param id 商家ID
     * @return 商家信息
     */
    @GetMapping("/business/{id}")
    R<Business> getBusinessById(@PathVariable("id") Integer id);
} 