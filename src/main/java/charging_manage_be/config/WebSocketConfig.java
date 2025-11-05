package charging_manage_be.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

@Configuration
@EnableWebSocketMessageBroker
public class    WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // server gửi message tới /topic/* và /queue/* với một simple broker
        // message broker là một thành phần trung gian chịu trách nhiệm nhận,
        // lưu trữ và chuyển tiếp các tin nhắn giữa các client và server.
        config.enableSimpleBroker("/topic", "/queue");
        // client gửi message tới /app/*
        config.setApplicationDestinationPrefixes("/app");
        // prefix cho user-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // client connect tới ws://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();  // Enable SockJS fallback

        // Endpoint without SockJS for native WebSocket clients
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    // ✅ Set Principal từ STOMP header để Spring WebSocket biết user là ai
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Lấy user-name từ STOMP header (Frontend gửi lên)
                    String username = accessor.getFirstNativeHeader("user-name");

                    if (username != null) {
                        System.out.println("🔐 [WebSocket] Setting principal for user: " + username);

                        // Set Principal để Spring biết user là ai và routing message đúng
                        Principal principal = new Principal() {
                            @Override
                            public String getName() {
                                return username;
                            }
                        };

                        accessor.setUser(principal);
                    } else {
                        System.out.println("⚠️ [WebSocket] No user-name in STOMP header!");
                    }
                }

                return message;
            }
        });
    }
}