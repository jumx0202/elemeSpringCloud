package org.example.service;

import org.example.dto.R;
import org.example.dto.UserLoginDTO;
import org.example.dto.UserRegisterDTO;
import org.example.dto.UserUpdateDTO;
import org.example.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    R<Object> login(UserLoginDTO loginDTO);

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    R<Object> register(UserRegisterDTO registerDTO);

    /**
     * 根据手机号获取用户信息
     *
     * @param phoneNumber 手机号
     * @return 用户信息
     */
    R<User> getUserByPhoneNumber(String phoneNumber);

    /**
     * 更新用户信息
     *
     * @param phoneNumber 手机号
     * @param updateDTO 更新信息
     * @return 更新结果
     */
    R<Object> updateUser(String phoneNumber, UserUpdateDTO updateDTO);

    /**
     * 修改密码
     *
     * @param phoneNumber 手机号
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    R<Object> changePassword(String phoneNumber, String oldPassword, String newPassword);

    /**
     * 检查手机号是否已注册
     *
     * @param phoneNumber 手机号
     * @return 是否已注册
     */
    boolean isPhoneNumberRegistered(String phoneNumber);

    /**
     * 检查邮箱是否已注册
     *
     * @param email 邮箱
     * @return 是否已注册
     */
    boolean isEmailRegistered(String email);
} 