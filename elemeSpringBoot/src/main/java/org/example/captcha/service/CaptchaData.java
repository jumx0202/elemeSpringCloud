package org.example.captcha.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CaptchaData {
    // 验证码ID
    private UUID id;
    // 验证码字符串
    private String code;
    // 生成时间
    private LocalDateTime generateTime;
}
