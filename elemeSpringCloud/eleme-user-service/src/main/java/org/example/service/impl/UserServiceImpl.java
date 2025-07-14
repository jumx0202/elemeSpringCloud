package org.example.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import lombok.extern.slf4j.Slf4j;
import org.example.constants.CommonConstants;
import org.example.dto.R;
import org.example.dto.UserLoginDTO;
import org.example.dto.UserRegisterDTO;
import org.example.dto.UserUpdateDTO;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.example.service.EmailService;
import org.example.service.UserService;
import org.example.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    @SentinelResource(value = "user-login", fallback = "loginFallback")
    public R<Object> login(UserLoginDTO loginDTO) {
        try {
            // 检查参数
            if (StrUtil.isBlank(loginDTO.getPhoneNumber()) || StrUtil.isBlank(loginDTO.getPassword())) {
                return R.error(CommonConstants.ERROR_CODE, "手机号和密码不能为空");
            }

            // 验证验证码（如果需要）
            if (StrUtil.isNotBlank(loginDTO.getCaptchaId()) && StrUtil.isNotBlank(loginDTO.getCaptchaValue())) {
                if (!verifyCaptcha(loginDTO.getCaptchaId(), loginDTO.getCaptchaValue())) {
                    return R.error(CommonConstants.ERROR_CODE, "验证码错误");
                }
            }

            // 密码加密
            String encryptedPassword = DigestUtil.md5Hex(loginDTO.getPassword());

            // 查询用户
            User user = userMapper.findByPhoneNumberAndPassword(loginDTO.getPhoneNumber(), encryptedPassword);
            if (user == null) {
                log.warn("用户登录失败，手机号或密码错误: {}", loginDTO.getPhoneNumber());
                return R.error(CommonConstants.ERROR_CODE, "手机号或密码错误");
            }

            // 检查用户状态
            if (user.getStatus() != CommonConstants.USER_STATUS_NORMAL) {
                return R.error(CommonConstants.ERROR_CODE, "用户已被禁用");
            }

            // 生成JWT Token
            String token = JwtUtil.generateUserToken(user.getPhoneNumber());

            // 缓存用户信息到Redis
            String userKey = CommonConstants.USER_TOKEN_PREFIX + user.getPhoneNumber();
            redisTemplate.opsForValue().set(userKey, token, CommonConstants.TOKEN_EXPIRE, java.util.concurrent.TimeUnit.SECONDS);

            // 构造返回数据
            Map<String, Object> loginResult = new HashMap<>();
            loginResult.put("token", token);
            
            // 用户信息（隐藏敏感信息）
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("phoneNumber", user.getPhoneNumber());
            userInfo.put("name", user.getName());
            userInfo.put("email", user.getEmail());
            userInfo.put("gender", user.getGender());
            userInfo.put("avatar", user.getAvatar());
            loginResult.put("user", userInfo);

            log.info("用户登录成功: {}", user.getPhoneNumber());
            return R.success("登录成功", loginResult);

        } catch (Exception e) {
            log.error("用户登录异常", e);
            return R.error("登录失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SentinelResource(value = "user-register", fallback = "registerFallback")
    public R<Object> register(UserRegisterDTO registerDTO) {
        try {
            // 检查密码一致性
            if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
                return R.error(CommonConstants.ERROR_CODE, "两次密码不一致");
            }

            // 检查手机号是否已注册
            if (userMapper.existsByPhoneNumber(registerDTO.getPhoneNumber())) {
                return R.error(CommonConstants.ERROR_CODE, "该手机号已被注册");
            }

            // 检查邮箱是否已注册
            if (userMapper.existsByEmail(registerDTO.getEmail())) {
                return R.error(CommonConstants.ERROR_CODE, "该邮箱已被注册");
            }

            // 验证邮箱验证码
            if (!emailService.verifyCode(registerDTO.getEmail(), registerDTO.getVerifyCode())) {
                return R.error(CommonConstants.ERROR_CODE, "邮箱验证码错误或已过期");
            }

            // 创建用户
            User user = new User();
            user.setPhoneNumber(registerDTO.getPhoneNumber());
            user.setPassword(DigestUtil.md5Hex(registerDTO.getPassword())); // MD5加密
            user.setName(registerDTO.getName());
            user.setEmail(registerDTO.getEmail());
            user.setGender(registerDTO.getGender());
            user.setStatus(CommonConstants.USER_STATUS_NORMAL);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            int result = userMapper.insert(user);
            if (result > 0) {
                log.info("用户注册成功: {}", user.getPhoneNumber());
                return R.success("注册成功");
            } else {
                return R.error("注册失败");
            }

        } catch (Exception e) {
            log.error("用户注册异常", e);
            return R.error("注册失败，请稍后重试");
        }
    }

    @Override
    @SentinelResource(value = "get-user", fallback = "getUserFallback")
    public R<User> getUserByPhoneNumber(String phoneNumber) {
        try {
            if (StrUtil.isBlank(phoneNumber)) {
                return R.error(CommonConstants.ERROR_CODE, "手机号不能为空");
            }

            User user = userMapper.findByPhoneNumber(phoneNumber);
            if (user == null) {
                return R.error(CommonConstants.NOT_FOUND_CODE, "用户不存在");
            }

            // 隐藏敏感信息
            user.setPassword(null);

            return R.success(user);

        } catch (Exception e) {
            log.error("获取用户信息异常", e);
            return R.error("获取用户信息失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SentinelResource(value = "update-user", fallback = "updateUserFallback")
    public R<Object> updateUser(String phoneNumber, UserUpdateDTO updateDTO) {
        try {
            if (StrUtil.isBlank(phoneNumber)) {
                return R.error(CommonConstants.ERROR_CODE, "手机号不能为空");
            }

            User user = userMapper.findByPhoneNumber(phoneNumber);
            if (user == null) {
                return R.error(CommonConstants.NOT_FOUND_CODE, "用户不存在");
            }

            // 检查邮箱是否被其他用户使用
            if (StrUtil.isNotBlank(updateDTO.getEmail()) && !updateDTO.getEmail().equals(user.getEmail())) {
                if (userMapper.existsByEmail(updateDTO.getEmail())) {
                    return R.error(CommonConstants.ERROR_CODE, "该邮箱已被其他用户使用");
                }
            }

            // 更新用户信息
            if (StrUtil.isNotBlank(updateDTO.getName())) {
                user.setName(updateDTO.getName());
            }
            if (StrUtil.isNotBlank(updateDTO.getEmail())) {
                user.setEmail(updateDTO.getEmail());
            }
            if (StrUtil.isNotBlank(updateDTO.getGender())) {
                user.setGender(updateDTO.getGender());
            }
            if (StrUtil.isNotBlank(updateDTO.getAvatar())) {
                user.setAvatar(updateDTO.getAvatar());
            }
            user.setUpdatedAt(LocalDateTime.now());

            int result = userMapper.updateById(user);
            if (result > 0) {
                log.info("用户信息更新成功: {}", phoneNumber);
                return R.success("更新成功");
            } else {
                return R.error("更新失败");
            }

        } catch (Exception e) {
            log.error("更新用户信息异常", e);
            return R.error("更新失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SentinelResource(value = "change-password", fallback = "changePasswordFallback")
    public R<Object> changePassword(String phoneNumber, String oldPassword, String newPassword) {
        try {
            if (StrUtil.isBlank(phoneNumber) || StrUtil.isBlank(oldPassword) || StrUtil.isBlank(newPassword)) {
                return R.error(CommonConstants.ERROR_CODE, "参数不能为空");
            }

            if (newPassword.length() < 6 || newPassword.length() > 20) {
                return R.error(CommonConstants.ERROR_CODE, "新密码长度必须在6-20位之间");
            }

            // 验证旧密码
            String encryptedOldPassword = DigestUtil.md5Hex(oldPassword);
            User user = userMapper.findByPhoneNumberAndPassword(phoneNumber, encryptedOldPassword);
            if (user == null) {
                return R.error(CommonConstants.ERROR_CODE, "旧密码错误");
            }

            // 更新密码
            user.setPassword(DigestUtil.md5Hex(newPassword));
            user.setUpdatedAt(LocalDateTime.now());

            int result = userMapper.updateById(user);
            if (result > 0) {
                // 删除Redis中的Token，强制重新登录
                String userKey = CommonConstants.USER_TOKEN_PREFIX + phoneNumber;
                redisTemplate.delete(userKey);

                log.info("用户密码修改成功: {}", phoneNumber);
                return R.success("密码修改成功，请重新登录");
            } else {
                return R.error("密码修改失败");
            }

        } catch (Exception e) {
            log.error("修改密码异常", e);
            return R.error("密码修改失败，请稍后重试");
        }
    }

    @Override
    public boolean isPhoneNumberRegistered(String phoneNumber) {
        return userMapper.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public boolean isEmailRegistered(String email) {
        return userMapper.existsByEmail(email);
    }

    /**
     * 验证验证码
     */
    private boolean verifyCaptcha(String captchaId, String captchaValue) {
        String key = CommonConstants.CAPTCHA_PREFIX + captchaId;
        String storedValue = redisTemplate.opsForValue().get(key);
        if (storedValue != null && storedValue.equalsIgnoreCase(captchaValue)) {
            redisTemplate.delete(key); // 验证成功后删除
            return true;
        }
        return false;
    }

    // Sentinel 降级方法
    public R<Object> loginFallback(UserLoginDTO loginDTO, Throwable ex) {
        log.error("用户登录服务降级", ex);
        return R.error("登录服务暂时不可用，请稍后重试");
    }

    public R<Object> registerFallback(UserRegisterDTO registerDTO, Throwable ex) {
        log.error("用户注册服务降级", ex);
        return R.error("注册服务暂时不可用，请稍后重试");
    }

    public R<User> getUserFallback(String phoneNumber, Throwable ex) {
        log.error("获取用户信息服务降级", ex);
        return R.error("获取用户信息服务暂时不可用");
    }

    public R<Object> updateUserFallback(String phoneNumber, UserUpdateDTO updateDTO, Throwable ex) {
        log.error("更新用户信息服务降级", ex);
        return R.error("更新用户信息服务暂时不可用");
    }

    public R<Object> changePasswordFallback(String phoneNumber, String oldPassword, String newPassword, Throwable ex) {
        log.error("修改密码服务降级", ex);
        return R.error("修改密码服务暂时不可用");
    }
} 