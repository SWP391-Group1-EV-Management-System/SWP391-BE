package charging_manage_be.controller.users;

import charging_manage_be.model.dto.user.LoginRequest;
import charging_manage_be.model.dto.user.UserResponse;
import charging_manage_be.model.entity.users.UserEntity;
import charging_manage_be.security.JwtUtil;
import charging_manage_be.services.UserToken.CustomerDetailService;
import charging_manage_be.services.status_service.UserStatusService;
import charging_manage_be.services.users.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RequestMapping("/users")
@RestController
public class LoginController {
    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private CustomerDetailService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private UserService userService;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai tài khoản hoặc mật khẩu");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);
        final String refresh = jwtUtil.generateRefreshToken(userDetails);
        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true) // không cho JS đọc tránh XXS
                .secure(true) // chỉ gửi qua HTTPS
                .path("/")
                .maxAge(24 * 60 * 60) // 1 ngày
                .build();
        ResponseCookie cookie1 = ResponseCookie.from("refresh", refresh)
                .httpOnly(true) // không cho JS đọc tránh XXS
                .secure(true) // chỉ gửi qua HTTPS
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 ngày
                .build();
        String userId = userService.getUserEmail(request.getEmail()).getUserID();
        redisTemplate.opsForHash().put("user:" + userId, "refresh_token", refresh);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.SET_COOKIE, cookie1.toString())
                .body("Đăng nhập thành công!");
    }

    @GetMapping("/admin/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Chào ADMIN 👑");
    }
    // chỉ khi nào token cũ hết hạn thì mới cho gọi API này
    @PostMapping("/re-login")
    public ResponseEntity<?> reLogin(HttpServletRequest request) {
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        String email = jwtUtil.extractUsernameRefresh(refreshToken);
        String userId = userService.getUserEmail(email).getUserID();
        String storedRefreshToken = (String) redisTemplate.opsForHash().get("user:" + userId, "refresh_token");
        if(storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token không hợp lệ");
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        final String newJwt = jwtUtil.generateToken(userDetails);
        final String refresh = jwtUtil.generateRefreshToken(userDetails);
        ResponseCookie cookie = ResponseCookie.from("jwt", newJwt)
                .httpOnly(true) // không cho JS đọc tránh XXS
                .secure(true) // chỉ gửi qua HTTPS
                .path("/")
                .maxAge(24 * 60 * 60) // 1 ngày
                .build();
        ResponseCookie cookie1 = ResponseCookie.from("refresh", refresh)
                .httpOnly(true) // không cho JS đọc tránh XXS
                .secure(true) // chỉ gửi qua HTTPS
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 ngày
                .build();
        redisTemplate.opsForHash().put("user:" + userId, "refresh_token", refresh);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.SET_COOKIE, cookie1.toString())
                .body("Tạo mới token thành công");

    }
}



//@Autowired
//private UserService userService;
//@Autowired
//private UserStatusService userStatusService;
// @PostMapping("/login")
//    public ResponseEntity<?> loginPost(@RequestBody LoginRequest loginRequest, HttpSession session) {
//        System.out.println("Received login request for email: " + loginRequest.getEmail()); // Debug line
//
//        UserEntity user = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
//        if(user == null) {
//            return ResponseEntity.status(401).body("Sai email hoặc mật khẩu");
//        }
//        if(!user.isStatus()) {
//            return ResponseEntity.status(401).body("Tài Khoản Đã Bị Chặn");
//        }
//        UserResponse userResponse = new UserResponse(user.getUserID(), user.getEmail(),user.getFirstName(),user.getLastName(),user.getRole(), user.getCreatedAt());
//        session.setAttribute("userSession", userResponse);
//        String userStatus = userStatusService.getUserStatus(user.getUserID());
//        if(userStatus == null ) {
//            userStatusService.idleUserStatus(user.getUserID());
//        }
//        session.setAttribute("action", userStatus);
//        return ResponseEntity.ok(userResponse);
//    }