package org.example.service.impl;

import jakarta.annotation.Resource;
import org.example.entity.User;
import org.example.mapper.IUserMapper;
import org.example.service.IUserService;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserService implements IUserService {
    //资源
    @Resource
    private IUserMapper userMapper;
    @Override
    public User login(String phoneNumber, String password) {
        return userMapper.findByPhoneNumberAndPassword(phoneNumber, password);
    }

    @Override
    public Integer register(String phoneNumber, String password, String confirmPassword, 
                          String name, String email) {
        // 检查手机号是否已注册
        if (userMapper.existsByPhoneNumber(phoneNumber)) {
            return 0; // 已注册
        }
        
        // 检查密码是否一致
        if (!password.equals(confirmPassword)) {
            return -2; // 密码不一致
        }
        
        // 创建新用户
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setPassword(password);
        user.setName(name);
        user.setEmail(email);
        
        userMapper.save(user);
        return 1; // 注册成功
    }
}
