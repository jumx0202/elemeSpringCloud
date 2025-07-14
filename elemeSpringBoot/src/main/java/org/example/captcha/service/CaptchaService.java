package org.example.captcha.service;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.example.captcha.dto.CaptchaRO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
@EnableScheduling
public class CaptchaService {

    // 验证码字符集, 去掉了一些容易混淆的字符, 如 0, O, 1, I
    private static final String CAPTCHA_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final String CAPTCHA_DIGITS = "0123456789";
    private static final Random random = new Random();

    // 验证码存储
    private final Map<UUID, CaptchaData> captchaMap = new HashMap<>();

    // 生成随机验证码字符串
    private static String generateText(int length, boolean isDigits) {
        var characters = isDigits? CAPTCHA_DIGITS : CAPTCHA_CHARACTERS;
        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            char character = characters.charAt(index);
            captcha.append(character);
        }
        return captcha.toString();
    }

    // 生成
    public CaptchaData createCaptchaData(int length, boolean isDigits) {
        var captchaData = new CaptchaData(
                UUID.randomUUID(),
                generateText(length, isDigits),
                LocalDateTime.now()
        );
        captchaMap.put(captchaData.getId(), captchaData);
        return captchaData;
    }

    // 验证
    public boolean validateCaptcha(CaptchaRO ro) {
        CaptchaData captchaData = captchaMap.get(ro.id);
        if (captchaData == null) {
            return false;
        }
        // 验证码只能使用一次
        captchaMap.remove(ro.id);
        return captchaData.getCode().equalsIgnoreCase(ro.value); //忽略大小写
    }

    // 定时清理过期验证码，每隔 5 分钟清理一次
    @Scheduled(fixedRate = 5 * 60 * 1000)
    private void cleanExpiredCaptcha() {
        captchaMap.entrySet().removeIf(
            entry -> entry.getValue().getGenerateTime().plusMinutes(5).isBefore(LocalDateTime.now())
        );
    }
}

