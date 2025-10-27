package charging_manage_be.controller.agent;


import charging_manage_be.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/agent")
public class AgentCheckToken {
    @Autowired
    private JwtUtil jwtUtil;
    @PostMapping("/check_token")
    public String checkToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No cookies found");
        }

        String accessToken = null;

        // 🔍 Duyệt cookie để lấy access_token
        for (Cookie cookie : cookies) {
            if ("jwt".equals(cookie.getName())) {
                accessToken = cookie.getValue();
                break;
            }
        }

        if (accessToken == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access token not found in cookies");
        }

        try {
            // 🧩 Giải mã và xác thực JWT
            String userName = jwtUtil.extractUsername(accessToken);
            return userName;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired access token");
        }
    }
}
