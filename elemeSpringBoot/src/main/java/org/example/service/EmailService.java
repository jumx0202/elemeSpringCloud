package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    
    // 存储邮箱验证码
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();
    
    public void sendVerificationCode(String to) {
        String code = generateVerificationCode();
        verificationCodes.put(to, code);
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("org@org6.org"); // 配置发件人邮箱
        message.setTo(to);
        message.setSubject("饿了么注册验证码");
        message.setText("您的验证码是: " + code + "\n验证码有效期为5分钟。");
        
        mailSender.send(message);
        
        // 5分钟后删除验证码
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                verificationCodes.remove(to);
            }
        }, 5 * 60 * 1000);
    }
    
    public boolean verifyCode(String email, String code) {
        String storedCode = verificationCodes.get(email);
        return storedCode != null && storedCode.equals(code);
    }
    
    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
} 