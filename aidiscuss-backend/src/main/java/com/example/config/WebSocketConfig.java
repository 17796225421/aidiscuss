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
        // 允许多个来源的跨域请求
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://127.0.0.1:10001", "https://s1.v100.vip:8563") // 这里添加了https://s1.v100.vip:8563
                .withSockJS(); // 使用SockJS协议
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 设置消息代理的前缀
        registry.setApplicationDestinationPrefixes("/app");
        // 使用内置的消息代理，进行广播（/topic）或对特定用户发送（/user）
        registry.enableSimpleBroker("/topic");
    }
}