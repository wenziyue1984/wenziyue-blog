package com.wenziyue.blog.biz.config;

import com.wenziyue.blog.biz.interceptor.StompAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * @author wenziyue
 */
@EnableWebSocketMessageBroker
@Configuration
@RequiredArgsConstructor
public class WsStompConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 浏览器将连接的 HTTP 端点（随后升级为 WebSocket）
        registry.addEndpoint("/ws")
//                .setHandshakeHandler(new DemoPrincipalHandshakeHandler()) // 用于测试不用鉴权使用，测试完后删除
                .setAllowedOriginPatterns("*"); // 本地调试先放开
        // 如果需要 SockJS（可选）：.withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 客户端“发消息”要以 /app 开头
        registry.setApplicationDestinationPrefixes("/app");
        // 服务端“推消息”到这些前缀，使用内置简单代理
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setUserDestinationPrefix("/user"); // 可省，默认就是 /user
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
