package com.wenziyue.blog.biz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.wenziyue.blog.biz.utils.IdUtils;
import com.wenziyue.blog.biz.utils.SecurityUtils;
import com.wenziyue.blog.common.constants.RedisConstant;
import com.wenziyue.blog.common.enums.ThirdOauthProviderEnum;
import com.wenziyue.blog.common.enums.UserRoleEnum;
import com.wenziyue.blog.common.enums.UserStatusEnum;
import com.wenziyue.blog.common.exception.BlogResultCode;
import com.wenziyue.blog.dal.dto.GoogleLoginDTO;
import com.wenziyue.blog.dal.dto.LoginDTO;
import com.wenziyue.blog.biz.service.BizAuthService;
import com.wenziyue.blog.dal.entity.ThirdOauthEntity;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.blog.dal.service.ThirdOauthService;
import com.wenziyue.blog.dal.service.UserService;
import com.wenziyue.framework.exception.ApiException;
import com.wenziyue.redis.utils.RedisUtils;
import com.wenziyue.security.utils.JwtUtils;
import com.wenziyue.uid.core.IdGen;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

/**
 * @author wenziyue
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizAuthServiceImpl implements BizAuthService {

    private final UserService  userService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtils redisUtils;
    private final ThirdOauthService thirdOauthService;
    private final IdGen idGen;
    private final ObjectMapper objectMapper;

    @Value("${wenziyue.security.expire}")
    private Long expire;
    @Value("${wenziyue.security.token-prefix}")
    private String tokenPrefix;
    @Value("${wenziyue.security.token-header}")
    private String tokenHeader;
    @Value("${wenziyue.security.google.client-id}")
    private String googleClientId;


    @Override
    public String login(LoginDTO dto) {
        if (dto == null) {
            throw new ApiException(BlogResultCode.LOGIN_PARAM_IS_EMPTY);
        }
        // 校验验证码
//        if (dto.getCaptchaCode() == null || dto.getCaptchaCode().isEmpty()
//                || dto.getCaptchaUuid() == null || dto.getCaptchaUuid().isEmpty()) {
//            throw new ApiException(BlogResultCode.CAPTCHA_IS_EMPTY);
//        }
//        val captcha = redisUtils.get(RedisConstant.CAPTCHA_KEY + dto.getCaptchaUuid(), String.class);
//        if (captcha == null) {
//            throw new ApiException(BlogResultCode.CAPTCHA_HAS_EXPIRED);
//        }
//        if (!dto.getCaptchaCode().equals(captcha)) {
//            throw new ApiException(BlogResultCode.CAPTCHA_IS_ERROR);
//        }
//        // 删除校验通过的验证码
//        redisUtils.delete(RedisConstant.CAPTCHA_KEY + dto.getCaptchaUuid());

        // 校验登录信息
        if (dto.getName() == null || dto.getName().isEmpty()
                || dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new ApiException(BlogResultCode.REGISTER_PARAM_ERROR);
        }
        // 校验用户名密码
        val userEntity = userService.getOne(Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getName, dto.getName()));
        if (userEntity == null) {
            log.error("login用户不存在：{}", dto);
            throw new ApiException(BlogResultCode.LOGIN_PARAM_ERROR);
        }
        if (!passwordEncoder.matches(dto.getPassword(), userEntity.getPassword())) {
            log.error("login用户密码不对：{}", dto);
            throw new ApiException(BlogResultCode.LOGIN_PARAM_ERROR);
        }
        // 生成token
        val token = jwtUtils.generateToken(userEntity.getId().toString());

        // 将用户信息存入redis && 维护用户的所有活跃token
        SecurityUtils.userInfoSaveInRedisAndRefreshUserToken(redisUtils, userEntity, token, null, expire, objectMapper);

        return token;
    }

    @Override
    public String googleLogin(GoogleLoginDTO dto) {
        try {
            // 1. 验证 ID Token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(dto.getIdToken());
            if (idToken == null) {
                throw new ApiException(BlogResultCode.LOGIN_PARAM_ERROR, "无效的 Google ID Token");
            }

            // 2. 从 Token 提取用户信息
            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleUserId = payload.getSubject(); // Google 用户唯一 ID
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            // 3. 查找对应UserEntity，没有的话创建一个
            UserEntity userEntity;
            ThirdOauthEntity thirdOauthEntity = thirdOauthService.getOne(Wrappers.<ThirdOauthEntity>lambdaQuery()
                    .eq(ThirdOauthEntity::getProvider, ThirdOauthProviderEnum.GOOGLE)
                    .eq(ThirdOauthEntity::getProviderUid, googleUserId));
            if (thirdOauthEntity == null) {
                // 创建用户
                userEntity = UserEntity.builder()
                        .id(IdUtils.getID(idGen))
                        .name(name)
                        .email(email)
                        .avatarUrl(picture)
                        .status(UserStatusEnum.ENABLED)
                        .role(UserRoleEnum.USER)
                        .build();
                userService.save(userEntity);
                // 创建第三方授权信息
                thirdOauthEntity = ThirdOauthEntity.builder()
                        .id(IdUtils.getID(idGen))
                        .userId(userEntity.getId())
                        .provider(ThirdOauthProviderEnum.GOOGLE)
                        .providerUid(googleUserId).extra(payload).build();
                thirdOauthService.save(thirdOauthEntity);
            } else {
                userEntity = userService.getById(thirdOauthEntity.getUserId());
                if (userEntity == null) {
                    throw new ApiException(BlogResultCode.USER_NOT_EXIST);
                }
            }

            // 4. 颁发我们自己的 JWT
            val token = jwtUtils.generateToken(userEntity.getId().toString());

            // 5. 缓存用户信息 + 维护活跃集合
            SecurityUtils.userInfoSaveInRedisAndRefreshUserToken(redisUtils, userEntity, token, null, expire, objectMapper);
            return token;
        } catch (Exception e) {
            log.error("google登录异常", e);
            throw new ApiException(BlogResultCode.LOGIN_ERROR);
        }
    }

    /**
     * 将redis中的用户信息作为token是否有效的依据，如果存在则认为token有效，否则认为token无效
     */
    @Override
    public boolean logout(HttpServletRequest request) {
        String token = SecurityUtils.getTokenFromRequest(request, tokenHeader, tokenPrefix);
        if (token == null) {
            return false;
        }
        // 删除redis中的用户信息
        redisUtils.delete(RedisConstant.LOGIN_TOKEN_KEY + token);
        // 解析用户id
        val userIdFromToken = jwtUtils.getUserIdFromToken(token);
        // 维护用户的所有活跃token
        redisUtils.sRemove(RedisConstant.USER_TOKENS_KEY + userIdFromToken, token);
        return true;
    }

    @Override
    public void forceLogout(Long id) {
        val tokenSet = redisUtils.sMembers(RedisConstant.USER_TOKENS_KEY + id);
        if (tokenSet == null || tokenSet.isEmpty()) {
            return;
        }
        tokenSet.forEach(token -> redisUtils.delete(RedisConstant.LOGIN_TOKEN_KEY + token.toString()));
        redisUtils.delete(RedisConstant.USER_TOKENS_KEY + id);
    }

    @Override
    public boolean nameExists(String name) {
        val list = userService.list(Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getName, name.trim()));
        return !list.isEmpty();
    }
}
