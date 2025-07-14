package org.example.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.ChineseCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.CaptchaGenerateRequest;
import org.example.dto.CaptchaValidateRequest;
import org.example.dto.CaptchaResponse;
import org.example.service.CaptchaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {
    
    private final StringRedisTemplate redisTemplate;
    
    @Value("${captcha.image.type:arithmetic}")
    private String imageCaptchaType;
    
    @Value("${captcha.image.length:4}")
    private Integer imageCaptchaLength;
    
    @Value("${captcha.image.width:120}")
    private Integer imageCaptchaWidth;
    
    @Value("${captcha.image.height:40}")
    private Integer imageCaptchaHeight;
    
    @Value("${captcha.image.expire-seconds:300}")
    private Integer imageCaptchaExpireSeconds;
    
    @Value("${captcha.sms.length:6}")
    private Integer smsCodeLength;
    
    @Value("${captcha.sms.expire-seconds:300}")
    private Integer smsCodeExpireSeconds;
    
    @Value("${captcha.sms.rate-limit-seconds:60}")
    private Integer smsRateLimitSeconds;
    
    @Value("${captcha.email.length:6}")
    private Integer emailCodeLength;
    
    @Value("${captcha.email.expire-seconds:300}")
    private Integer emailCodeExpireSeconds;
    
    @Value("${captcha.email.rate-limit-seconds:60}")
    private Integer emailRateLimitSeconds;
    
    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    
    @Override
    @SentinelResource(value = "generateCaptcha", fallback = "generateCaptchaFallback")
    public CaptchaResponse generateCaptcha(CaptchaGenerateRequest request) {
        log.info("生成验证码请求，类型：{}", request.getType());
        
        // 检查频率限制
        if (!checkRateLimit(request.getType(), getIdentifier(request))) {
            CaptchaResponse response = new CaptchaResponse();
            response.setSuccess(false);
            response.setMessage("请求过于频繁，请稍后再试");
            return response;
        }
        
        switch (request.getType().toLowerCase()) {
            case "image":
                return generateImageCaptcha(request);
            case "sms":
                return generateSmsVerificationCode(request);
            case "email":
                return generateEmailVerificationCode(request);
            case "slider":
                return generateSliderCaptcha(request);
            case "click":
                return generateClickCaptcha(request);
            default:
                throw new IllegalArgumentException("不支持的验证码类型: " + request.getType());
        }
    }
    
    @Override
    public boolean validateCaptcha(CaptchaValidateRequest request) {
        log.info("验证验证码请求，类型：{}，Key：{}", request.getType(), request.getKey());
        
        String key = CAPTCHA_PREFIX + request.getKey();
        String storedValue = redisTemplate.opsForValue().get(key);
        
        if (!StringUtils.hasText(storedValue)) {
            log.warn("验证码不存在或已过期，Key：{}", request.getKey());
            return false;
        }
        
        boolean isValid = false;
        
        switch (request.getType().toLowerCase()) {
            case "image":
            case "sms":
            case "email":
                isValid = storedValue.equalsIgnoreCase(request.getValue());
                break;
            case "slider":
                isValid = validateSliderCaptcha(storedValue, request.getSliderPosition());
                break;
            case "click":
                isValid = validateClickCaptcha(storedValue, request.getClickPoints());
                break;
            default:
                log.error("不支持的验证码类型: {}", request.getType());
                return false;
        }
        
        if (isValid && request.getUseImmediately()) {
            // 验证成功后立即删除验证码
            redisTemplate.delete(key);
            log.info("验证码验证成功并已删除，Key：{}", request.getKey());
        }
        
        return isValid;
    }
    
    @Override
    public CaptchaResponse generateImageCaptcha(CaptchaGenerateRequest request) {
        try {
            // 创建验证码对象
            Captcha captcha = createCaptcha(request);
            
            // 生成验证码
            String captchaText = captcha.text();
            String captchaBase64 = captcha.toBase64();
            
            // 生成唯一Key
            String key = generateCaptchaKey();
            
            // 存储到Redis
            String redisKey = CAPTCHA_PREFIX + key;
            redisTemplate.opsForValue().set(redisKey, captchaText, imageCaptchaExpireSeconds, TimeUnit.SECONDS);
            
            // 构造响应
            CaptchaResponse response = new CaptchaResponse();
            response.setKey(key);
            response.setType("image");
            response.setImageBase64(captchaBase64);
            response.setImageDataUrl("data:image/png;base64," + captchaBase64);
            response.setExpireSeconds(imageCaptchaExpireSeconds);
            response.setExpireTime(LocalDateTime.now().plusSeconds(imageCaptchaExpireSeconds));
            response.setCreateTime(LocalDateTime.now());
            
            log.info("图形验证码生成成功，Key：{}", key);
            return response;
            
        } catch (Exception e) {
            log.error("图形验证码生成失败", e);
            throw new RuntimeException("图形验证码生成失败: " + e.getMessage());
        }
    }
    
    @Override
    public CaptchaResponse generateSmsVerificationCode(CaptchaGenerateRequest request) {
        if (!StringUtils.hasText(request.getReceiver())) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        
        // 生成验证码
        String code = generateRandomCode(smsCodeLength);
        String key = generateCaptchaKey();
        
        // 存储到Redis
        String redisKey = CAPTCHA_PREFIX + key;
        redisTemplate.opsForValue().set(redisKey, code, smsCodeExpireSeconds, TimeUnit.SECONDS);
        
        // 设置频率限制
        String rateLimitKey = RATE_LIMIT_PREFIX + "sms:" + request.getReceiver();
        redisTemplate.opsForValue().set(rateLimitKey, "1", smsRateLimitSeconds, TimeUnit.SECONDS);
        
        // 构造响应
        CaptchaResponse response = new CaptchaResponse();
        response.setKey(key);
        response.setType("sms");
        response.setReceiver(request.getReceiver());
        response.setSendStatus("sent");
        response.setExpireSeconds(smsCodeExpireSeconds);
        response.setExpireTime(LocalDateTime.now().plusSeconds(smsCodeExpireSeconds));
        response.setCreateTime(LocalDateTime.now());
        
        log.info("短信验证码生成成功，Key：{}，接收者：{}", key, request.getReceiver());
        return response;
    }
    
    @Override
    public CaptchaResponse generateEmailVerificationCode(CaptchaGenerateRequest request) {
        if (!StringUtils.hasText(request.getReceiver())) {
            throw new IllegalArgumentException("邮箱地址不能为空");
        }
        
        // 生成验证码
        String code = generateRandomCode(emailCodeLength);
        String key = generateCaptchaKey();
        
        // 存储到Redis
        String redisKey = CAPTCHA_PREFIX + key;
        redisTemplate.opsForValue().set(redisKey, code, emailCodeExpireSeconds, TimeUnit.SECONDS);
        
        // 设置频率限制
        String rateLimitKey = RATE_LIMIT_PREFIX + "email:" + request.getReceiver();
        redisTemplate.opsForValue().set(rateLimitKey, "1", emailRateLimitSeconds, TimeUnit.SECONDS);
        
        // 构造响应
        CaptchaResponse response = new CaptchaResponse();
        response.setKey(key);
        response.setType("email");
        response.setReceiver(request.getReceiver());
        response.setSendStatus("sent");
        response.setExpireSeconds(emailCodeExpireSeconds);
        response.setExpireTime(LocalDateTime.now().plusSeconds(emailCodeExpireSeconds));
        response.setCreateTime(LocalDateTime.now());
        
        log.info("邮箱验证码生成成功，Key：{}，接收者：{}", key, request.getReceiver());
        return response;
    }
    
    @Override
    public CaptchaResponse generateSliderCaptcha(CaptchaGenerateRequest request) {
        // 这里简化实现，实际应用中需要生成带缺口的背景图和拼图
        String key = generateCaptchaKey();
        int correctPosition = new SecureRandom().nextInt(200) + 50; // 50-250像素范围
        
        // 存储正确位置到Redis
        String redisKey = CAPTCHA_PREFIX + key;
        redisTemplate.opsForValue().set(redisKey, String.valueOf(correctPosition), 300, TimeUnit.SECONDS);
        
        CaptchaResponse response = new CaptchaResponse();
        response.setKey(key);
        response.setType("slider");
        response.setBackgroundImage("/api/captcha/slider/background/" + key);
        response.setSliderImage("/api/captcha/slider/piece/" + key);
        response.setExpireSeconds(300);
        response.setExpireTime(LocalDateTime.now().plusSeconds(300));
        response.setCreateTime(LocalDateTime.now());
        
        log.info("滑动验证码生成成功，Key：{}", key);
        return response;
    }
    
    @Override
    public CaptchaResponse generateClickCaptcha(CaptchaGenerateRequest request) {
        // 这里简化实现，实际应用中需要生成带文字的图片
        String key = generateCaptchaKey();
        String clickText = "请点击文字：春";
        
        // 存储点击答案到Redis（简化为固定坐标）
        String redisKey = CAPTCHA_PREFIX + key;
        redisTemplate.opsForValue().set(redisKey, "100,150", 300, TimeUnit.SECONDS);
        
        CaptchaResponse response = new CaptchaResponse();
        response.setKey(key);
        response.setType("click");
        response.setClickImage("/api/captcha/click/image/" + key);
        response.setClickText(clickText);
        response.setExpireSeconds(300);
        response.setExpireTime(LocalDateTime.now().plusSeconds(300));
        response.setCreateTime(LocalDateTime.now());
        
        log.info("点选验证码生成成功，Key：{}", key);
        return response;
    }
    
    @Override
    public boolean checkRateLimit(String type, String identifier) {
        if (!StringUtils.hasText(identifier)) {
            return true;
        }
        
        String rateLimitKey = RATE_LIMIT_PREFIX + type + ":" + identifier;
        String exists = redisTemplate.opsForValue().get(rateLimitKey);
        
        return !StringUtils.hasText(exists);
    }
    
    @Override
    public void cleanupExpiredCaptcha() {
        // Redis的过期策略会自动清理过期的验证码
        log.info("验证码清理任务执行完成");
    }
    
    /**
     * 创建验证码对象
     */
    private Captcha createCaptcha(CaptchaGenerateRequest request) {
        int width = request.getWidth() != null ? request.getWidth() : imageCaptchaWidth;
        int height = request.getHeight() != null ? request.getHeight() : imageCaptchaHeight;
        int length = request.getLength() != null ? request.getLength() : imageCaptchaLength;
        
        Captcha captcha;
        switch (imageCaptchaType) {
            case "chinese":
                captcha = new ChineseCaptcha(width, height, length);
                break;
            case "spec":
                captcha = new SpecCaptcha(width, height, length);
                break;
            case "arithmetic":
            default:
                captcha = new ArithmeticCaptcha(width, height);
                break;
        }
        
        captcha.setCharType(Captcha.TYPE_DEFAULT);
        return captcha;
    }
    
    /**
     * 生成随机验证码
     */
    private String generateRandomCode(int length) {
        String chars = "0123456789";
        StringBuilder code = new StringBuilder();
        SecureRandom random = new SecureRandom();
        
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return code.toString();
    }
    
    /**
     * 生成验证码Key
     */
    private String generateCaptchaKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 获取标识符
     */
    private String getIdentifier(CaptchaGenerateRequest request) {
        if (StringUtils.hasText(request.getReceiver())) {
            return request.getReceiver();
        }
        return request.getClientIp();
    }
    
    /**
     * 验证滑动验证码
     */
    private boolean validateSliderCaptcha(String storedValue, Integer position) {
        try {
            int correctPosition = Integer.parseInt(storedValue);
            int tolerance = 5; // 允许5像素误差
            return Math.abs(correctPosition - position) <= tolerance;
        } catch (NumberFormatException e) {
            log.error("滑动验证码位置解析失败", e);
            return false;
        }
    }
    
    /**
     * 验证点选验证码
     */
    private boolean validateClickCaptcha(String storedValue, java.util.List<CaptchaValidateRequest.CaptchaPoint> clickPoints) {
        try {
            String[] coords = storedValue.split(",");
            if (coords.length != 2 || clickPoints == null || clickPoints.isEmpty()) {
                return false;
            }
            
            int correctX = Integer.parseInt(coords[0]);
            int correctY = Integer.parseInt(coords[1]);
            int tolerance = 10; // 允许10像素误差
            
            for (CaptchaValidateRequest.CaptchaPoint point : clickPoints) {
                if (Math.abs(point.getX() - correctX) <= tolerance && 
                    Math.abs(point.getY() - correctY) <= tolerance) {
                    return true;
                }
            }
            
            return false;
        } catch (NumberFormatException e) {
            log.error("点选验证码坐标解析失败", e);
            return false;
        }
    }
    
    /**
     * 生成验证码失败回调
     */
    public CaptchaResponse generateCaptchaFallback(CaptchaGenerateRequest request, Throwable ex) {
        log.error("验证码生成服务降级，类型：{}，错误：{}", request.getType(), ex.getMessage());
        
        CaptchaResponse response = new CaptchaResponse();
        response.setSuccess(false);
        response.setMessage("验证码服务暂时不可用，请稍后再试");
        response.setType(request.getType());
        response.setCreateTime(LocalDateTime.now());
        
        return response;
    }
} 