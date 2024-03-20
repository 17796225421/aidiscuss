package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // 开启WebSocket消息代理
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://127.0.0.1:10001") // 允许指定的源进行跨域请求
                .withSockJS(); // 使用SockJS协议
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 设置消息代理的前缀，即如果消息的目的地是以"/app"开头，那么将会路由到@MessageMapping注解方法中，而非代理到外部消息系统
        registry.setApplicationDestinationPrefixes("/app");
        // 使用内置的消息代理，进行广播（/topic）或对特定用户发送（/user）
        registry.enableSimpleBroker("/topic");
    }
}