package charging_manage_be.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class    WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // server gửi message tới /topic/* với một simple broker là một
        // message broker là một thành phần trung gian chịu trách nhiệm nhận,
        // lưu trữ và chuyển tiếp các tin nhắn giữa các client và server.
        config.enableSimpleBroker("/topic");
        // client gửi message tới /app/*
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // client connect tới ws://localhost:8080/ws
        registry.addEndpoint("/ws").setAllowedOrigins("*");
    }
}