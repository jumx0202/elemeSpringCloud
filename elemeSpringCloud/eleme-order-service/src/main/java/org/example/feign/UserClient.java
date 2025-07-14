package org.example.feign;

import org.example.dto.R;
import org.example.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "eleme-user-service", fallback = UserClientFallback.class)
public interface UserClient {

    /**
     * 根据手机号查询用户信息
     * @param phone 手机号
     * @return 用户信息
     */
    @GetMapping("/user/phone/{phone}")
    R<User> getUserByPhone(@PathVariable("phone") String phone);

    /**
     * 根据ID查询用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/user/{id}")
    R<User> getUserById(@PathVariable("id") Integer id);
} 