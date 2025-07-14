package org.example.captcha;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.example.captcha.dto.CaptchaRO;
import org.example.captcha.dto.CaptchaVO;
import org.example.captcha.service.CaptchaImageGenerator;
import org.example.captcha.service.CaptchaService;

import java.io.IOException;
import java.util.Base64;

@RestController
@Tag(name = "captcha", description = "验证码相关接口")
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }


    // 返回 CaptchaVO 的 API
    @Operation(summary = "获取验证码", description = "获取验证码")
    @GetMapping("/captcha")
    public CaptchaVO getCaptcha() throws IOException {
        var captchaData = captchaService.createCaptchaData(4, false);
        var captchaImgBytes = CaptchaImageGenerator.generateJpegImg(captchaData.getCode(), 100, 30);

        return new CaptchaVO(
                captchaData.getId(),
                "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(captchaImgBytes)
        );
    }

    @Operation(summary = "验证验证码", description = "验证用户输入的验证码是否正确")
    @PostMapping("/captcha")
    public boolean validateCaptcha(@RequestBody CaptchaRO Ro) {
        return captchaService.validateCaptcha(Ro);
    }
}