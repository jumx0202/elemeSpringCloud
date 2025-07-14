package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.R;
import org.example.dto.UserLoginDTO;
import org.example.dto.UserRegisterDTO;
import org.example.dto.UserUpdateDTO;
import org.example.entity.User;
import org.example.service.EmailService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户注册、登录、个人信息管理")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "通过手机号和密码登录")
    public R<Object> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        log.info("用户登录请求: {}", loginDTO.getPhoneNumber());
        return userService.login(loginDTO);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册新账号")
    public R<Object> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        log.info("用户注册请求: {}", registerDTO.getPhoneNumber());
        return userService.register(registerDTO);
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/sendVerificationCode")
    @Operation(summary = "发送邮箱验证码", description = "向指定邮箱发送验证码")
    public R<Object> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        log.info("发送邮箱验证码请求: {}", email);
        
        if (email == null || email.trim().isEmpty()) {
            return R.error("邮箱不能为空");
        }

        // 检查邮箱格式
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return R.error("邮箱格式不正确");
        }

        // 检查邮箱是否已注册
        if (userService.isEmailRegistered(email)) {
            return R.error("该邮箱已被注册");
        }

        boolean success = emailService.sendVerificationCode(email);
        if (success) {
            return R.success("验证码已发送到您的邮箱，请注意查收");
        } else {
            return R.error("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    public R<User> getUserInfo(
            @Parameter(description = "用户手机号，从请求头获取") 
            @RequestHeader("X-User-Id") String phoneNumber) {
        log.info("获取用户信息请求: {}", phoneNumber);
        return userService.getUserByPhoneNumber(phoneNumber);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    @Operation(summary = "更新用户信息", description = "更新用户个人信息")
    public R<Object> updateUser(
            @Parameter(description = "用户手机号，从请求头获取") 
            @RequestHeader("X-User-Id") String phoneNumber,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        log.info("更新用户信息请求: {}", phoneNumber);
        return userService.updateUser(phoneNumber, updateDTO);
    }

    /**
     * 修改密码
     */
    @PutMapping("/changePassword")
    @Operation(summary = "修改密码", description = "修改用户登录密码")
    public R<Object> changePassword(
            @Parameter(description = "用户手机号，从请求头获取") 
            @RequestHeader("X-User-Id") String phoneNumber,
            @RequestBody Map<String, String> request) {
        log.info("修改密码请求: {}", phoneNumber);
        
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return R.error("旧密码和新密码不能为空");
        }
        
        return userService.changePassword(phoneNumber, oldPassword, newPassword);
    }

    /**
     * 检查手机号是否已注册
     */
    @GetMapping("/checkPhone")
    @Operation(summary = "检查手机号", description = "检查手机号是否已注册")
    public R<Object> checkPhoneNumber(@Parameter(description = "手机号") @RequestParam String phoneNumber) {
        log.info("检查手机号请求: {}", phoneNumber);
        
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return R.error("手机号不能为空");
        }
        
        boolean registered = userService.isPhoneNumberRegistered(phoneNumber);
        return R.success("检查完成", Map.of("registered", registered));
    }

    /**
     * 检查邮箱是否已注册
     */
    @GetMapping("/checkEmail")
    @Operation(summary = "检查邮箱", description = "检查邮箱是否已注册")
    public R<Object> checkEmail(@Parameter(description = "邮箱") @RequestParam String email) {
        log.info("检查邮箱请求: {}", email);
        
        if (email == null || email.trim().isEmpty()) {
            return R.error("邮箱不能为空");
        }
        
        boolean registered = userService.isEmailRegistered(email);
        return R.success("检查完成", Map.of("registered", registered));
    }

    /**
     * 根据手机号获取用户信息（供其他服务调用）
     */
    @GetMapping("/phone/{phone}")
    @Operation(summary = "根据手机号获取用户信息", description = "根据手机号获取用户信息（供其他服务调用）")
    public R<User> getUserByPhone(
            @Parameter(description = "用户手机号", required = true) 
            @PathVariable("phone") String phone) {
        log.info("根据手机号获取用户信息: {}", phone);
        return userService.getUserByPhoneNumber(phone);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "服务健康状态检查")
    public R<Object> health() {
        return R.success("用户服务运行正常");
    }
} 