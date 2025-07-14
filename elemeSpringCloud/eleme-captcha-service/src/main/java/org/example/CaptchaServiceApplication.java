package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 验证码服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
public class CaptchaServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CaptchaServiceApplication.class, args);
    }
} 