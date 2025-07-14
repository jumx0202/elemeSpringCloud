package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.NotificationResponseDTO;
import org.example.dto.VerificationCodeRequestDTO;
import org.example.dto.VerificationCodeValidateDTO;
import org.example.service.VerificationCodeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {
    
    private final StringRedisTemplate redisTemplate;
    
    private static final String CODE_PREFIX = "verification_code:";
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final int DEFAULT_CODE_LENGTH = 6;
    private static final int DEFAULT_EXPIRE_MINUTES = 5;
    private static final int DEFAULT_RATE_LIMIT_SECONDS = 60;
    
    @Override
    public NotificationResponseDTO sendVerificationCode(VerificationCodeRequestDTO request) {
        log.info("发送验证码请求，接收者：{}，类型：{}", request.getReceiver(), request.getCodeType());
        
        // 检查频率限制
        if (!checkRateLimit(request.getReceiver(), request.getClientIp())) {
            NotificationResponseDTO response = new NotificationResponseDTO();
            response.setType("VERIFICATION");
            response.setReceiver(request.getReceiver());
            response.setStatus("FAILED");
            response.setFailureReason("发送过于频繁，请稍后再试");
            response.setCreateTime(LocalDateTime.now());
            return response;
        }
        
        // 生成验证码
        String code = generateCode(DEFAULT_CODE_LENGTH);
        String key = CODE_PREFIX + request.getReceiver();
        
        // 存储到Redis
        redisTemplate.opsForValue().set(key, code, DEFAULT_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 设置频率限制
        String rateLimitKey = RATE_LIMIT_PREFIX + request.getReceiver();
        redisTemplate.opsForValue().set(rateLimitKey, "1", DEFAULT_RATE_LIMIT_SECONDS, TimeUnit.SECONDS);
        
        // 构造响应
        NotificationResponseDTO response = new NotificationResponseDTO();
        response.setType("VERIFICATION");
        response.setReceiver(request.getReceiver());
        response.setTitle("验证码");
        response.setContent("您的验证码是：" + code + "，" + DEFAULT_EXPIRE_MINUTES + "分钟内有效。");
        response.setStatus("SENT");
        response.setBusinessType("VERIFICATION");
        response.setCreateTime(LocalDateTime.now());
        
        log.info("验证码发送成功，接收者：{}，验证码：{}", request.getReceiver(), code);
        return response;
    }
    
    @Override
    public boolean validateVerificationCode(VerificationCodeValidateDTO validateDTO) {
        log.info("验证验证码，接收者：{}，代码：{}", validateDTO.getReceiver(), validateDTO.getCode());
        
        String key = CODE_PREFIX + validateDTO.getReceiver();
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (!StringUtils.hasText(storedCode)) {
            log.warn("验证码不存在或已过期，接收者：{}", validateDTO.getReceiver());
            return false;
        }
        
        boolean isValid = storedCode.equals(validateDTO.getCode());
        
        if (isValid) {
            // 验证成功后删除验证码
            redisTemplate.delete(key);
            log.info("验证码验证成功，接收者：{}", validateDTO.getReceiver());
        } else {
            log.warn("验证码验证失败，接收者：{}，输入：{}，期望：{}", 
                    validateDTO.getReceiver(), validateDTO.getCode(), storedCode);
        }
        
        return isValid;
    }
    
    @Override
    public boolean checkRateLimit(String receiver, String clientIp) {
        String rateLimitKey = RATE_LIMIT_PREFIX + receiver;
        String exists = redisTemplate.opsForValue().get(rateLimitKey);
        return !StringUtils.hasText(exists);
    }
    
    @Override
    public String generateCode(int length) {
        String chars = "0123456789";
        StringBuilder code = new StringBuilder();
        SecureRandom random = new SecureRandom();
        
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return code.toString();
    }
    
    @Override
    public void cleanupExpiredCodes() {
        // Redis的过期策略会自动清理过期的验证码
        log.info("验证码清理任务执行完成");
    }
}
