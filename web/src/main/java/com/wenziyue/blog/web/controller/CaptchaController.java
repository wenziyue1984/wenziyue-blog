package com.wenziyue.blog.web.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.wenziyue.blog.common.constants.RedisConstant;
import com.wenziyue.blog.dal.dto.CaptchaDTO;
import com.wenziyue.framework.annotation.ResponseResult;
import com.wenziyue.redis.utils.RedisUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码接口
 *
 * @author wenziyue
 */
@RestController
@RequestMapping("/captcha")
@Slf4j
@RequiredArgsConstructor
@ResponseResult
@Tag(name = "验证码", description = "验证码接口")
public class CaptchaController {

    private final RedisUtils redisUtils;

    @GetMapping
    public CaptchaDTO captcha() {
        // 生成验证码
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 80, 4, 20); // 宽,高,位数,干扰线
        String code = captcha.getCode();
        String uuid = UUID.randomUUID().toString();

        // 存入redis
        redisUtils.set(RedisConstant.CAPTCHA_KEY + uuid, code, 5, TimeUnit.MINUTES);
        return CaptchaDTO.builder()
                .captcha(captcha.getImageBase64Data())
                .uuid(uuid).build();
    }

}
