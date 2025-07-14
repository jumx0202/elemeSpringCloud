package org.example.captcha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

import java.util.UUID;

// 用于返回给前端的验证码信息
@AllArgsConstructor
@Schema(description = "验证码信息")
public class CaptchaVO {
    @Schema(description = "验证码ID")
    public UUID id;
    @Schema(description = "验证码图片的Base64编码")
    public String imageBase64;
}
