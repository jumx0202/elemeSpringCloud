package org.example.feign;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.R;
import org.example.entity.Business;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BusinessClientFallback implements BusinessClient {

    @Override
    public R<Business> getBusinessById(Integer id) {
        log.error("调用商家服务失败，商家ID: {}", id);
        return R.error("商家服务暂时不可用");
    }
} 