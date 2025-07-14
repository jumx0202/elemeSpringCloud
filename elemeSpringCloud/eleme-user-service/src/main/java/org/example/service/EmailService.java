package org.example.service;

/**
 * 邮件服务接口
 */
public interface EmailService {

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱地址
     * @return 是否发送成功
     */
    boolean sendVerificationCode(String email);

    /**
     * 验证邮箱验证码
     *
     * @param email 邮箱地址
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean verifyCode(String email, String code);
} 