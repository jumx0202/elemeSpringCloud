package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.constants.CommonConstants;
import org.example.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 邮件服务实现类
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean sendVerificationCode(String email) {
        try {
            // 检查是否频繁发送
            String rateKey = "email_rate:" + email;
            String rateValue = redisTemplate.opsForValue().get(rateKey);
            if (rateValue != null) {
                log.warn("邮箱 {} 发送验证码过于频繁", email);
                return false;
            }

            // 生成6位数验证码
            String code = generateVerificationCode();

            // 发送邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("org@org6.org");
            message.setTo(email);
            message.setSubject("饿了么注册验证码");
            message.setText(String.format(
                "您好！\n\n" +
                "您正在注册饿了么账号，验证码为：%s\n\n" +
                "验证码有效期为5分钟，请尽快完成验证。\n" +
                "如果不是您本人操作，请忽略此邮件。\n\n" +
                "饿了么团队", code));

            mailSender.send(message);

            // 存储验证码到Redis，5分钟过期
            String codeKey = CommonConstants.EMAIL_CODE_PREFIX + email;
            redisTemplate.opsForValue().set(codeKey, code, CommonConstants.EMAIL_CODE_EXPIRE, TimeUnit.SECONDS);

            // 设置发送频率限制，60秒内不能重复发送
            redisTemplate.opsForValue().set(rateKey, "1", 60, TimeUnit.SECONDS);

            log.info("验证码发送成功到邮箱: {}", email);
            return true;

        } catch (Exception e) {
            log.error("发送邮箱验证码失败: {}", email, e);
            return false;
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        try {
            String codeKey = CommonConstants.EMAIL_CODE_PREFIX + email;
            String storedCode = redisTemplate.opsForValue().get(codeKey);

            if (storedCode != null && storedCode.equals(code)) {
                // 验证成功后删除验证码
                redisTemplate.delete(codeKey);
                log.info("邮箱验证码验证成功: {}", email);
                return true;
            }

            log.warn("邮箱验证码验证失败: {}, 输入的验证码: {}", email, code);
            return false;

        } catch (Exception e) {
            log.error("验证邮箱验证码异常: {}", email, e);
            return false;
        }
    }

    /**
     * 生成6位数字验证码
     */
    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
} 