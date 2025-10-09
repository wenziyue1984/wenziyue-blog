package com.wenziyue.blog.biz.interceptor;

import com.wenziyue.security.service.UserDetailsServiceByIdOrToken;
import com.wenziyue.security.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

/**
 * @author wenziyue
 */

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceByIdOrToken userDetailsServiceByIdOrToken;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null) return message;

        StompCommand cmd = acc.getCommand();
        if (cmd == StompCommand.CONNECT) {
            // 1) 从 STOMP CONNECT 帧里拿 Authorization: Bearer <token>
            String auth = first(acc.getNativeHeader("Authorization"));
            if (auth == null || !auth.startsWith("Bearer ")) {
                throw new AccessDeniedException("Missing Authorization header");
            }
            String token = auth.substring(7);

            // 2) 校验 token 是否过期；解析 userId
            if (jwtUtils.isTokenExpired(token)) {
                throw new AccessDeniedException("Token expired");
            }
            String userId = jwtUtils.getUserIdFromToken(token);
            if (userId == null || userId.isEmpty()) {
                throw new AccessDeniedException("Invalid token subject");
            }

            // 3) 走你的业务校验：必须在 redis 的活跃 token 集合内
            UserDetails ud = userDetailsServiceByIdOrToken.loadUserByUserIdOrToken(userId, token);

            // 4) 构造认证对象并挂到会话
            // 这里把 principal 直接设成 userId（字符串），便于 convertAndSendToUser 按 userId 路由。
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, ud.getAuthorities());

            acc.setUser(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else if (cmd == StompCommand.SEND || cmd == StompCommand.SUBSCRIBE) {
            // 二次保护：必须已认证
            if (acc.getUser() == null) {
                throw new AccessDeniedException("Unauthenticated");
            }
        }
        return message;
    }

    private static String first(List<String> vals) {
        return (vals == null || vals.isEmpty()) ? null : vals.get(0);
    }
}
