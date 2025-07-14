package org.example.captcha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.UUID;

@Schema(description = "验证码信息")
public class CaptchaRO {
    @Schema(description = "验证码ID")
    public UUID id;
    @NotEmpty
    @Schema(description = "验证码")
    public String value;
}
