package org.example.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.entity.User;
import org.example.mapper.IUserMapper;
import org.example.service.IUserService;
import org.example.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;


@RestController
//API子路径
@RequestMapping("/user")
//标签
@Tag(name = "用户服务接口")
//跨域
@CrossOrigin(origins = "*" )
public class UserController {
    //资源
    @Resource
    private IUserMapper userMapper;
    @Resource
    private IUserService userService;
    @Resource
    private EmailService emailService;



    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        try {
            String phoneNumber = loginData.get("phoneNumber");
            String password = loginData.get("password");
            
            System.out.println("Received login request - Phone: " + phoneNumber);
            
            if (phoneNumber == null || password == null) {
                System.out.println("Missing required fields");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            
            User user = userService.login(phoneNumber, password);
            System.out.println("Login service returned user: " + user);
            
            if (user != null) {
                String token = UUID.randomUUID().toString();
                
                // 创建用户信息对象
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("phoneNumber", user.getPhoneNumber());
                userInfo.put("name", user.getName());
                userInfo.put("email", user.getEmail());
                userInfo.put("gender", user.getGender());
                
                // 创建响应数据
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "登录成功");
                response.put("token", token);
                response.put("user", userInfo);
                
                System.out.println("Login successful, returning response");
                return ResponseEntity.ok(response);
            } else {
                System.out.println("Login failed - invalid credentials");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("code", 401);
                errorResponse.put("message", "用户名或密码错误");
                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "服务器错误：" + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/sendVerifyCode")
    public ResponseEntity<String> sendVerifyCode(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        if (email == null || email.isEmpty()) {
            return new ResponseEntity<>("邮箱不能为空", HttpStatus.BAD_REQUEST);
        }
        
        try {
            emailService.sendVerificationCode(email);
            return new ResponseEntity<>("验证码已发送", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("发送验证码失败", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Integer> register(@RequestBody Map<String, String> registerData) {
        String phoneNumber = registerData.get("phoneNumber");
        String password = registerData.get("password");
        String confirmPassword = registerData.get("confirmPassword");
        String name = registerData.get("name");
        String email = registerData.get("email");
        String verifyCode = registerData.get("verifyCode");
        
        // 验证邮箱验证码
        if (!emailService.verifyCode(email, verifyCode)) {
            return new ResponseEntity<>(-1, HttpStatus.OK); // -1 表示验证码错误
        }
        
        Integer haveRegistered = userService.register(phoneNumber, password, confirmPassword, name, email);
        return new ResponseEntity<>(haveRegistered, HttpStatus.OK);
    }


}
