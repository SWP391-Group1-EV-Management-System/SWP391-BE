package charging_manage_be.config;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
public class WebSocketEventListener {
    // class để debug
    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "Anonymous";

        System.out.println("🔌 [WebSocket] Client connected:");
        System.out.println("   Session ID: " + sessionId);
        System.out.println("   Username: " + username);
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "Anonymous";

        System.out.println("❌ [WebSocket] Client disconnected:");
        System.out.println("   Session ID: " + sessionId);
        System.out.println("   Username: " + username);
    }

    @EventListener
    public void handleWebSocketSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        String sessionId = headerAccessor.getSessionId();
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "Anonymous";

        System.out.println("🎧 [WebSocket] Client subscribed:");
        System.out.println("   Session ID: " + sessionId);
        System.out.println("   Username: " + username);
        System.out.println("   Destination: " + destination);
    }
}

