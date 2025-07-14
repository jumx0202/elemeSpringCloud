package org.example.feign;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.R;
import org.example.entity.User;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserClientFallback implements UserClient {

    @Override
    public R<User> getUserByPhone(String phone) {
        log.error("调用用户服务失败，手机号: {}", phone);
        return R.error("用户服务暂时不可用");
    }

    @Override
    public R<User> getUserById(Integer id) {
        log.error("调用用户服务失败，用户ID: {}", id);
        return R.error("用户服务暂时不可用");
    }
} 